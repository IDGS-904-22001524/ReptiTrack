package com.waldoz_x.reptitrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview // Importar la anotación Preview
import androidx.compose.ui.unit.sp // Importar para usar sp en el tamaño de fuente
import com.waldoz_x.reptitrack.R // Necesario para painterResource
import java.util.Locale

@Composable
fun ActuatorControlCard(
    actuatorKey: String,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val actuatorName = when (actuatorKey) {
        "light1_active" -> "Luz 1"
        "light2_active" -> "Luz 2"
        "light3_active" -> "Luz 3"
        "heat_plate1_active" -> "Placa de Calor 1"
        else -> actuatorKey.replace("_active", "").replace("_", " ").capitalize(Locale.getDefault())
    }

    val icon: Painter = when (actuatorKey) {
        "light1_active", "light2_active", "light3_active" -> painterResource(id = R.drawable.baseline_lightbulb_24)
        "heat_plate1_active" -> painterResource(id = R.drawable.ic_outline_heat_24)
        else -> painterResource(id = R.drawable.ic_baseline_info_24)
    }

    val cardActiveColor = when (actuatorKey) {
        "light1_active" -> Color(0xFFFFA500) // Naranja
        "light2_active" -> Color(0xFF6A5ACD) // Azul Púrpura
        "light3_active" -> Color(0xFF8A2BE2) // Azul Violeta
        "heat_plate1_active" -> Color(0xFFF44336) // Rojo
        else -> Color(0xFF4CAF50) // Verde
    }
    val cardInactiveColor = Color(0xFF616161) // Gris oscuro

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) cardActiveColor.copy(alpha = 0.9f) else Color(0xFF2C3E50).copy(alpha = 0.7f), // Más opaco cuando activo, tono oscuro cuando inactivo
        animationSpec = tween(durationMillis = 400) // Animación más suave
    )

    // Colores del track del switch para el nuevo tema
    val switchCheckedTrackColor = cardActiveColor.copy(alpha = 0.8f) // Un poco más oscuro que el color activo
    val switchUncheckedTrackColor = Color(0xFF424242) // Un gris más oscuro para el estado inactivo

    Card(
        modifier = Modifier
            .width(200.dp) // Card más ancho
            .height(180.dp) // Card más alto
            .padding(8.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp)), // Sombra más pronunciada
        shape = RoundedCornerShape(20.dp), // Bordes más redondeados
        elevation = CardDefaults.cardElevation(0.dp), // La sombra ya la manejamos con .shadow
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = { onToggle(!isActive) } // La tarjeta es clickeable para alternar
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Más padding interno y espacioso
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly // Distribuye el espacio uniformemente
        ) {
            Icon(
                painter = icon,
                contentDescription = actuatorName,
                tint = Color.White,
                modifier = Modifier.size(60.dp) // Ícono aún más grande para impacto
            )
            Text(
                text = actuatorName,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp), // Tamaño de fuente más grande
                fontWeight = FontWeight.ExtraBold, // Mayor énfasis
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Switch(
                checked = isActive,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = switchCheckedTrackColor, // Color de track ajustado
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = switchUncheckedTrackColor // Color de track ajustado
                )
            )
        }
    }
}

// --- Función de Preview para ActuatorControlCard ---
@Preview(showBackground = true, widthDp = 400)
@Composable
fun PreviewActuatorControlCard() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Actuadores de Control",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black, // O un color que contraste con el fondo del preview
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Fila de ejemplos de actuadores
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActuatorControlCard(actuatorKey = "light1_active", isActive = true, onToggle = {})
                ActuatorControlCard(actuatorKey = "light2_active", isActive = false, onToggle = {})
                ActuatorControlCard(actuatorKey = "heat_plate1_active", isActive = true, onToggle = {})
                ActuatorControlCard(actuatorKey = "light3_active", isActive = false, onToggle = {})
                ActuatorControlCard(actuatorKey = "fan_active", isActive = true, onToggle = {}) // Un actuador genérico
            }
        }
    }
}
