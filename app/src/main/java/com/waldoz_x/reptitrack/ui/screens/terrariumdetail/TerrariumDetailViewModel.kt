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

    // Agrega una propiedad para el userId si lo necesitas (ajusta según tu lógica)
    private val userId: String = savedStateHandle.get<String>("userId")
        ?: "default_user_id" // Cambia esto por la forma correcta de obtener el userId

    init {
        Log.d(TAG, "ViewModel inicializado para Terrario ID: $terrariumId")
        loadTerrariumData()
        observeMqttMessages()
        observeMqttConnection()
    }

    private fun loadTerrariumData() {
        viewModelScope.launch(Dispatchers.IO) {
            // CORREGIDO: Pasa el parámetro 'userId' si la función lo requiere
            terrariumRepository.getTerrariumById(
                userId = userId,
                id = terrariumId
            ).collect { terrarium ->
                // El procesamiento de los datos del terrario es ligero, puede ser en Main o IO
                // Si el terrarium es muy grande y el mapeo es costoso, considera withContext(Dispatchers.Default)
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
                    Log.d(TAG, "Conexión MQTT establecida, suscribiendo a tópicos del terrario: $terrariumId")
                    // Las suscripciones MQTT son operaciones de red/I/O, por lo que deben ir en Dispatchers.IO
                    withContext(Dispatchers.IO) {
                        hiveMqttClient.subscribeToTopic("terrarium/$terrariumId/sensors/#")
                        hiveMqttClient.subscribeToTopic("terrarium/$terrariumId/actuators/feedback/#")
                    }
                } else {
                    Log.d(TAG, "Conexión MQTT perdida para terrario: $terrariumId")
                }
            }
        }
    }

    private fun processMqttMessage(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 4 && parts[0] == "terrarium" && parts[1] == terrariumId) {
            when (parts[2]) {
                "sensors" -> {
                    if (parts.size >= 5) {
                        val sensorType = parts[3]
                        val sensorValueType = parts[4]
                        val sensorKey = "${sensorType}_${sensorValueType}"

                        _sensorData.value = _sensorData.value.toMutableMap().apply {
                            this[sensorKey] = payload
                            this["lastUpdated"] = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) + " (hace " + formatTimeAgo(System.currentTimeMillis()) + ")"
                        }
                        Log.d(TAG, "Sensor $sensorKey actualizado a $payload")
                    }
                }
                "actuators" -> {
                    if (parts.size >= 5 && parts[3] == "feedback") {
                        val actuatorName = parts[4]
                        val isActive = payload.uppercase(Locale.getDefault()) == "ON"

                        _actuatorStates.value = _actuatorStates.value.toMutableMap().apply {
                            this["${actuatorName}_active"] = isActive
                        }
                        Log.d(TAG, "Actuador ${actuatorName} estado: $isActive")
                    }
                }
            }
        }
    }

    fun toggleActuator(terrariumId: String, actuatorKey: String, newState: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val actuatorName = actuatorKey.replace("_active", "")
            val payload = if (newState) "ON" else "OFF"
            val topic = "terrarium/$terrariumId/actuators/$actuatorName/set"

            Log.d(TAG, "Publicando comando MQTT: $topic -> $payload")
            hiveMqttClient.publishMessage(topic, payload)

            withContext(Dispatchers.Main) {
                _actuatorStates.value = _actuatorStates.value.toMutableMap().apply {
                    this[actuatorKey] = newState
                }
            }

            // CORREGIDO: Pasa el parámetro 'userId' correctamente
            terrariumRepository.updateTerrariumActuatorState(
                userId = userId,
                terrariumId = terrariumId,
                actuatorKey = actuatorKey,
                newState = newState
            )
        }
    }
}
