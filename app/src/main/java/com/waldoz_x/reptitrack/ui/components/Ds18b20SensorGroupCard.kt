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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview // Importar la anotación Preview
import androidx.compose.ui.unit.sp // Importar para usar sp en el tamaño de fuente
import androidx.compose.foundation.layout.FlowRow // ¡Importación actualizada a la versión nativa de Compose!
import androidx.compose.ui.text.style.TextAlign
import com.waldoz_x.reptitrack.ui.components.sensorUtils.getHealthColor
import com.waldoz_x.reptitrack.ui.components.sensorUtils.getSensorIcon

@Composable
fun Ds18b20SensorGroupCard(sensorData: Map<String, String>) {
    val dsTemperatures = sensorData.filterKeys { it.startsWith("ds18b20_") && it.endsWith("_temperature") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(animationSpec = tween(durationMillis = 300)), // Animación para el tamaño del contenido
        shape = RoundedCornerShape(20.dp), // Bordes más redondeados
        elevation = CardDefaults.cardElevation(6.dp), // Sombra consistente
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50).copy(alpha = 0.7f)) // Tono oscuro y transparente
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Más padding interno
        ) {
            Text(
                text = "Sensores de Temperatura", // Nombre más amigable
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), // Tamaño de fuente más grande
                fontWeight = FontWeight.ExtraBold, // Más énfasis
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp) // Más espacio debajo del título
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Centrar los elementos en el FlowRow
                verticalArrangement = Arrangement.spacedBy(16.dp) // Más espacio vertical entre elementos
            ) {
                // Iterar hasta 5 sensores, o menos si no hay datos
                for (i in 1..5) {
                    val key = "ds18b20_${i}_temperature"
                    val value = dsTemperatures[key]
                    val tempValue = value?.replace("°C", "")?.toFloatOrNull()
                    val tempColor = getHealthColor(tempValue, "temperature")

                    // Nuevo diseño para cada ítem de temperatura
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 12.dp) // Más espacio horizontal para cada columna
                            .width(IntrinsicSize.Min) // Asegura que la columna se ajuste a su contenido
                    ) {
                        Icon(
                            painter = getSensorIcon("temperature"),
                            contentDescription = "Temperatura",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp) // Ícono aún más grande
                        )
                        Spacer(modifier = Modifier.height(6.dp)) // Espacio entre ícono y etiqueta
                        Text(
                            text = "Temp $i", // Etiqueta del sensor
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp), // Tamaño de fuente ligeramente más grande
                            fontWeight = FontWeight.SemiBold,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp)) // Espacio entre etiqueta y valor
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = value ?: "N/A",
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp), // Tamaño de fuente más grande para el valor
                                fontWeight = FontWeight.ExtraBold, // Más énfasis en el valor
                                color = tempColor
                            )
                            if (tempValue != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Canvas(modifier = Modifier.size(12.dp), onDraw = { // Círculo un poco más grande
                                    drawCircle(color = tempColor)
                                })
                            }
                        }
                    }
                }
            }
            if (dsTemperatures.isEmpty()) {
                Text(
                    text = "No hay datos de sensores DS18B20 disponibles.",
                    style = MaterialTheme.typography.bodyMedium, // Tamaño de fuente ajustado
                    color = Color.LightGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp), // Más espacio superior
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- Función de Preview para Ds18b20SensorGroupCard ---
@Preview(showBackground = true, widthDp = 400) // Ancho para ver el FlowRow
@Composable
fun PreviewDs18b20SensorGroupCard() {
    // Datos de ejemplo para el preview
    val sampleSensorData = mapOf(
        "ds18b20_1_temperature" to "22.3°C",
        "ds18b20_2_temperature" to "28.1°C",
        "ds18b20_3_temperature" to "19.5°C",
        "ds18b20_4_temperature" to "31.0°C", // Fuera de rango para mostrar color
        "ds18b20_5_temperature" to "24.8°C"
    )

    val emptySensorData = mapOf<String, String>() // Datos vacíos para probar el mensaje "N/A"

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ejemplo con datos:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Ds18b20SensorGroupCard(sensorData = sampleSensorData)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ejemplo sin datos:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Ds18b20SensorGroupCard(sensorData = emptySensorData)
        }
    }
}
