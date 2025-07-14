 package com.waldoz_x.reptitrack.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.R // Asegúrate de que R esté disponible para drawables

// Composable para mostrar una tarjeta visual de un terrario.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerrariumCard(
    terrarium: Terrarium,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit // Callback para cuando se hace clic en la tarjeta
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp) // Padding exterior
            .clickable { onClick(terrarium.id) }, // Hace la tarjeta clickeable
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp), // Esquinas redondeadas
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Color de fondo de la tarjeta
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f) // Ocupa el espacio restante
            ) {
                Text(
                    text = terrarium.name,
                    style = MaterialTheme.typography.headlineSmall, // Título más grande
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = terrarium.description.ifEmpty { "Terrario para reptiles." }, // Descripción
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Estado de los actuadores más importantes
                ActuatorStatusRow(
                    icon = Icons.Default.Face,
                    label = "Bomba Agua",
                    isActive = terrarium.waterPumpActive
                )
                ActuatorStatusRow(
                    icon = Icons.Default.Face,
                    label = "Foco 1",
                    isActive = terrarium.light1Active
                )
                ActuatorStatusRow(
                    icon = Icons.Default.Face,
                    label = "Ventilador 1",
                    isActive = terrarium.fan1Active
                )
                ActuatorStatusRow(
                    icon = Icons.Default.Face, // Icono para placa de calor
                    label = "Placa Calor 1",
                    isActive = terrarium.heatPlate1Active
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lecturas clave:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Lecturas de Sensores más importantes
                SensorReadingRow(
                    icon = Icons.Default.Face,
                    label = "Temp (DHT22_1)",
                    value = terrarium.dht22_1_temperature,
                    unit = "°C"
                )
                SensorReadingRow(
                    icon = Icons.Default.Face,
                    label = "Hum (DHT22_1)",
                    value = terrarium.dht22_1_humidity,
                    unit = "%"
                )
                SensorReadingRow(
                    icon = Icons.Default.Face,
                    label = "Temp (DS18B20_1)",
                    value = terrarium.ds18b20_1_temperature,
                    unit = "°C"
                )
                SensorReadingRow(
                    icon = Icons.Default.Face,
                    label = "Distancia",
                    value = terrarium.hc_sr04_1_distance,
                    unit = "cm"
                )
                SensorReadingRow(
                    icon = Icons.Default.Face,
                    label = "Consumo",
                    value = terrarium.pzem_1_power,
                    unit = "W"
                )
                // Puedes añadir más filas aquí si consideras otros sensores importantes para el resumen
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Imagen del terrario (si existe)
            if (terrarium.imageUrl != null && terrarium.imageUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(terrarium.imageUrl),
                    contentDescription = "Imagen del terrario ${terrarium.name}",
                    modifier = Modifier
                        .size(100.dp) // Tamaño de la imagen
                        .clip(RoundedCornerShape(8.dp)), // Esquinas redondeadas
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder si no hay imagen
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Usa un icono de tu proyecto
                    contentDescription = "Placeholder de terrario",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                )
            }
        }
    }
}

// Composable auxiliar para mostrar el estado de un actuador
@Composable
fun ActuatorStatusRow(icon: ImageVector, label: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isActive) "Activado" else "Desactivado",
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

// Composable auxiliar para mostrar una fila de lectura de sensor
@Composable
fun SensorReadingRow(icon: ImageVector, label: String, value: Float?, unit: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value?.let { "${String.format("%.1f", it)} $unit" } ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
