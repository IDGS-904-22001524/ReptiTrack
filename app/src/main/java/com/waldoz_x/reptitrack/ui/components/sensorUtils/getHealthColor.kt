package com.waldoz_x.reptitrack.ui.components.sensorUtils

import androidx.compose.ui.graphics.Color

fun getHealthColor(value: Float?, sensorType: String): Color {
    if (value == null) return Color.LightGray.copy(alpha = 0.6f)
    return when (sensorType) {
        "temperature" -> when {
            value < 20.0f -> Color(0xFFADD8E6) // Light Blue (Cold)
            value > 30.0f -> Color(0xFFFFA07A) // Light Salmon (Hot)
            else -> Color(0xFF90EE90) // Light Green (Normal)
        }
        "humidity" -> when {
            value < 50.0f -> Color(0xFFFFA07A) // Light Salmon (Dry)
            value > 80.0f -> Color(0xFFADD8E6) // Light Blue (Humid)
            else -> Color(0xFF90EE90) // Light Green (Normal)
        }
        "distance" -> when {
            value < 10.0f -> Color(0xFFFFA07A) // Too close
            value > 50.0f -> Color(0xFFADD8E6) // Too far
            else -> Color(0xFF90EE90) // Normal
        }
        "power" -> when {
            value > 100.0f -> Color(0xFFFFA07A) // High power
            else -> Color(0xFF90EE90) // Normal
        }
        else -> Color.White
    }
}
