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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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

    // Flow para exponer el último error ocurrido en sendWifiCredentials (o null si no hay error)
    private val _provisioningError = MutableStateFlow<String?>(null)
    val provisioningError: StateFlow<String?> = _provisioningError.asStateFlow()

    // Limpiar error (útil para la UI cuando se muestra el error y luego se quiere resetear)
    fun clearProvisioningError() {
        _provisioningError.value = null
    }


    // true si ya se completó el aprovisionamiento del primer ESP32
    private val _checkpointReached = MutableStateFlow(false)
    val checkpointReached: StateFlow<Boolean> = _checkpointReached.asStateFlow()

    private val _provisioningRound = MutableStateFlow(1)
    val provisioningRound: StateFlow<Int> = _provisioningRound.asStateFlow()

    fun markCheckpointReached() {
        _checkpointReached.value = true
    }

    fun resetCheckpoint() {
        _checkpointReached.value = false
    }

    // Método para incrementar la ronda
    fun incrementProvisioningRound() {
        _provisioningRound.value = _provisioningRound.value + 1
    }

    // Método para resetear, si quieres
    fun resetProvisioningRound() {
        _provisioningRound.value = 1
    }


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

            /* * Actualizamos el valor del StateFlow _deviceDisconnected a 'true'.
                * Esto es una señal para cualquier observador (por ejemplo, una pantalla en Jetpack Compose)
                * de que el dispositivo se ha desconectado.
                  * Al ser un StateFlow, este cambio puede ser observado de forma reactiva para mostrar un AlertDialog,
                   * redirigir al usuario a otra pantalla, etc. */

                    _deviceDisconnected.value = true
                    Log.e("ProvisioningRepo", "Se ha desconectado el dispositivo")

        }
    }

    fun saveWifiCredentials(ssid: String, password: String) {
        _ssid.value = ssid.trim()
        _password.value = password.trim()
    }

    fun clearDisconnectedFlag() {
        _deviceDisconnected.value = false
    }

    fun cleanup() {
        EventBus.getDefault().unregister(this)
    }
    fun saveCredentials(username: String, proof: String) {
        _username.value = username.trim() // Asigna el nuevo valor al flujo de _username.
        _proof.value = proof.trim()      // Asigna el nuevo valor al flujo de _proof.
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
            Log.e("ProvisioningRepo", "Dispositivo desconectado manualmente")
        } catch (e: Exception) {
            Log.e("ProvisioningRepo", "Error al desconectar dispositivo: ${e.message}")
        }
    }

    // ----------- FUNCIONES DE PROVISIONING -----------
// Función suspendida que envía credenciales Wi-Fi al dispositivo ESP32
// Está suspendida para poder usarla dentro de una coroutine y esperar el resultado asincrónico
    suspend fun sendWifiCredentials(ssid: String, password: String) {

        // Obtiene el dispositivo actual desde un StateFlow o similar
        // Si no existe, lanza una excepción porque no se puede continuar sin el dispositivo
        val device = _espDevice.value ?: throw IllegalStateException("ESPDevice no disponible")

        // Inicia una coroutine cancelable, lo que permite suspender la ejecució
        // hasta que se reciba un callback de éxito o error del provisioning
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->

            // Se llama a provision(), que es el método de la librería ESPProvisionManager
            // Este método envía las credenciales (SSID y contraseña) al dispositivo ESP32
            device.provision(ssid, password, object : ProvisionListener {

                // Se ejecuta si falla la creación de la sesión segura (por ejemplo, falló el handshake BLE).
                override fun createSessionFailed(e: Exception?) {
                    val errorMessage = e?.message ?: "Fallo al crear sesión"
                    _provisioningError.value = errorMessage // Guarda solo el mensaje de error

                // Reanuda la ejecución de la coroutine que estaba suspendida,
                    // 'cont' es una continuación (Continuation), usada para reanudar una función suspendida.
                    // Aquí se está indicando que la operación de aprovisionamiento falló,
                    // por lo que se crea una excepción con el mensaje de error ('errorMessage').
                    // Luego se envuelve esa excepción en un Result.failure(...) para representar el fallo.
                    // Finalmente, 'cont.resumeWith(...)' reanuda la coroutine enviando ese fallo,
                    // lo que provoca que la función suspendida termine con una excepción que puede capturarse
                    // usando try/catch o propagarse hacia arriba.
                    cont.resumeWith(Result.failure(Exception(errorMessage)))
                }

                // Se llama cuando las credenciales Wi-Fi (SSID y password) han sido enviadas con éxito.
                override fun wifiConfigSent() {
                    // Solo notifica que fueron enviadas, pero aún no aplicadas.
                    // Aquí es CUANDO SE ENVIAN LAS CREDENCIALES al ESP32 vía BLE.
                }

                // Se ejecuta si ocurrió un error al enviar las credenciales Wi-Fi.
                override fun wifiConfigFailed(e: Exception?) {
                    val errorMessage = e?.message ?: "Fallo al enviar configuración Wi-Fi"
                    _provisioningError.value = errorMessage
                    cont.resumeWith(Result.failure(Exception(errorMessage)))
                }

                // Se llama cuando el dispositivo recibió y aplicó la configuración Wi-Fi correctamente,
                // pero aún no se ha confirmado si logró conectarse a la red.
                override fun wifiConfigApplied() {
                    // Opcional: podrías hacer un log aquí o usarlo para mostrar progreso
                }

                // Se llama si hubo un error al aplicar la configuración Wi-Fi (por ejemplo, el ESP32 no pudo usar las credenciales).
                override fun wifiConfigApplyFailed(e: Exception?) {
                    val errorMessage = e?.message ?: "Fallo al aplicar configuración Wi-Fi"
                    _provisioningError.value = errorMessage
                    cont.resumeWith(Result.failure(Exception(errorMessage)))
                }

                // Se llama cuando el dispositivo ESP32 responde que falló la conexión Wi-Fi (por ejemplo, clave incorrecta).
                override fun provisioningFailedFromDevice(failureReason: ESPConstants.ProvisionFailureReason?) {
                    val errorMessage = when (failureReason) {
                        ESPConstants.ProvisionFailureReason.AUTH_FAILED -> "autenticación fallida"
                        ESPConstants.ProvisionFailureReason.NETWORK_NOT_FOUND -> "red no encontrada"
                        ESPConstants.ProvisionFailureReason.DEVICE_DISCONNECTED -> "dispositivo desconectado"
                        ESPConstants.ProvisionFailureReason.UNKNOWN, null -> "desconocido"
                    }

                    _provisioningError.value = "Error en la configuración: $errorMessage"
                    cont.resumeWith(Result.failure(Exception(errorMessage)))
                }


                // Se llama si todo el proceso fue exitoso:
                // 1. Sesión creada
                // 2. Credenciales enviadas
                // 3. Credenciales aplicadas
                // 4. Dispositivo conectado exitosamente a la red Wi-Fi
                override fun deviceProvisioningSuccess() {
                    _provisioningError.value = "" // Limpia cualquier error anterior (cadena vacía)
                    /*
                     * Reanuda la ejecución de la coroutine que estaba suspendida.
                     * 'cont' es una Continuation, un objeto que representa el punto en que se suspendió la función.
                     * Aquí se está indicando que la operación de aprovisionamiento fue exitosa.

                      * Se utiliza Result.success(Unit) para envolver el resultado exitoso de la operación,
                      * donde 'Unit' representa que no hay un valor específico que devolver, solo que se completó correctamente.

                      * Finalmente, cont.resumeWith(...) reanuda la coroutine con ese resultado exitoso,
                      * lo que permite que la función suspendida continúe normalmente sin lanzar ninguna excepción.
                      */
                    cont.resumeWith(Result.success(Unit))                }

                // Se llama si ocurre cualquier otro error general durante el proceso de provisioning.
                override fun onProvisioningFailed(e: Exception?) {
                    val errorMessage = e?.message ?: "Error en la configuración"
                    _provisioningError.value = errorMessage
                    cont.resumeWith(Result.failure(Exception(errorMessage)))
                }
            })
        }
    }


    // Enviar configuración de base de datos (ejemplo)
    suspend fun sendDatabaseConfig(userId: String) {
        // Obtenemos la ronda actual (por defecto empieza en 1)
        val round = _provisioningRound.value

        // Generamos un ID único para el ESP32 basado en la ronda
        val espId = "esp0$round"

        val jsonObj = JSONObject().apply {
            put("userId", userId)
            put("espId", espId)
            // Puedes agregar más parámetros según necesites
        }

        val data = jsonObj.toString().toByteArray(Charsets.UTF_8)

        sendCustomData(
            path = "db-config",
            data = data
        )
    }
 

    // Enviar configuración MQTT

    suspend fun sendMqttConfig() {
        // JSON con las credenciales hardcodeadas
        val jsonObj = org.json.JSONObject().apply {
            put("broker", "mqtts://bda98a85f86a454891057738db2eb24c.s1.eu.hivemq.cloud:8883")
            put("username", "dev_Android")
            put("password", "REXvALDO23")

        }

        val jsonString = jsonObj.toString()
        Log.i("ProvisioningRepo", "Enviando configuración MQTT JSON: $jsonString")

        val jsonData = jsonString.toByteArray(Charsets.UTF_8)

        // Envía el JSON al endpoint "mqtt-config" del ESP32
        sendCustomData(
            path = "mqtt-config",
            data = jsonData
        )
    }

    // Verifica si el dispositivo responde al endpoint "ping-conn"
    suspend fun isDeviceReallyConnected(): Boolean {
        Log.d("ProvisioningRepo", "Llamando a isDeviceReallyConnected()")
        val device = _espDevice.value ?: return false

        return try {
            withTimeoutOrNull(3000) { // Espera máximo 1 segundo
                kotlinx.coroutines.suspendCancellableCoroutine<Boolean> { cont ->
                    device.sendDataToCustomEndPoint("ping-conn", ByteArray(0), object : ResponseListener {
                        override fun onSuccess(returnData: ByteArray?) {
                            cont.resume(true, null)
                        }

                        override fun onFailure(e: Exception?) {
                            cont.resume(false, null)
                        }
                    })
                }
            } ?: false // Si pasa el tiempo sin respuesta, asumimos que está desconectado
        } catch (e: Exception) {
            Log.e("ProvisioningRepo", "ping-conn fallo: ${e.message}")
            false
        }
    }

    // Función genérica para enviar datos personalizados al ESP32
    private suspend fun sendCustomData(path: String, data: ByteArray) {
        val device = _espDevice.value ?: throw IllegalStateException("ESPDevice no disponible")
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
            //se envia aqui los datos
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
