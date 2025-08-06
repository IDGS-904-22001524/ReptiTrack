package com.waldoz_x.reptitrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waldoz_x.reptitrack.R // Asegúrate de que R.drawable.ic_outline_water_pump_24 exista

// Helper function to get health color
@Composable
fun getHealthColor(value: Float?, sensorType: String): Color {
    if (value == null) return Color.LightGray.copy(alpha = 0.6f)

    return when (sensorType) {
        "distance" -> {
            when {
                value < 10.0f -> Color(0xFFFFA07A) // Salmón claro (Demasiado cerca)
                value > 50.0f -> Color(0xFFADD8E6) // Azul claro (Demasiado lejos)
                else -> Color(0xFF90EE90) // Verde claro (Normal)
            }
        }
        "power" -> {
            when {
                value == null -> Color.LightGray.copy(alpha = 0.6f)
                value > 100.0f -> Color.Red
                else -> Color.Green
            }
        }
        "temperature" -> {
            when {
                value == null -> Color.LightGray.copy(alpha = 0.6f)
                value < 20f || value > 30f -> Color.Red
                else -> Color.Green
            }
        }
        "humidity" -> {
            when {
                value == null -> Color.LightGray.copy(alpha = 0.6f)
                value < 40f || value > 70f -> Color.Red
                else -> Color.Green
            }
        }
        else -> Color.White
    }
}

// Helper function to get sensor icons
@Composable
fun getSensorIcon(sensorType: String): Painter {
    return when (sensorType) {
        "distance" -> painterResource(id = R.drawable.ic_outline_distance_24)
        "power" -> painterResource(id = R.drawable.ic_outline_settings_power_24)
        "temperature" -> painterResource(id = R.drawable.ic_baseline_thermostat_24)
        "humidity" -> painterResource(id = R.drawable.ic_outline_humidity_high_24)
        else -> painterResource(id = R.drawable.ic_baseline_cloud_24) // Generic placeholder
    }
}

@Composable
fun RainSystemCard(
    isActive: Boolean,
    waterDistance: Float?,
    onToggle: (Boolean) -> Unit
) {
    // Colores para la tarjeta activa e inactiva
    val cardActiveColor = Color(0xFF4FC3F7) // Azul claro para sistema de lluvia activo
    val cardInactiveColor = Color(0xFF2C3E50) // Gris oscuro para inactivo

    // Animación del color de fondo de la tarjeta
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) cardActiveColor.copy(alpha = 0.9f) else cardInactiveColor.copy(alpha = 0.7f),
        animationSpec = tween(durationMillis = 400)
    )
    val shadowElevation by animateDpAsState(targetValue = if (isActive) 10.dp else 6.dp, animationSpec = tween(durationMillis = 400))

    val distanceText = waterDistance?.let { "${it} cm" } ?: "N/A"
    val distanceColor = getHealthColor(waterDistance, "distance")

    // Colores de texto ajustados para mejorar la legibilidad cuando la tarjeta está activa (azul claro)
    val titleTextColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF2C3E50) else Color.White, // Más oscuro cuando activo, blanco cuando inactivo
        animationSpec = tween(durationMillis = 400)
    )
    val descriptionTextColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFFFFFFFF) else Color.LightGray, // Un gris más oscuro cuando activo, gris claro cuando inactivo
        animationSpec = tween(durationMillis = 400)
    )
    val iconTint by animateColorAsState(
        targetValue = if (isActive) Color(0xFF2C3E50) else Color.White, // Icono oscuro cuando activo, blanco cuando inactivo
        animationSpec = tween(durationMillis = 400)
    )

    // Nuevo color animado para el texto "Nivel de Agua:"
    val waterLevelLabelColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFFFFFFFF) else Color.LightGray, // Mismo color que la descripción
        animationSpec = tween(durationMillis = 400)
    )

    // Nuevo color animado para el texto "ENCENDIDO" / "APAGADO"
    val statusTextColor by animateColorAsState(
        targetValue = if (isActive) Color.White else Color.White, // Siempre blanco
        animationSpec = tween(durationMillis = 400)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Altura ajustada para el nuevo diseño (Aumentado)
            .padding(8.dp)
            .shadow(shadowElevation, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor), // Usar el color animado aquí
        onClick = { onToggle(!isActive) } // La tarjeta sigue siendo clickeable para alternar
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Padding interno para el contenido
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Espacio entre las secciones
        ) {
            // Sección izquierda: Título y Descripción
            Column(
                modifier = Modifier.weight(1.5f), // Más peso para el texto
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outline_water_pump_24), // Icono del sistema de lluvia
                        contentDescription = "Sistema de Lluvia",
                        tint = iconTint, // Usar el color de icono animado
                        modifier = Modifier.size(48.dp) // Tamaño del icono
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sistema de Lluvia",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor // Usar el color de texto animado
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Controla el sistema de riego automático de tu terrario.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), // Reducido a 14.sp
                    color = descriptionTextColor, // Usar el color de texto animado
                    maxLines = 3, // Limitar a 2 líneas para evitar truncamiento excesivo
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis // Añadir puntos suspensivos si se trunca
                )
            }

            // Sección derecha: Nivel de Agua y Interruptor
            Column(
                modifier = Modifier
                    .weight(1f) // Menos peso para esta sección
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Nivel de Agua (Sensor de Distancia)
                Text(
                    text = "Nivel de Agua:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp), // Slightly larger font for label
                    fontWeight = FontWeight.SemiBold, // Bolder label
                    color = waterLevelLabelColor // Usar el nuevo color animado aquí
                )
                Spacer(modifier = Modifier.height(6.dp)) // Slightly more space below label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = getSensorIcon("distance"),
                        contentDescription = "Distancia del agua",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Increased space between icon and text (Reduced to 8dp)
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), // Slightly reduced font size (Reduced to 22sp)
                        fontWeight = FontWeight.ExtraBold,
                        color = distanceColor
                    )
                    if (waterDistance != null) {
                        Spacer(modifier = Modifier.width(4.dp)) // Slightly more space before circle (Reduced to 4dp)
                        Canvas(modifier = Modifier.size(10.dp), onDraw = { // Slightly larger circle (Reduced to 10dp)
                            drawCircle(color = distanceColor)
                        })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Espacio entre nivel de agua y switch

                // Interruptor de Encendido/Apagado
                Switch(
                    checked = isActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFA7D9F7), // Un azul más claro para el track activo para que se diferencie del fondo
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF424242) // Gris oscuro para el track inactivo
                    )
                )
                Text(
                    text = if (isActive) "ENCENDIDO" else "APAGADO",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = statusTextColor // Usar el nuevo color animado aquí
                )
            }
        }
    }
}

// --- Función de Preview para RainSystemCard ---
@Preview(showBackground = true, widthDp = 400, heightDp = 250)
@Composable
fun PreviewRainSystemCardRedesigned() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF333333)), // Un fondo oscuro para que la tarjeta resalte
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Previsualización del Sistema de Lluvia",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            RainSystemCard(isActive = true, waterDistance = 25.0f, onToggle = {})
            Spacer(modifier = Modifier.height(16.dp))
            RainSystemCard(isActive = false, waterDistance = 70.0f, onToggle = {}) // Demasiado lejos
            Spacer(modifier = Modifier.height(16.dp))
            RainSystemCard(isActive = true, waterDistance = 5.0f, onToggle = {}) // Demasiado cerca
            Spacer(modifier = Modifier.height(16.dp))
            RainSystemCard(isActive = false, waterDistance = null, onToggle = {}) // Sin datos
        }
    }
}
