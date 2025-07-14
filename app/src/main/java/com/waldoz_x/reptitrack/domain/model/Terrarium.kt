package com.waldoz_x.reptitrack.domain.model

// Modelo de dominio para un Terrario.
// Contiene todos los datos relevantes para la lógica de negocio,
// incluyendo el estado de actuadores y las últimas lecturas de sensores.
data class Terrarium(
    val id: String = "", // ID único del terrario (ej. ID de documento de Firestore)
    val name: String = "Nuevo Terrario", // Nombre del terrario
    val description: String = "", // Descripción opcional
    val imageUrl: String? = null, // URL de la imagen del terrario (opcional)

    // Estado de los Actuadores (dispositivos de control)
    val waterPumpActive: Boolean = false, // Bomba de Agua
    val fan1Active: Boolean = false,      // Ventilador 1
    val fan2Active: Boolean = false,      // Ventilador 2
    val light1Active: Boolean = false,    // Foco 1
    val light2Active: Boolean = false,    // Foco 2
    val light3Active: Boolean = false,    // Foco 3
    val heatPlate1Active: Boolean = false, // Placa de Calor 1

    // Últimas lecturas de Sensores (para la vista de resumen en Home)
    // Usamos Float? para permitir valores nulos si la lectura no está disponible
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

    val hc_sr04_1_distance: Float? = null, // Distancia del sensor ultrasónico
    val pzem_1_power: Float? = null,       // Consumo de energía del PZEM

    val lastUpdated: Long = System.currentTimeMillis() // Timestamp de la última actualización
)
