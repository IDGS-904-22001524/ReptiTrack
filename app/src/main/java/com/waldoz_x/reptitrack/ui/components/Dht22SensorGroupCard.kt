package com.waldoz_x.reptitrack.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview // Importar la anotación Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp // Importar para usar sp en el tamaño de fuente
// Importar las funciones reales de sensorUtils
import com.waldoz_x.reptitrack.ui.components.sensorUtils.getHealthColor
import com.waldoz_x.reptitrack.ui.components.sensorUtils.getSensorIcon
import androidx.compose.ui.res.painterResource
import com.waldoz_x.reptitrack.R

// --- Tu Composable original modificado para ser más compacto ---
@Composable
fun Dht22SensorGroupCard(
    sensorNumber: Int,
    sensorData: Map<String, String>,
    hasMqttData: Boolean = false // <- este parámetro ya no se usa
) {
    val tempKey = "dht22_${sensorNumber}_temperature"
    val humKey = "dht22_${sensorNumber}_humidity"
    val temperatureValue = sensorData[tempKey]?.replace("°C", "")?.toFloatOrNull()
    val humidityValue = sensorData[humKey]?.replace("%", "")?.toFloatOrNull()

    // Si no hay datos, muestra "N/A"
    val temperatureText = sensorData[tempKey]?.takeIf { !it.isNullOrBlank() && it != "null" } ?: "N/A"
    val humidityText = sensorData[humKey]?.takeIf { !it.isNullOrBlank() && it != "null" } ?: "N/A"

    val tempColor = getHealthColor(temperatureValue, "temperature")
    val humColor = getHealthColor(humidityValue, "humidity")

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(130.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50).copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = getSensorIcon("temperature")),
                    contentDescription = "Temperatura",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = temperatureText,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = tempColor
                )
                if (temperatureValue != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Canvas(modifier = Modifier.size(12.dp), onDraw = {
                        drawCircle(color = tempColor)
                    })
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = getSensorIcon("humidity")),
                    contentDescription = "Humedad",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = humidityText,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = humColor
                )
                if (humidityValue != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Canvas(modifier = Modifier.size(12.dp), onDraw = {
                        drawCircle(color = humColor)
                    })
                }
            }
            // ...NO mostrar mensaje de "Esperando datos..."...
        }
    }
}

// --- Función de Preview ---
@Preview(showBackground = true)
@Composable
fun PreviewDht22SensorGroupCard() {
    // Para el preview, puedes simular datos de sensor
    val sampleSensorData1 = mapOf(
        "dht22_1_temperature" to "25.5°C",
        "dht22_1_humidity" to "60.2%"
    )
    val sampleSensorData2 = mapOf(
        "dht22_2_temperature" to "18.0°C", // Temperatura fuera de rango para mostrar color rojo
        "dht22_2_humidity" to "80.0%"    // Humedad fuera de rango para mostrar color rojo
    )
    val sampleSensorData3 = mapOf(
        "dht22_3_temperature" to "N/A", // Datos no disponibles
        "dht22_3_humidity" to "N/A"
    )

    // Puedes envolverlo en un tema si tu aplicación usa un tema personalizado
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Dht22SensorGroupCard(sensorNumber = 1, sensorData = sampleSensorData1)
            Spacer(modifier = Modifier.height(16.dp))
            Dht22SensorGroupCard(sensorNumber = 2, sensorData = sampleSensorData2)
            Spacer(modifier = Modifier.height(16.dp))
            Dht22SensorGroupCard(sensorNumber = 3, sensorData = sampleSensorData3)
        }
    }
}
