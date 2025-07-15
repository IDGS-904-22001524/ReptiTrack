// com.waldoz_x.reptitrack.data.source.remote/HiveMqttClient.kt
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

    // URL y puerto de tu clúster HiveMQ
    private val BROKER_URI = "ssl://bde98a85f86a454891057738db2eb24c.s1.eu.hivemq.cloud:8883"

    // ¡Credenciales predeterminadas! Estas se usarán si no se proporcionan credenciales
    // al llamar a la función connect().
    // Cambiado a 'internal' para que MqttSettingsViewModel pueda acceder a ellas
    internal val DEFAULT_USERNAME = "dev_Android"
    internal val DEFAULT_PASSWORD = "REXvALDO23" // Ahora accesible como String

    private lateinit var mqttClient: MqttClient

    // Estado de conexión MQTT
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    // Flow para emitir mensajes recibidos (topic, payload)
    private val _receivedMessages = MutableStateFlow<Pair<String, String>?>(null)
    val receivedMessages: StateFlow<Pair<String, String>?> = _receivedMessages


    init {
        try {
            val clientId = UUID.randomUUID().toString()
            mqttClient = MqttClient(BROKER_URI, clientId, MemoryPersistence())

            // Configura el callback para manejar eventos de conexión y mensajes
            mqttClient.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String) {
                    Log.d(TAG, "Cliente MQTT conectado: $serverURI (reconnect: $reconnect)")
                    _isConnected.value = true
                    // Puedes suscribirte a tópicos aquí automáticamente después de reconectar
                    // Por ejemplo:
                    // subscribeToTopic("sensors/+/data")
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "Conexión MQTT perdida: ${cause?.message}", cause)
                    _isConnected.value = false
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
        } catch (e: MqttException) {
            Log.e(TAG, "Error al inicializar MqttClient: ${e.message}", e)
        }
    }

    // Función para conectar al broker MQTT
    // Ahora acepta username y password como parámetros opcionales.
    // Si no se proporcionan, usará las credenciales predeterminadas.
    suspend fun connect(username: String? = null, password: String? = null) {
        if (mqttClient.isConnected) {
            Log.d(TAG, "Cliente ya está conectado.")
            return
        }
        try {
            val options = MqttConnectOptions().apply {
                isCleanSession = true // Iniciar una nueva sesión limpia
                connectionTimeout = 30 // Segundos
                keepAliveInterval = 30 // Segundos

                // Determina las credenciales a usar: las proporcionadas o las predeterminadas
                val finalUsername = username ?: DEFAULT_USERNAME
                val finalPassword = password ?: DEFAULT_PASSWORD

                // Asigna las credenciales solo si no están vacías
                if (finalUsername.isNotBlank()) {
                    this.userName = finalUsername
                }
                if (finalPassword.isNotBlank()) {
                    this.password = finalPassword.toCharArray()
                }

                // Configuración SSL/TLS (para puerto 8883)
                // Paho maneja SSL por defecto si el URI es "ssl://"
                // Si necesitas configurar un TrustStore/KeyStore específico, se haría aquí.
            }

            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    mqttClient.connect(options) // Llamada síncrona
                    continuation.resume(Unit) // Reanuda la coroutine si la conexión es exitosa
                } catch (e: MqttException) {
                    continuation.resumeWithException(e) // Reanuda con excepción si falla
                }
            }
            Log.d(TAG, "Conexión MQTT exitosa!")
            _isConnected.value = true // Actualiza el estado a conectado
        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar al broker MQTT: ${e.message}", e)
            _isConnected.value = false // Asegura que el estado sea desconectado
            // No se hace 'throw e' aquí para evitar que la aplicación crashee.
        }
    }

    // Función para desconectar del broker MQTT
    suspend fun disconnect() {
        if (!mqttClient.isConnected) {
            Log.d(TAG, "Cliente ya está desconectado.")
            return
        }
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    mqttClient.disconnect() // Llamada síncrona
                    continuation.resume(Unit)
                } catch (e: MqttException) {
                    continuation.resumeWithException(e)
                }
            }
            Log.d(TAG, "Desconexión MQTT exitosa!")
            _isConnected.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error al desconectar del broker MQTT: ${e.message}", e)
        }
    }

    // Función para suscribirse a un tópico
    suspend fun subscribeToTopic(topic: String, qos: Int = 1) { // QoS 1 = AT_LEAST_ONCE
        if (!mqttClient.isConnected) {
            Log.e(TAG, "No se puede suscribir: Cliente no conectado.")
            return
        }
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    mqttClient.subscribe(topic, qos) // Llamada síncrona
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

    // Función para anular la suscripción a un tópico
    suspend fun unsubscribeFromTopic(topic: String) {
        if (!mqttClient.isConnected) {
            Log.e(TAG, "No se puede anular la suscripción: Cliente no conectado.")
            return
        }
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    mqttClient.unsubscribe(topic) // Llamada síncrona
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

    // Función para publicar un mensaje en un tópico
    suspend fun publishMessage(topic: String, message: String, qos: Int = 1, retained: Boolean = false) {
        if (!mqttClient.isConnected) {
            Log.e(TAG, "No se puede publicar: Cliente no conectado.")
            return
        }
        try {
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                this.qos = qos
                this.isRetained = retained
            }
            suspendCancellableCoroutine<Unit> { continuation ->
                try {
                    mqttClient.publish(topic, mqttMessage) // Llamada síncrona
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
