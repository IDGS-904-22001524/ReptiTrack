package com.waldoz_x.reptitrack.data.model

import com.waldoz_x.reptitrack.domain.model.Terrarium

// DTO (Data Transfer Object) para la entidad Terrarium en Firebase Firestore.
// Los nombres de las propiedades deben coincidir con los campos en Firestore.
data class TerrariumDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,

    // Actuadores
    val waterPumpActive: Boolean = false,
    val fan1Active: Boolean = false,
    val fan2Active: Boolean = false,
    val light1Active: Boolean = false,
    val light2Active: Boolean = false,
    val light3Active: Boolean = false,
    val heatPlate1Active: Boolean = false,

    // Sensores
    val dht22_1_temperature: Float? = null,
    val dht22_1_humidity: Float? = null,
    val dht22_2_temperature: Float? = null,
    val dht22_2_humidity: Float? = null,
    val dht22_3_temperature: Float? = null,
    val dht22_3_humidity: Float? = null,
    val dht22_4_temperature: Float? = null,
    val dht22_4_humidity: Float? = null,

    val ds18b20_1_temperature: Float? = null,
    val ds18b20_2_temperature: Float? = null,
    val ds18b20_3_temperature: Float? = null,
    val ds18b20_4_temperature: Float? = null,
    val ds18b20_5_temperature: Float? = null,

    val hc_sr04_1_distance: Float? = null,
    val pzem_1_power: Float? = null,

    val lastUpdated: Long = 0L
) {
    // Función de mapeo de DTO a modelo de dominio
    fun toDomain(): Terrarium {
        return Terrarium(
            id = id,
            name = name,
            description = description,
            imageUrl = imageUrl,
            waterPumpActive = waterPumpActive,
            fan1Active = fan1Active,
            fan2Active = fan2Active,
            light1Active = light1Active,
            light2Active = light2Active,
            light3Active = light3Active,
            heatPlate1Active = heatPlate1Active,
            dht22_1_temperature = dht22_1_temperature,
            dht22_1_humidity = dht22_1_humidity,
            dht22_2_temperature = dht22_2_temperature,
            dht22_2_humidity = dht22_2_humidity,
            dht22_3_temperature = dht22_3_temperature,
            dht22_3_humidity = dht22_3_humidity,
            dht22_4_temperature = dht22_4_temperature,
            dht22_4_humidity = dht22_4_humidity,
            ds18b20_1_temperature = ds18b20_1_temperature,
            ds18b20_2_temperature = ds18b20_2_temperature,
            ds18b20_3_temperature = ds18b20_3_temperature,
            ds18b20_4_temperature = ds18b20_4_temperature,
            ds18b20_5_temperature = ds18b20_5_temperature,
            hc_sr04_1_distance = hc_sr04_1_distance,
            pzem_1_power = pzem_1_power,
            lastUpdated = lastUpdated
        )
    }

    companion object {
        // Función de mapeo de modelo de dominio a DTO
        fun fromDomain(terrarium: Terrarium): TerrariumDto {
            return TerrariumDto(
                id = terrarium.id,
                name = terrarium.name,
                description = terrarium.description,
                imageUrl = terrarium.imageUrl,
                waterPumpActive = terrarium.waterPumpActive,
                fan1Active = terrarium.fan1Active,
                fan2Active = terrarium.fan2Active,
                light1Active = terrarium.light1Active,
                light2Active = terrarium.light2Active,
                light3Active = terrarium.light3Active,
                heatPlate1Active = terrarium.heatPlate1Active,
                dht22_1_temperature = terrarium.dht22_1_temperature,
                dht22_1_humidity = terrarium.dht22_1_humidity,
                dht22_2_temperature = terrarium.dht22_2_temperature,
                dht22_2_humidity = terrarium.dht22_2_humidity,
                dht22_3_temperature = terrarium.dht22_3_temperature,
                dht22_3_humidity = terrarium.dht22_3_humidity,
                dht22_4_temperature = terrarium.dht22_4_temperature,
                dht22_4_humidity = terrarium.dht22_4_humidity,
                ds18b20_1_temperature = terrarium.ds18b20_1_temperature,
                ds18b20_2_temperature = terrarium.ds18b20_2_temperature,
                ds18b20_3_temperature = terrarium.ds18b20_3_temperature,
                ds18b20_4_temperature = terrarium.ds18b20_4_temperature,
                ds18b20_5_temperature = terrarium.ds18b20_5_temperature,
                hc_sr04_1_distance = terrarium.hc_sr04_1_distance,
                pzem_1_power = terrarium.pzem_1_power,
                lastUpdated = terrarium.lastUpdated
            )
        }
    }
}
