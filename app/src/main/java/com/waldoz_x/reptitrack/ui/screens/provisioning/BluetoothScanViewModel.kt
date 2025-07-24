// com.waldoz_x.reptitrack.ui.screens.provisioning.BluetoothScanViewModel.kt

package com.waldoz_x.reptitrack.ui.screens.provisioning

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.espressif.provisioning.*
import com.espressif.provisioning.listeners.BleScanListener
import com.espressif.provisioning.listeners.ResponseListener
import com.waldoz_x.reptitrack.data.model.ConnectionState
import com.waldoz_x.reptitrack.data.model.DispositivoBLE
import com.waldoz_x.reptitrack.data.repository.ProvisioningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import javax.inject.Inject


@HiltViewModel
class BluetoothScanViewModel @Inject constructor(
    private val provisioningRepository: ProvisioningRepository
) : ViewModel() {

    private val _dispositivos = MutableStateFlow<List<DispositivoBLE>>(emptyList())
    val dispositivos: StateFlow<List<DispositivoBLE>> = _dispositivos

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _haEscaneado = MutableStateFlow(false)
    val haEscaneado: StateFlow<Boolean> = _haEscaneado

    private val _mensaje = MutableSharedFlow<String>()
    val mensaje: SharedFlow<String> = _mensaje

    private val _solicitarActivarBluetooth = MutableSharedFlow<Unit>()
    val solicitarActivarBluetooth: SharedFlow<Unit> = _solicitarActivarBluetooth

    private var espDevice: ESPDevice? = null

    private var username: String? = null
    private var proof: String? = null

    var isConnecting by mutableStateOf(false)
        private set

    var isDeviceConnected by mutableStateOf(false)
        private set

    private var connectTimeoutJob: Job? = null

    // Nuevo: mapa que guarda el estado de conexión de cada dispositivo por su dirección MAC
    private val _connectionStates = MutableStateFlow<Map<String, ConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, ConnectionState>> = _connectionStates


    private val _navegarAWifi = MutableSharedFlow<Unit>()
    val navegarAWifi: SharedFlow<Unit> = _navegarAWifi.asSharedFlow()

    init {
        viewModelScope.launch {
            provisioningRepository.username.collect {
                username = it
                Log.d("BluetoothScanVM", "Username recibido: $username")
            }
        }
        viewModelScope.launch {
            provisioningRepository.proof.collect {
                proof = it
                Log.d("BluetoothScanVM", "Proof recibido: $proof")
            }
        }

        EventBus.getDefault().register(this)
    }



    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    private fun setConnectionState(address: String, state: ConnectionState) {
        val newMap = _connectionStates.value.toMutableMap()
        newMap[address] = state
        _connectionStates.value = newMap
    }

    fun startConnectTimeout(address: String) {
        connectTimeoutJob?.cancel()
        connectTimeoutJob = viewModelScope.launch {
            delay(20_000) // 20 segundos de timeout
            Log.e("BluetoothScanVM", "Timeout de conexión alcanzado para $address")
            setConnectionState(address, ConnectionState.ERROR)

            // Espera 5 segundos para que el mensaje se vea
            delay(4_000) //
            _mensaje.emit("Tiempo de espera agotado. Desconectando dispositivo...")

            // Intentamos desconectar por timeout, usando la función centralizada
            desconectarDispositivo(address)
        }
    }


    fun cancelConnectTimeout() {
        connectTimeoutJob?.cancel()
    }

    fun limpiarEstados() {
        val nuevosEstados = connectionStates.value.toMutableMap()
        nuevosEstados.keys.forEach {
            nuevosEstados[it] = ConnectionState.IDLE
        }
        _connectionStates.value = nuevosEstados
    }


    @Suppress("MissingPermission")
    fun iniciarEscaneo(context: Context) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            viewModelScope.launch {
                _solicitarActivarBluetooth.emit(Unit)
                _mensaje.emit("El Bluetooth está desactivado")
            }
            return
        }

        if (_isScanning.value) return

        _isScanning.value = true
        _haEscaneado.value = false
        _dispositivos.value = emptyList()
        _connectionStates.value = emptyMap() // Resetear estados de conexión

        val provisionManager = ESPProvisionManager.getInstance(context)

        provisionManager.searchBleEspDevices(object : BleScanListener {
            override fun scanStartFailed() {
                viewModelScope.launch { _mensaje.emit("Bluetooth no disponible") }
                _isScanning.value = false
            }

            override fun onPeripheralFound(device: BluetoothDevice, scanResult: android.bluetooth.le.ScanResult) {
                val serviceUuid = scanResult.scanRecord?.serviceUuids?.firstOrNull()?.toString() ?: return
                val deviceName = device.name ?: "Sin nombre"
                val newDevice = DispositivoBLE(deviceName, device.address, serviceUuid)

                if (_dispositivos.value.none { it.address == device.address }) {
                    val currentList = _dispositivos.value.toMutableList()
                    currentList.add(newDevice)

                    // Ordenar: primero los que comienzan con "ReptiTrack_BLE" alfabéticamente,
                    // luego el resto también ordenados alfabéticamente (opcional)
                    val sortedList = currentList.sortedWith(compareBy<DispositivoBLE> {
                        // Primero los que no empiezan con "ReptiTrack_BLE" reciben 1 (después)
                        if (it.name.startsWith("ReptiTrack_BLE")) 0 else 1
                    }.thenBy { it.name })

                    _dispositivos.value = sortedList
                }
            }


            override fun scanCompleted() {
                _isScanning.value = false
                _haEscaneado.value = true
            }

            override fun onFailure(e: Exception) {
                _isScanning.value = false
                viewModelScope.launch { _mensaje.emit("Error al escanear: ${e.message}") }
            }
        })
    }

    fun conectarDispositivo(context: Context, dispositivo: DispositivoBLE) {
        val currentState = connectionStates.value[dispositivo.address]

        if (currentState == ConnectionState.CONNECTING) {
            Log.d("BluetoothScanVM", "Ya se está intentando conectar a ${dispositivo.address}, se evita conexión duplicada.")
            return
        }
        if (currentState == ConnectionState.ERROR) {
            Log.d("BluetoothScanVM", "Dispositivo ${dispositivo.address} en estado ERROR, esperar a resetear antes de conectar.")
            return
        }

        // Actualiza el estado de ese dispositivo a CONNECTING
        setConnectionState(dispositivo.address, ConnectionState.CONNECTING)

        val provisionManager = ESPProvisionManager.getInstance(context)
        espDevice = provisionManager.createESPDevice(
            ESPConstants.TransportType.TRANSPORT_BLE,
            ESPConstants.SecurityType.SECURITY_1
        )
        val btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(dispositivo.address)
        espDevice?.bluetoothDevice = btDevice
        espDevice?.connectBLEDevice(btDevice, dispositivo.serviceUuid)
        startConnectTimeout(dispositivo.address)
    }

        fun desconectarDispositivo(address: String) {
            try {
                espDevice?.disconnectDevice()
                setConnectionState(address, ConnectionState.IDLE)
            } catch (e: Exception) {
                Log.e("BluetoothScanVM", "Error al desconectar dispositivo: ${e.message}")
            }
        }



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceConnectionEvent(event: DeviceConnectionEvent) {
        val address = espDevice?.bluetoothDevice?.address ?: return

        when (event.eventType) {
            ESPConstants.EVENT_DEVICE_CONNECTED -> {
                cancelConnectTimeout()
                setConnectionState(address, ConnectionState.IDLE)
                viewModelScope.launch { _mensaje.emit("Dispositivo conectado") }

                espDevice?.let { device ->
                    val versionInfo = device.versionInfo
                    val secVer = try {
                        JSONObject(versionInfo).optJSONObject("prov")?.optInt("sec_ver", 1) ?: 1
                    } catch (e: Exception) {
                        1
                    }

                    when (secVer) {
                        0 -> device.setSecurityType(ESPConstants.SecurityType.SECURITY_0)
                        1 -> device.setSecurityType(ESPConstants.SecurityType.SECURITY_1)
                        2 -> {
                            device.setSecurityType(ESPConstants.SecurityType.SECURITY_2)
                            device.userName = username
                            device.proofOfPossession = proof
                        }
                    }

                    device.initSession(object : ResponseListener {
                        override fun onSuccess(data: ByteArray?) {
                            /*
                             Es una forma de iniciar una tarea asíncrona (una coroutine) dentro del ViewModel que se
                             ejecuta en segundo plano, sin bloquear la interfaz de usuario, y que se cancela
                             automáticamente cuando el ViewModel se destruye, evitando pérdidas de memoria o
                             procesos colgando
                            */
                            viewModelScope.launch {
                                _mensaje.emit("Sesión segura establecida")
                                _dispositivos.value = _dispositivos.value.filterNot { it.address == address }
                                provisioningRepository.saveEspDevice(device)
                                _navegarAWifi.emit(Unit)
                            }
                        }

                        override fun onFailure(e: Exception?) {
                            viewModelScope.launch { _mensaje.emit("Error en sesión segura") }
                        }
                    })
                }
            }

            ESPConstants.EVENT_DEVICE_DISCONNECTED -> {
                cancelConnectTimeout()
                setConnectionState(address, ConnectionState.IDLE)
                viewModelScope.launch { _mensaje.emit("Dispositivo desconectado") }
            }

            ESPConstants.EVENT_DEVICE_CONNECTION_FAILED -> {
                cancelConnectTimeout()
                Log.e("BluetoothScanVM", "Evento conexión fallida detectado")
                setConnectionState(address, ConnectionState.ERROR)
                viewModelScope.launch {
                    _mensaje.emit("Error: comunicación fallida. Dispositivo no compatible o desconectado.")
                    delay(10_000)  // Espera 10 segundos mostrando el mensaje de error
                    desconectarDispositivo(address)
                }
            }


        }
    }
}
