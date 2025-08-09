package com.waldoz_x.reptitrack.ui.components.sensorUtils

import com.waldoz_x.reptitrack.R

fun getSensorIcon(sensorType: String): Int {
    return when (sensorType.lowercase()) {
        "temperature", "temperatura" -> R.drawable.ic_temperatura
        "humidity", "humedad" -> R.drawable.ic_humedad
        "distance", "distancia" -> R.drawable.ic_outline_sensors_24 // Usa el ícono que tengas para distancia
        "power", "potencia" -> R.drawable.ic_outline_water_pump_24 // Usa el ícono que tengas para potencia
        else -> R.drawable.ic_baseline_account_circle_24 // Ícono genérico o uno por defecto
    }
}
