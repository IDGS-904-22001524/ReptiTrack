package com.waldoz_x.reptitrack.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview // Important for @Preview
import androidx.compose.ui.unit.dp
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.R // Ensure R is available for drawables

// Composable to display a compact visual card of a terrarium.
// Composable para mostrar una tarjeta visual compacta de un terrario.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerrariumCard(
    terrarium: Terrarium,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit // Callback for when the card is clicked
) {
    val cardBackgroundColor = Color.Black.copy(alpha = 0.7f) // Adjusted alpha for more darkness

    Card(
        modifier = modifier
            .width(180.dp) // Fixed width for two columns layout
            .padding(8.dp) // Reduced outer padding
            .clickable { onClick(terrarium.id) }, // Makes the card clickable
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp), // Rounded corners
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor // Fixed dark transparent background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp), // Adjusted horizontal padding to give more space
            horizontalAlignment = Alignment.CenterHorizontally // Centers content horizontally
        ) {
            // Terrarium image
            // Imagen del terrario
            Image(
                painter = painterResource(id = R.drawable.terrario),
                contentDescription = "Imagen del terrario ${terrarium.name}",
                modifier = Modifier
                    .size(80.dp) // Smaller image size
                    .clip(RoundedCornerShape(8.dp)) // Rounded corners
                    .background(Color.DarkGray.copy(alpha = 0.8f)), // Slightly lighter dark background for image
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Terrarium name - fixed color (e.g., white or light gray)
            // Nombre del terrario - color fijo (ej. blanco o gris claro)
            Text(
                text = terrarium.name,
                style = MaterialTheme.typography.titleMedium, // Medium title size
                fontWeight = FontWeight.Bold,
                color = Color.White, // Fixed white color for the name
                maxLines = 1, // Limit to one line for small cards
                overflow = TextOverflow.Ellipsis // Add ellipsis if text is too long
            )
            Spacer(modifier = Modifier.height(4.dp))

            // DHT22 readings (Temperature and Humidity)
            // Lecturas del DHT22 (Temperatura y Humedad)
            val tempDHT = terrarium.dht22_1_temperature
            val humDHT = terrarium.dht22_1_humidity

            // Function to get color based on temperature
            // Función para obtener el color basado en la temperatura
            fun getTemperatureColor(temp: Float?): Color {
                return when (temp) {
                    null -> Color.Gray // N/A color
                    in 0f..17f -> Color(0xFF2196F3) // Blue for very cold
                    in 17.1f..22f -> Color(0xFF03A9F4) // Lighter blue for cold
                    in 22.1f..27f -> Color(0xFF8BC34A) // Green for ideal/normal
                    in 27.1f..32f -> Color(0xFFFFC107) // Orange for warm
                    else -> Color(0xFFF44336) // Red for hot
                }
            }

            // Function to get color based on humidity
            // Función para obtener el color basado en la humedad
            fun getHumidityColor(humidity: Float?): Color {
                return when (humidity) {
                    null -> Color.Gray // N/A color
                    in 0f..30f -> Color(0xFFEF5350) // Red for very dry
                    in 30.1f..50f -> Color(0xFFFF9800) // Orange for dry
                    in 50.1f..70f -> Color(0xFF4CAF50) // Green for ideal
                    else -> Color(0xFF2196F3) // Blue for high humidity
                }
            }

            Column( // Changed from Row to Column to stack temperature and humidity vertically
                horizontalAlignment = Alignment.CenterHorizontally, // Center each sensor reading row
                modifier = Modifier.fillMaxWidth()
            ) {
                // Temperature Reading
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icono de Temperatura
                    Icon(
                        painter = painterResource(id = R.drawable.ic_temperatura), // Using painter for custom drawable
                        contentDescription = "DHT22 Temperature",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp) // Increased icon size
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Spacer between icon and text
                    Text(
                        text = tempDHT?.let { "${String.format("%.1f", it)}°C" } ?: "N/A", // Show N/A if null
                        style = MaterialTheme.typography.bodyLarge, // Changed to bodyLarge for better visibility
                        color = getTemperatureColor(tempDHT), // Dynamic color for text
                        fontWeight = FontWeight.Bold // Make sensor readings bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp)) // Small space between temperature and humidity readings

                // Humidity Reading
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icono de Humedad
                    Icon(
                        painter = painterResource(id = R.drawable.ic_humedad), // Using painter for custom drawable
                        contentDescription = "DHT22 Humidity",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp) // Increased icon size
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Spacer between icon and text
                    Text(
                        text = humDHT?.let { "${String.format("%.1f", it)}%" } ?: "N/A", // Show N/A if null
                        style = MaterialTheme.typography.bodyLarge, // Changed to bodyLarge for better visibility
                        color = getHumidityColor(humDHT), // Dynamic color for text
                        fontWeight = FontWeight.Bold // Make sensor readings bold
                    )
                }
            }
        }
    }
}


// --- PREVIEW SECTION ---
// Sección de previsualización
@Preview(showBackground = true, name = "Small Terrarium Card")
@Composable
fun PreviewSmallTerrariumCard() {
    // Example data for the small card preview
    // Datos de ejemplo para la previsualización de la tarjeta pequeña
    val sampleTerrariumSmallNormal = Terrarium(
        id = "ter_small_001",
        name = "Iguana Terrarium", // Changed name for example
        description = "Un terrario de ejemplo para previsualización.",
        imageResId = R.drawable.terrario,
        dht22_1_temperature = 25.0f, // Normal temp (green)
        dht22_1_humidity = 60.5f // Normal humidity (green)
    )

    val sampleTerrariumSmallHighTemp = Terrarium(
        id = "ter_small_003",
        name = "Gecko Hotbox", // Changed name for example
        description = "Terrarium with high temperature.",
        imageResId = R.drawable.terrario, // Use the terrario image
        dht22_1_temperature = 35.5f, // High temp (red)
        dht22_1_humidity = 40.0f // Dry humidity (orange)
    )

    val sampleTerrariumSmallLowTemp = Terrarium(
        id = "ter_small_004",
        name = "Chameleon Den", // Changed name for example
        description = "Terrarium with low temperature.",
        imageResId = R.drawable.terrario, // Use the terrario image
        dht22_1_temperature = 15.0f, // Low temp (blue)
        dht22_1_humidity = 80.0f // High humidity (blue)
    )

    val sampleTerrariumSmallNoData = Terrarium(
        id = "ter_small_002",
        name = "Empty Enclosure", // Changed name for example
        description = "Terrarium with no sensor data.",
        imageResId = R.drawable.terrario, // Use the terrario image
        dht22_1_temperature = null,
        dht22_1_humidity = null
    )

    MaterialTheme {
        // Set a dark background for the preview to better visualize transparency
        // Establece un fondo oscuro para la previsualización para visualizar mejor la transparencia
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray) // Simulate your dark background
                .padding(16.dp)
        ) {
            TerrariumCard(terrarium = sampleTerrariumSmallNormal, onClick = { terrariumId ->
                println("Normal terrarium clicked: $terrariumId")
            })
            Spacer(modifier = Modifier.height(16.dp))
            TerrariumCard(terrarium = sampleTerrariumSmallHighTemp, onClick = { terrariumId ->
                println("Hot terrarium clicked: $terrariumId")
            })
            Spacer(modifier = Modifier.height(16.dp))
            TerrariumCard(terrarium = sampleTerrariumSmallLowTemp, onClick = { terrariumId ->
                println("Cold terrarium clicked: $terrariumId")
            })
            Spacer(modifier = Modifier.height(16.dp))
            TerrariumCard(terrarium = sampleTerrariumSmallNoData, onClick = { terrariumId ->
                println("Empty terrarium clicked: $terrariumId")
            })
        }
    }
}
