package com.waldoz_x.reptitrack.data.repository

import com.waldoz_x.reptitrack.data.source.remote.HiveMqttClient
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.domain.repository.TerrariumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Esta anotación es crucial para que Dagger Hilt provea una única instancia
class TerrariumRepositoryImpl @Inject constructor( // Este constructor @Inject es crucial para la inyección de dependencias
    private val hiveMqttClient: HiveMqttClient // Usar MQTT en vez de Firestore
) : TerrariumRepository { // Asegúrate de que implementa la interfaz TerrariumRepository

    /**
     * Obtiene un flujo de todos los terrarios para un usuario específico desde la fuente de datos de Firebase.
     * Mapea los objetos DTO (Data Transfer Objects) a objetos de dominio.
     * @param userId El ID del usuario para el que se obtendrán los terrarios.
     * @return Flow que emite una lista de objetos Terrarium.
     */
    override fun getAllTerrariums(userId: String): Flow<List<Terrarium>> {
        val fixedTerrariumId = "esp02"
        return hiveMqttClient.terrariumSensorData.map { terrariumMap ->
            val userTerrariums = terrariumMap[userId] ?: emptyMap()
            val sensorData = userTerrariums[fixedTerrariumId] ?: emptyMap()
            listOf(
                Terrarium(
                    id = fixedTerrariumId,
                    name = "Terrario $fixedTerrariumId",
                    dht22_1_temperature = sensorData["dht22_1_temperature"]?.toFloatOrNull(),
                    dht22_1_humidity = sensorData["dht22_1_humidity"]?.toFloatOrNull(),
                    dht22_2_temperature = sensorData["dht22_2_temperature"]?.toFloatOrNull(),
                    dht22_2_humidity = sensorData["dht22_2_humidity"]?.toFloatOrNull(),
                    dht22_3_temperature = sensorData["dht22_3_temperature"]?.toFloatOrNull(),
                    dht22_3_humidity = sensorData["dht22_3_humidity"]?.toFloatOrNull(),
                    dht22_4_temperature = sensorData["dht22_4_temperature"]?.toFloatOrNull(),
                    dht22_4_humidity = sensorData["dht22_4_humidity"]?.toFloatOrNull(),
                    ds18b20_1_temperature = sensorData["ds18b20_1_temperature"]?.toFloatOrNull(),
                    ds18b20_2_temperature = sensorData["ds18b20_2_temperature"]?.toFloatOrNull(),
                    ds18b20_3_temperature = sensorData["ds18b20_3_temperature"]?.toFloatOrNull(),
                    ds18b20_4_temperature = sensorData["ds18b20_4_temperature"]?.toFloatOrNull(),
                    ds18b20_5_temperature = sensorData["ds18b20_5_temperature"]?.toFloatOrNull(),
                    hc_sr04_1_distance = sensorData["hc_sr04_1_distance"]?.toFloatOrNull(),
                    pzem_1_power = sensorData["pzem_1_power"]?.toFloatOrNull()
                )
            )
        }
    }

    /**
     * Obtiene un terrario específico por su ID para un usuario desde la fuente de datos de Firebase.
     * @param userId El ID del usuario actual.
     * @param id El ID del terrario a obtener.
     * @return El objeto Terrarium si se encuentra, o null si no.
     */
    override fun getTerrariumById(userId: String, id: String): Flow<Terrarium?> {
        val fixedTerrariumId = "esp02"
        return hiveMqttClient.terrariumSensorData.map { terrariumMap ->
            val userTerrariums = terrariumMap[userId] ?: emptyMap()
            val sensorData = userTerrariums[fixedTerrariumId] ?: emptyMap()
            Terrarium(
                id = fixedTerrariumId,
                name = "Terrario $fixedTerrariumId",
                dht22_1_temperature = sensorData["dht22_1_temperature"]?.toFloatOrNull(),
                dht22_1_humidity = sensorData["dht22_1_humidity"]?.toFloatOrNull(),
                dht22_2_temperature = sensorData["dht22_2_temperature"]?.toFloatOrNull(),
                dht22_2_humidity = sensorData["dht22_2_humidity"]?.toFloatOrNull(),
                dht22_3_temperature = sensorData["dht22_3_temperature"]?.toFloatOrNull(),
                dht22_3_humidity = sensorData["dht22_3_humidity"]?.toFloatOrNull(),
                dht22_4_temperature = sensorData["dht22_4_temperature"]?.toFloatOrNull(),
                dht22_4_humidity = sensorData["dht22_4_humidity"]?.toFloatOrNull(),
                ds18b20_1_temperature = sensorData["ds18b20_1_temperature"]?.toFloatOrNull(),
                ds18b20_2_temperature = sensorData["ds18b20_2_temperature"]?.toFloatOrNull(),
                ds18b20_3_temperature = sensorData["ds18b20_3_temperature"]?.toFloatOrNull(),
                ds18b20_4_temperature = sensorData["ds18b20_4_temperature"]?.toFloatOrNull(),
                ds18b20_5_temperature = sensorData["ds18b20_5_temperature"]?.toFloatOrNull(),
                hc_sr04_1_distance = sensorData["hc_sr04_1_distance"]?.toFloatOrNull(),
                pzem_1_power = sensorData["pzem_1_power"]?.toFloatOrNull()
            )
        }
    }

    override suspend fun addTerrarium(userId: String, terrarium: Terrarium) {
        throw NotImplementedError("Solo lectura por MQTT")
    }

    override suspend fun updateTerrarium(userId: String, terrarium: Terrarium) {
        throw NotImplementedError("Solo lectura por MQTT")
    }

    override suspend fun deleteTerrarium(userId: String, id: String) {
        throw NotImplementedError("Solo lectura por MQTT")
    }

    override suspend fun updateTerrariumActuatorState(userId: String, terrariumId: String, actuatorKey: String, newState: Boolean) {
        // Publica el comando MQTT para cambiar el estado del actuador
        val topic = "terrarium/$terrariumId/actuators/$actuatorKey/set"
        val message = if (newState) "ON" else "OFF"
        hiveMqttClient.publishMessage(topic, message)
    }
}
