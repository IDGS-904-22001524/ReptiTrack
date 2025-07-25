package com.waldoz_x.reptitrack.data.source.remote

import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Esta clase manejará la conexión y comunicación con el broker MQTT
@Singleton
class HiveMqttClient @Inject constructor() {

    private val TAG = "HiveMqttClient"

    private val BROKER_URI = "ssl://bda98a85f86a454891057738db2eb24c.s1.eu.hivemq.cloud:8883"
    internal val DEFAULT_USERNAME = "dev_Android"
    internal val DEFAULT_PASSWORD = "REXvALDO23"

    // Se inicializa como 'lateinit var' pero la inicialización real de MqttClient se moverá a connect()
    // para evitar hacer trabajo pesado en el constructor o el bloque init.
    private var mqttClient: MqttClient? = null // Hacerlo nullable para inicialización tardía

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _receivedMessages = MutableStateFlow<Pair<String, String>?>(null)
    val receivedMessages: StateFlow<Pair<String, String>?> = _receivedMessages

    // No hay bloque 'init' que haga trabajo pesado. La configuración se hace al conectar.

    /**
     * Inicializa el cliente MQTT si no ha sido inicializado y luego intenta conectar.
     * Se puede llamar varias veces; solo inicializará el cliente una vez.
     *
     * @param username Nombre de usuario opcional para la conexión MQTT.
     * @param password Contraseña opcional para la conexión MQTT.
     */
    suspend fun connect(username: String? = null, password: String? = null) {
        if (_isConnected.value) {
            Log.d(TAG, "Cliente ya está conectado.")
            return
        }

        // Inicializa el MqttClient solo si es nulo (primera vez o después de un reinicio)
        if (mqttClient == null) {
            try {
                val clientId = UUID.randomUUID().toString()
                mqttClient = MqttClient(BROKER_URI, clientId, MemoryPersistence())
                setupMqttCallback() // Configura el callback una vez
                Log.d(TAG, "MqttClient inicializado.")
            } catch (e: MqttException) {
                Log.e(TAG, "Error al inicializar MqttClient: ${e.message}", e)
                _isConnected.value = false
                return // No podemos continuar sin un cliente inicializado
            }
        }

        try {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 30
                keepAliveInterval = 30

                val finalUsername = username ?: DEFAULT_USERNAME
                val finalPassword = password ?: DEFAULT_PASSWORD

                if (finalUsername.isNotBlank()) {
                    this.userName = finalUsername
                }
                if (finalPassword.isNotBlank()) {
                    this.password = finalPassword.toCharArray()
                }
            }

            // Asegurarse de que el cliente no sea nulo antes de usarlo
            val client = mqttClient ?: throw IllegalStateException("MqttClient no inicializado.")

            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    client.connect(options)
                    continuation.resume(Unit)
                } catch (e: MqttException) {
                    continuation.resumeWithException(e)
                }
            }
            Log.d(TAG, "Conexión MQTT exitosa!")
            _isConnected.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar al broker MQTT: ${e.message}", e)
            _isConnected.value = false
        }
    }

    /**
     * Configura el MqttCallback para el cliente. Se llama solo una vez cuando el cliente es inicializado.
     */
    private fun setupMqttCallback() {
        mqttClient?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                Log.d(TAG, "Cliente MQTT conectado: $serverURI (reconnect: $reconnect)")
                _isConnected.value = true
            }

            override fun connectionLost(cause: Throwable?) {
                Log.e(TAG, "Conexión MQTT perdida: ${cause?.message}", cause)
                _isConnected.value = false
                // Opcional: Reintentar la conexión automáticamente aquí o notificar a la UI para que reintente.
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val payload = String(message.payload)
                Log.d(TAG, "Mensaje recibido en $topic: $payload")
                _receivedMessages.value = Pair(topic, payload)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.d(TAG, "Entrega de mensaje completa: ${token.messageId}")
            }
        })
    }

    suspend fun disconnect() {
        if (!_isConnected.value || mqttClient == null) {
            Log.d(TAG, "Cliente ya está desconectado o no inicializado.")
            return
        }
        try {
            // Asegurarse de que el cliente no sea nulo antes de usarlo
            val client = mqttClient ?: return

            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    client.disconnect()
                    continuation.resume(Unit)
                } catch (e: MqttException) {
                    continuation.resumeWithException(e)
                }
            }
            Log.d(TAG, "Desconexión MQTT exitosa!")
            _isConnected.value = false
            mqttClient = null // Libera el cliente para permitir una nueva inicialización si es necesario
        } catch (e: Exception) {
            Log.e(TAG, "Error al desconectar del broker MQTT: ${e.message}", e)
        }
    }

    suspend fun subscribeToTopic(topic: String, qos: Int = 1) {
        if (!_isConnected.value || mqttClient == null) {
            Log.e(TAG, "No se puede suscribir: Cliente no conectado o no inicializado.")
            return
        }
        try {
            val client = mqttClient ?: return
            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    client.subscribe(topic, qos)
                    continuation.resume(Unit)
                } catch (e: MqttException) {
                    continuation.resumeWithException(e)
                }
            }
            Log.d(TAG, "Suscrito exitosamente al tópico: $topic, QoS: $qos")
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al suscribirse al tópico $topic: ${e.message}", e)
        }
    }

    suspend fun unsubscribeFromTopic(topic: String) {
        if (!_isConnected.value || mqttClient == null) {
            Log.e(TAG, "No se puede anular la suscripción: Cliente no conectado o no inicializado.")
            return
        }
        try {
            val client = mqttClient ?: return
            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    client.unsubscribe(topic)
                    continuation.resume(Unit)
                } catch (e: MqttException) {
                    continuation.resumeWithException(e)
                }
            }
            Log.d(TAG, "Anulada la suscripción al tópico: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Error al anular la suscripción al tópico $topic: ${e.message}", e)
        }
    }

    suspend fun publishMessage(topic: String, message: String, qos: Int = 1, retained: Boolean = false) {
        if (!_isConnected.value || mqttClient == null) {
            Log.e(TAG, "No se puede publicar: Cliente no conectado o no inicializado.")
            return
        }
        try {
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                this.qos = qos
                this.isRetained = retained
            }
            val client = mqttClient ?: return
            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    client.publish(topic, mqttMessage)
                    continuation.resume(Unit)
                } catch (e: MqttException) {
                    continuation.resumeWithException(e)
                }
            }
            Log.d(TAG, "Mensaje publicado en $topic: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Error al publicar mensaje en $topic: ${e.message}", e)
        }
    }
}