// Definimos el paquete donde se encuentra esta clase. 
// Es una convención en Kotlin/Java para organizar el código según su función dentro del proyecto.
package com.waldoz_x.reptitrack.data.repository

// Importamos clases necesarias de Android y Kotlin Flow:

// Context: proporciona acceso a recursos del sistema, como DataStore o archivos.
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf

// Funciones para editar y definir claves de preferencias (útiles para guardar datos en DataStore).
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPDevice
import com.espressif.provisioning.listeners.ProvisionListener
import com.espressif.provisioning.listeners.ResponseListener
import org.json.JSONObject


// Importamos una extensión que expone el DataStore configurado previamente en el proyecto.
import com.waldoz_x.reptitrack.data.preferences.dataStore

// Flow y operadores de Flow nos permiten trabajar con flujos de datos asíncronos y reactivos.
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Anotaciones y clases de Dagger Hilt para la inyección de dependencias.
import javax.inject.Inject // Marca un constructor o campo para inyección automática.
import javax.inject.Singleton // Indica que esta clase debe tener una sola instancia (singleton).
import dagger.hilt.android.qualifiers.ApplicationContext // Proporciona el contexto de la aplicación para evitar fugas de memoria.
import kotlinx.coroutines.flow.StateFlow
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


// Definimos la clase ProvisioningRepository que actuará como una fuente de datos para llevar todo el proceso de aprovisionamiento
@Singleton // Esta anotación le dice a Hilt que esta clase debe existir como una sola instancia global (singleton).
class ProvisioningRepository @Inject constructor(
    @ApplicationContext private val context: Context // Inyectamos el contexto de la aplicación usando Hilt.
) {

    // Variables internas privadas que representan flujos de estado modificables.
    // Se utilizan para guardar temporalmente en memoria las credenciales del usuario.

    private val _username = MutableStateFlow<String?>(null) // MutableStateFlow permite emitir nuevos valores de manera reactiva.
    private val _proof = MutableStateFlow<String?>(null)    // Se inicializan con null y podrán cambiar durante el flujo de provisión.

    // Variables públicas que exponen los flujos anteriores como Flow de solo lectura.
    // asStateFlow() transforma un MutableStateFlow en un StateFlow de solo lectura.
    // Esto garantiza que otras clases puedan observar los datos, pero no modificarlos directamente.

    val username: Flow<String?> = _username.asStateFlow() // Exposición pública de _username.
    val proof: Flow<String?> = _proof.asStateFlow()       // Exposición pública de _proof.

    // Función pública para guardar credenciales en memoria.
    // Asigna los valores pasados como parámetro a las variables privadas _username y _proof.
    // Esto permite que los observadores de los flujos reaccionen automáticamente a los cambios.

    private val _espDevice = MutableStateFlow<ESPDevice?>(null)
    val espDevice: StateFlow<ESPDevice?> = _espDevice.asStateFlow()

    private val _deviceDisconnected = MutableStateFlow(false)
    val deviceDisconnected: StateFlow<Boolean> = _deviceDisconnected

    // Privados y mutables (solo dentro del repositorio)
    private val _ssid = MutableStateFlow<String>("")
    private val _password = MutableStateFlow<String>("")

    // Públicos e inmutables para observar desde fuera
    val ssid: StateFlow<String> = _ssid.asStateFlow()
    val password: StateFlow<String> = _password.asStateFlow()


    // En el bloque init se registra la clase actual como suscriptora de eventos en EventBus.
// Esto significa que cualquier método anotado con @Subscribe en esta clase recibirá eventos publicados por otros.
    init {
        EventBus.getDefault().register(this)
    }

    /*
     * Esta función está suscrita a los eventos de tipo DeviceConnectionEvent que se publican mediante EventBus.
     * La anotación @Subscribe le indica a EventBus que este método debe ejecutarse cuando ocurra uno de esos eventos.
     *
     * threadMode = ThreadMode.MAIN significa que el método se ejecutará en el hilo principal (UI thread),
     * lo cual es importante porque vamos a actualizar un StateFlow observado por la UI (Compose),
     * y cualquier interacción con la interfaz de usuario debe hacerse en el hilo principal.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceConnectionEvent(event: DeviceConnectionEvent) {

        /*
         * Verificamos si el tipo de evento recibido es igual a EVENT_DEVICE_DISCONNECTED,
         * que es una constante definida en el SDK de Espressif.
         *
         * Este evento es publicado automáticamente por el SDK cuando el ESPDevice se desconecta,
         * ya sea por apagarse, alejarse, o fallar la conexión.
         */
        // El SDK utiliza algo asi cuando hay un evento de desconexion
        //EventBus.getDefault().post(DeviceConnectionEvent(ESPConstants.EVENT_DEVICE_DISCONNECTED))

        // Publica (emite) un evento de desconexión de dispositivo a todos los suscriptores registrados en EventBus.
        // getDefault() obtiene la instancia singleton de EventBus utilizada en toda la aplicación.
        // post(...) envía el evento para que sea recibido por cualquier método anotado con @Subscribe y que escuche DeviceConnectionEvent.
        // DeviceConnectionEvent(...) es el evento que se está enviando, en este caso indicando que el dispositivo se ha desconectado.
        // ESPConstants.EVENT_DEVICE_DISCONNECTED es una constante proporcionada por el SDK de Espressif que representa una desconexión del dispositivo.

        if (event.eventType == ESPConstants.EVENT_DEVICE_DISCONNECTED) {

            /*
             * Actualizamos el valor del StateFlow _deviceDisconnected a 'true'.
             * Esto es una señal para cualquier observador (por ejemplo, una pantalla en Jetpack Compose)
             * de que el dispositivo se ha desconectado.
             *
             * Al ser un StateFlow, este cambio puede ser observado de forma reactiva para mostrar un AlertDialog,
             * redirigir al usuario a otra pantalla, etc.
             */
            _deviceDisconnected.value = true
        }
    }

    fun saveWifiCredentials(ssid: String, password: String) {
        _ssid.value = ssid
        _password.value = password
    }

    fun clearDisconnectedFlag() {
        _deviceDisconnected.value = false
    }

    fun cleanup() {
        EventBus.getDefault().unregister(this)
    }
    fun saveCredentials(username: String, proof: String) {
        _username.value = username // Asigna el nuevo valor al flujo de _username.
        _proof.value = proof       // Asigna el nuevo valor al flujo de _proof.
    }


    // Guarda el objeto ESPDevice
    fun saveEspDevice(device: ESPDevice) {
        _espDevice.value = device

    }

    // Limpiar todos los datos después de provisionar
    fun clear() {
        _espDevice.value = null
        _ssid.value = ""
        _password.value = ""
        _username.value = null
        _proof.value = null
    }

    fun desconectarDispositivo() {
        try {
            _espDevice.value?.disconnectDevice()
            _espDevice.value = null // Limpiamos el estado después de desconectar
        } catch (e: Exception) {
            Log.e("ProvisioningRepo", "Error al desconectar dispositivo: ${e.message}")
        }
    }

    // ----------- FUNCIONES DE PROVISIONING -----------

    // Enviar credenciales Wi-Fi al ESP32
    suspend fun sendWifiCredentials(ssid: String, password: String) {
        val device = _espDevice.value ?: throw IllegalStateException("ESPDevice no disponible")
        // SuspendingCoroutine para esperar el resultado del listener
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
            device.provision(ssid, password, object : ProvisionListener {
                override fun createSessionFailed(e: Exception?) {
                    cont.resumeWith(Result.failure(e ?: Exception("Fallo al crear sesión")))
                }
                override fun wifiConfigSent() {}
                override fun wifiConfigFailed(e: Exception?) {
                    cont.resumeWith(Result.failure(e ?: Exception("Fallo al enviar configuración Wi-Fi")))
                }
                override fun wifiConfigApplied() {}
                override fun wifiConfigApplyFailed(e: Exception?) {
                    cont.resumeWith(Result.failure(e ?: Exception("Fallo al aplicar configuración Wi-Fi")))
                }
                override fun provisioningFailedFromDevice(failureReason: ESPConstants.ProvisionFailureReason?) {
                    cont.resumeWith(Result.failure(Exception("Provisioning falló: $failureReason")))
                }
                override fun deviceProvisioningSuccess() {
                    cont.resumeWith(Result.success(Unit))
                }
                override fun onProvisioningFailed(e: Exception?) {
                    cont.resumeWith(Result.failure(e ?: Exception("Provisioning falló")))
                }
            })
        }
    }

    // Enviar configuración de base de datos (ejemplo)
    suspend fun sendDatabaseConfig() {
        val exampleData = "config_example_db".toByteArray()
        sendCustomData(
            path = "db-config",
            data = exampleData
        )
    }

    // Enviar configuración MQTT

    suspend fun sendMqttConfig() {
        // JSON con las credenciales hardcodeadas
        val jsonObj = org.json.JSONObject().apply {
            put("broker", "ssl://bde98a85f86a454891057738db2eb24c.s1.eu.hivemq.cloud:8883")
            put("username", "dev_Android")
            put("password", "REXvALDO23")
        }

        val jsonString = jsonObj.toString()
        android.util.Log.i("ProvisioningRepo", "Enviando configuración MQTT JSON: $jsonString")

        val jsonData = jsonString.toByteArray(Charsets.UTF_8)

        // Envía el JSON al endpoint "mqtt-config" del ESP32
        sendCustomData(
            path = "mqtt-config",
            data = jsonData
        )
    }


    // Función genérica para enviar datos personalizados al ESP32
    private suspend fun sendCustomData(path: String, data: ByteArray) {
        val device = _espDevice.value ?: throw IllegalStateException("ESPDevice no disponible")
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
            device.sendDataToCustomEndPoint(path, data, object : ResponseListener {
                override fun onSuccess(returnData: ByteArray?) {
                    if (returnData != null) {
                        val respStr = String(returnData, charset = Charsets.UTF_8)
                        Log.i("ProvisioningRepo", "Respuesta recibida de $path: $respStr")
                    } else {
                        Log.i("ProvisioningRepo", "Respuesta vacía recibida de $path")
                    }
                    cont.resumeWith(Result.success(Unit))
                }
                override fun onFailure(e: Exception?) {
                    cont.resumeWith(Result.failure(e ?: Exception("Fallo al enviar datos a $path")))
                }
            })
        }
    }



}
