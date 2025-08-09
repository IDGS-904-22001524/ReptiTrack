package com.waldoz_x.reptitrack.ui.screens.terrariumdetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.source.remote.HiveMqttClient
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.domain.repository.TerrariumRepository
import com.waldoz_x.reptitrack.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers // Importar Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Importar withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

import javax.inject.Inject

@HiltViewModel
class TerrariumDetailViewModel @Inject constructor(
    private val terrariumRepository: TerrariumRepository,
    private val hiveMqttClient: HiveMqttClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "TerrariumDetailViewModel"

    private val _terrariumState = MutableStateFlow<Terrarium?>(null)
    val terrariumState: StateFlow<Terrarium?> = _terrariumState.asStateFlow()

    private val _sensorData = MutableStateFlow<Map<String, String>>(emptyMap())
    val sensorData: StateFlow<Map<String, String>> = _sensorData.asStateFlow()

    private val _actuatorStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val actuatorStates: StateFlow<Map<String, Boolean>> = _actuatorStates.asStateFlow()

    val isMqttConnected: StateFlow<Boolean> = hiveMqttClient.isConnected

    private val terrariumId: String = savedStateHandle.get<String>(Destinations.TERRARIUM_ID_ARG)
        ?: throw IllegalStateException("Terrarium ID no encontrado en los argumentos de navegación.")

    // Usa el userId correcto para que coincida con los topics MQTT reales
    private val userId: String = "Ipzro9ETmRX9moHzQ0QNXv06SBy1"

    init {
        Log.d(TAG, "ViewModel inicializado para Terrario ID: $terrariumId")
        startSimulatedSensorUpdates() // <-- Siempre simular datos
        loadTerrariumData()
        observeMqttMessages()
        observeMqttConnection()
    }

    // Simula y actualiza datos de sensores cada 2 segundos (excepto pzem)
    private fun startSimulatedSensorUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                // DHT: temperatura 22.0 - 24.3, humedad 55.0 - 62.0
                fun tempDht() = String.format("%.2f°C", Random.nextDouble(22.0, 24.3))
                fun humDht() = String.format("%.2f%%", Random.nextDouble(55.0, 62.0))
                // DS18B20: temperatura 24.5 - 25.7
                fun tempDsb() = String.format("%.2f°C", Random.nextDouble(24.5, 25.7))
                val current = _sensorData.value.toMutableMap()
                // DHT11
                current["dht11_1_humidity"] = humDht()
                current["dht11_1_temperature"] = tempDht()
                // DHT22
                current["dht22_1_humidity"] = humDht()
                current["dht22_1_temperature"] = tempDht()
                current["dht22_2_humidity"] = humDht()
                current["dht22_2_temperature"] = tempDht()
                current["dht22_3_humidity"] = humDht()
                current["dht22_3_temperature"] = tempDht()
                current["dht22_4_humidity"] = humDht()
                current["dht22_4_temperature"] = tempDht()
                // DS18B20
                current["ds18b20_1_temperature"] = tempDsb()
                current["ds18b20_2_temperature"] = tempDsb()
                current["ds18b20_3_temperature"] = tempDsb()
                current["ds18b20_4_temperature"] = tempDsb()
                current["ds18b20_5_temperature"] = tempDsb()
                // Última actualización
                current["lastUpdated"] = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) + " (Simulado)"
                // pzem_1_power NO se toca aquí, solo por MQTT real
                _sensorData.value = current
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    private fun loadTerrariumData() {
        viewModelScope.launch(Dispatchers.IO) {
            terrariumRepository.getTerrariumById(
                userId = userId,
                id = terrariumId
            ).collect { terrarium ->
                _terrariumState.value = terrarium
                terrarium?.let {
                    _actuatorStates.value = mapOf(
                        "water_pump_active" to it.waterPumpActive,
                        "fan1_active" to it.fan1Active,
                        "fan2_active" to it.fan2Active,
                        "light1_active" to it.light1Active,
                        "light2_active" to it.light2Active,
                        "light3_active" to it.light3Active,
                        "heat_plate1_active" to it.heatPlate1Active
                    )

                    val currentSensorData = mutableMapOf<String, String>()

                    it.dht22_1_temperature?.let { temp -> currentSensorData["dht22_1_temperature"] = "${temp}°C" }
                    it.dht22_1_humidity?.let { hum -> currentSensorData["dht22_1_humidity"] = "${hum}%" }
                    it.dht22_2_temperature?.let { temp -> currentSensorData["dht22_2_temperature"] = "${temp}°C" }
                    it.dht22_2_humidity?.let { hum -> currentSensorData["dht22_2_humidity"] = "${hum}%" }
                    it.dht22_3_temperature?.let { temp -> currentSensorData["dht22_3_temperature"] = "${temp}°C" }
                    it.dht22_3_humidity?.let { hum -> currentSensorData["dht22_3_humidity"] = "${hum}%" }
                    it.dht22_4_temperature?.let { temp -> currentSensorData["dht22_4_temperature"] = "${temp}°C" }
                    it.dht22_4_humidity?.let { hum -> currentSensorData["dht22_4_humidity"] = "${hum}%" }

                    it.ds18b20_1_temperature?.let { temp -> currentSensorData["ds18b20_1_temperature"] = "${temp}°C" }
                    it.ds18b20_2_temperature?.let { temp -> currentSensorData["ds18b20_2_temperature"] = "${temp}°C" }
                    it.ds18b20_3_temperature?.let { temp -> currentSensorData["ds18b20_3_temperature"] = "${temp}°C" }
                    it.ds18b20_4_temperature?.let { temp -> currentSensorData["ds18b20_4_temperature"] = "${temp}°C" }
                    it.ds18b20_5_temperature?.let { temp -> currentSensorData["ds18b20_5_temperature"] = "${temp}°C" }

                    it.hc_sr04_1_distance?.let { dist -> currentSensorData["hc_sr04_1_distance"] = "${dist} cm" }
                    it.pzem_1_power?.let { power -> currentSensorData["pzem_1_power"] = "${power} W" }

                    currentSensorData["lastUpdated"] = it.lastUpdated.let { timestamp ->
                        if (timestamp != null && timestamp > 0) {
                            val date = Date(timestamp)
                            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            sdf.format(date) + " (hace " + formatTimeAgo(timestamp) + ")"
                        } else {
                            "N/A"
                        }
                    }

                    _sensorData.value = currentSensorData
                }
            }
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "$seconds s"
            minutes < 60 -> "$minutes min"
            hours < 24 -> "$hours h"
            else -> "$days d"
        }
    }

    private fun observeMqttMessages() {
        viewModelScope.launch {
            combine(hiveMqttClient.isConnected, hiveMqttClient.receivedMessages) { isConnected, messagePair ->
                if (isConnected && messagePair != null) {
                    messagePair
                } else {
                    null
                }
            }.collect { messagePair ->
                messagePair?.let { (topic, payload) ->
                    Log.d(TAG, "Mensaje MQTT recibido en ViewModel: $topic -> $payload")
                    // El procesamiento del mensaje puede ser ligero, pero si se vuelve complejo,
                    // considera moverlo a un Dispatchers.Default o Dispatchers.IO
                    processMqttMessage(topic, payload)
                }
            }
        }
    }

    private fun observeMqttConnection() {
        viewModelScope.launch {
            hiveMqttClient.isConnected.collect { isConnected ->
                if (isConnected) {
                    Log.d(TAG, "Conexión MQTT establecida, suscribiendo a tópicos del usuario: $userId")
                    withContext(Dispatchers.IO) {
                        // Suscríbete al topic real de tus sensores
                        hiveMqttClient.subscribeToTopic("reptritrack/$userId/esp02/sensores/#")
                    }
                } else {
                    Log.d(TAG, "Conexión MQTT perdida para usuario: $userId")
                }
            }
        }
    }

    private fun processMqttMessage(topic: String, payload: String) {
        // Solo para el sensor de potencia, procesa únicamente la potencia
        if (topic == "reptritrack/D85QPSadZhd8pXC0dpBwEUGD5gR2/esp01/sensores/pzem004") {
            try {
                val map = payload.trim()
                    .removePrefix("{").removeSuffix("}")
                    .split(",")
                    .map { it.split(":") }
                    .associate { it[0].replace("\"", "").trim() to it[1].replace("\"", "").trim() }

                val updated = _sensorData.value.toMutableMap()
                map["potencia"]?.let { potencia ->
                    updated["pzem_1_power"] = "$potencia W"
                }
                updated["lastUpdated"] = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) + " (hace " + formatTimeAgo(System.currentTimeMillis()) + ")"
                _sensorData.value = updated
                Log.d(TAG, "Sensor pzem004 actualizado solo con potencia: $payload")
            } catch (e: Exception) {
                Log.e(TAG, "Error al parsear JSON de pzem004: $payload", e)
            }
            return
        }

        // Para todos los demás sensores, deja el comportamiento tal cual
        val parts = topic.split("/")
        if (parts.size >= 5 && parts[0] == "reptritrack" && parts[2] == "esp02" && parts[3] == "sensores") {
            val sensorKeyRaw = parts[4]
            val sensorKeyNormalized = sensorKeyRaw
                .replace(Regex("_(0*)([1-9][0-9]*)$"), "_$2")

            if (payload.trim().startsWith("{") && payload.trim().endsWith("}")) {
                try {
                    val map = payload.trim()
                        .removePrefix("{").removeSuffix("}")
                        .split(",")
                        .map { it.split(":") }
                        .associate { it[0].replace("\"", "").trim() to it[1].replace("\"", "").trim() }

                    val updated = _sensorData.value.toMutableMap()
                    map.forEach { (k, v) ->
                        val key = when (k.lowercase()) {
                            "temperature", "temperatura" -> "${sensorKeyNormalized}_temperature"
                            "humidity", "humedad" -> "${sensorKeyNormalized}_humidity"
                            "distance", "distancia" -> "${sensorKeyNormalized}_distance"
                            "power", "potencia" -> "${sensorKeyNormalized}_power"
                            else -> "${sensorKeyNormalized}_$k"
                        }
                        val value = when (k.lowercase()) {
                            "temperature", "temperatura" -> "$v°C"
                            "humidity", "humedad" -> "$v%"
                            "distance", "distancia" -> "$v cm"
                            "power", "potencia" -> "$v W"
                            else -> v
                        }
                        updated[key] = value
                    }
                    updated["lastUpdated"] = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) + " (hace " + formatTimeAgo(System.currentTimeMillis()) + ")"
                    _sensorData.value = updated
                    Log.d(TAG, "Sensor $sensorKeyNormalized actualizado con JSON: $payload")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al parsear JSON de sensor: $payload", e)
                }
            } else {
                val updated = _sensorData.value.toMutableMap()
                when {
                    sensorKeyNormalized.startsWith("dht22_") -> {
                        if (payload.contains("%")) {
                            updated["${sensorKeyNormalized}_humidity"] = payload
                        } else if (payload.contains("°C")) {
                            updated["${sensorKeyNormalized}_temperature"] = payload
                        } else {
                            if (payload.toFloatOrNull() != null && payload.toFloat() < 100) {
                                updated["${sensorKeyNormalized}_temperature"] = "$payload°C"
                            } else {
                                updated["${sensorKeyNormalized}_humidity"] = "$payload%"
                            }
                        }
                    }
                    sensorKeyNormalized.startsWith("ds18b20_") -> {
                        updated["${sensorKeyNormalized}_temperature"] = payload
                    }
                }
                updated["lastUpdated"] = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) + " (hace " + formatTimeAgo(System.currentTimeMillis()) + ")"
                _sensorData.value = updated
                Log.d(TAG, "Sensor $sensorKeyNormalized actualizado con valor simple: $payload")
            }
        }
    }

    // Agrega esta función al final de la clase
    fun toggleActuator(terrariumId: String, actuatorKey: String, newState: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // Aquí deberías implementar la lógica para cambiar el estado del actuador en tu base de datos o enviar un comando MQTT.
            // Por ahora, solo actualiza el estado localmente para evitar errores y permitir la interacción en la UI.
            val updated = _actuatorStates.value.toMutableMap()
            updated[actuatorKey] = newState
            _actuatorStates.value = updated

            // Si tienes lógica real para enviar el cambio al hardware, agrégala aquí.
            // Por ejemplo:
            // hiveMqttClient.publishActuatorCommand(terrariumId, actuatorKey, newState)
        }
    }
}
