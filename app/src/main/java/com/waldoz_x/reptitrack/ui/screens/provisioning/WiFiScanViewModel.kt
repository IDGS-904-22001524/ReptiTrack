package com.waldoz_x.reptitrack.presentation.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espressif.provisioning.ESPDevice
import com.espressif.provisioning.listeners.WiFiScanListener
import com.espressif.provisioning.WiFiAccessPoint
import com.waldoz_x.reptitrack.data.repository.ProvisioningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WiFiScanViewModel @Inject constructor(
    private val provisioningRepository: ProvisioningRepository
) : ViewModel() {

    private val _mensaje = MutableSharedFlow<String>()
    val mensaje: SharedFlow<String> = _mensaje.asSharedFlow()

    // Cambiado a List<WiFiAccessPoint>
    private val _wifiList = MutableStateFlow<List<WiFiAccessPoint>>(emptyList())
    val wifiList: StateFlow<List<WiFiAccessPoint>> = _wifiList.asStateFlow()

    val ssid = MutableStateFlow("")
    val password = MutableStateFlow("")
    val redOculta = MutableStateFlow(false)

    val isScanning = MutableStateFlow(false)

    private val device: ESPDevice?
        get() = provisioningRepository.espDevice.value

    val deviceDisconnected = provisioningRepository.deviceDisconnected

    fun clearDisconnectedFlag() {
        provisioningRepository.clearDisconnectedFlag()
    }

    init {
        escanearRedes()
    }

    fun desconectarDispositivo() {
        viewModelScope.launch {
            provisioningRepository.desconectarDispositivo()
            _mensaje.emit("Dispositivo desconectado")
        }
    }

    fun saveWifiCredentials(ssid: String, password: String) {
        provisioningRepository.saveWifiCredentials(ssid, password)
    }

    fun limpiarMensaje() {
        viewModelScope.launch {
            _mensaje.emit("")
        }
    }

    fun escanearRedes() {
        isScanning.value = true

        device?.scanNetworks(object : WiFiScanListener {
            override fun onWifiListReceived(wifiList: ArrayList<WiFiAccessPoint>) {
                // Se actualiza la lista completa, no sólo nombres
                _wifiList.value = wifiList.distinctBy { it.wifiName }
                isScanning.value = false
            }

            override fun onWiFiScanFailed(e: Exception) {
                viewModelScope.launch {
                    _mensaje.emit("Error al escanear redes Wi-Fi")
                }
                isScanning.value = false
            }
        }) ?: viewModelScope.launch {
            _mensaje.emit("Dispositivo no disponible")
            isScanning.value = false

        }
    }


}
