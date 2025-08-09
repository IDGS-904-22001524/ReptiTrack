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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview // Importar la anotación Preview
import androidx.compose.ui.unit.sp // Importar para usar sp en el tamaño de fuente
import com.waldoz_x.reptitrack.R // Necesario para painterResource
import com.waldoz_x.reptitrack.ui.components.sensorUtils.getHealthColor
import com.waldoz_x.reptitrack.ui.components.sensorUtils.getSensorIcon
import androidx.compose.ui.text.style.TextAlign

@Composable
fun OtherSensorsGroupCard(
    sensorData: Map<String, String>,
) {
    val distanceText = sensorData["hc_sr04_1_distance"]?.takeIf { it != "" && it != "null" } ?: "N/A"
    val powerText = sensorData["pzem_1_power"]?.takeIf { it != "" && it != "null" } ?: "N/A"
    val foodLoadsText = sensorData["food_dispenser_loads"]?.takeIf { it != "" && it != "null" } ?: "N/A"

    val distanceValue = distanceText.replace(" cm", "").toFloatOrNull()
    val powerValue = powerText.replace(" W", "").toFloatOrNull()

    val distanceColor = getHealthColor(distanceValue, "distance")
    val powerColor = getHealthColor(powerValue, "power")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(animationSpec = tween(durationMillis = 300)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50).copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Otros Dispositivos",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Fila para Distancia
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = getSensorIcon("distance")),
                        contentDescription = "Distancia",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Distancia:",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = distanceColor
                    )
                    if (distanceValue != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Canvas(modifier = Modifier.size(10.dp), onDraw = {
                            drawCircle(color = distanceColor)
                        })
                    }
                }
            }

            // Fila para Potencia
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = getSensorIcon("power")),
                        contentDescription = "Potencia",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Potencia:",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = powerText,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = powerColor
                    )
                    if (powerValue != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Canvas(modifier = Modifier.size(10.dp), onDraw = {
                            drawCircle(color = powerColor)
                        })
                    }
                }
            }

            // Fila para Cargas de Comida
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outline_heat_24),
                        contentDescription = "Cargas de comida",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cargas Comida:",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = foodLoadsText,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// --- Función de Preview para OtherSensorsGroupCard ---
@Preview(showBackground = true, widthDp = 360) // Ajusta el ancho para el preview
@Composable
fun PreviewOtherSensorsGroupCard() {
    // Datos de ejemplo para el preview
    val sampleSensorData1 = mapOf(
        "hc_sr04_1_distance" to "15.5 cm",
        "pzem_1_power" to "25.0 W",
        "food_dispenser_loads" to "3"
    )
    val sampleSensorData2 = mapOf(
        "hc_sr04_1_distance" to "5.0 cm", // Distancia "cercana"
        "pzem_1_power" to "150.0 W",    // Potencia "alta"
        "food_dispenser_loads" to "0"
    )
    val emptySensorData = mapOf<String, String>() // Datos vacíos

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ejemplo con datos:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OtherSensorsGroupCard(sensorData = sampleSensorData1)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ejemplo con datos críticos:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OtherSensorsGroupCard(sensorData = sampleSensorData2)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ejemplo sin datos:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OtherSensorsGroupCard(sensorData = emptySensorData)
        }
    }
}
