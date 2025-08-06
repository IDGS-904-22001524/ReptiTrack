package com.waldoz_x.reptitrack.ui.screens.terrariumdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waldoz_x.reptitrack.ui.theme.ReptiTrackTheme
import com.waldoz_x.reptitrack.domain.model.Terrarium
import java.util.Locale
import java.time.LocalTime // Import LocalTime
import java.time.temporal.ChronoUnit // Import ChronoUnit
import kotlinx.coroutines.delay // Import delay for LaunchedEffect
import androidx.compose.runtime.LaunchedEffect // Import LaunchedEffect

// Imports for using Painter and painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.waldoz_x.reptitrack.R // Make sure to import your R class to access drawables

// Import for loading images asynchronously (Coil) - Not directly used for background images now, but kept for future use if needed
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke // Import BorderStroke
import androidx.compose.animation.animateColorAsState // Import animateColorAsState
import androidx.compose.foundation.Canvas // Import Canvas for drawing circles
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import com.waldoz_x.reptitrack.ui.components.Dht22SensorGroupCard
import com.waldoz_x.reptitrack.ui.components.Ds18b20SensorGroupCard
import com.waldoz_x.reptitrack.ui.components.OtherSensorsGroupCard
import com.waldoz_x.reptitrack.ui.components.ActuatorControlCard
import com.waldoz_x.reptitrack.ui.components.FoodDispenserCard
import com.waldoz_x.reptitrack.ui.components.RainSystemCard

// Definition of filter categories
enum class TerrariumCategory(val displayName: String) {
    ALL("Todo"),
    SENSORS("Sensores"),
    LIGHTS("Iluminación"),
    CLIMATE_WATER("Clima y Agua"),
    RAIN_SYSTEM("Sistema de Lluvia"), // Nueva categoría
    OTHER_SENSORS("Otros Sensores")
}

// This is the terrarium detail screen
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TerrariumDetailScreen(
    terrariumId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TerrariumDetailViewModel = hiltViewModel()
) {
    // Observe terrarium state from the ViewModel
    val terrariumState by viewModel.terrariumState.collectAsState()
    val sensorData by viewModel.sensorData.collectAsState()
    val actuatorStates by viewModel.actuatorStates.collectAsState()
    val isMqttConnected by viewModel.isMqttConnected.collectAsState()

    // State for category filter
    var selectedCategory by remember { mutableStateOf(TerrariumCategory.ALL) }

    // --- Logic to handle real vs. mock data ---
    // If terrariumState is null, it means no real terrarium has been loaded (or we are in preview/mock mode)
    val useMockData = terrariumState == null && terrariumId == "placeholder_terrarium_id"

    val currentTerrarium = if (useMockData) createMockTerrarium(terrariumId) else terrariumState
    val currentSensorData = if (useMockData) createMockSensorData() else sensorData
    // We use a local mutable state for mock actuators so they respond to the UI
    var mockActuatorStates by remember { mutableStateOf(createMockActuatorStates()) }
    val currentActuatorStates = if (useMockData) mockActuatorStates else actuatorStates
    val currentMqttConnected = if (useMockData) true else isMqttConnected // Assume connected in mock

    // Calculate average temperature and humidity
    // These variables are moved to a higher scope so they are accessible by the summary Card
    val dhtTemperatures = currentSensorData.filterKeys { it.startsWith("dht22_") && it.endsWith("_temperature") }
        .mapNotNull { it.value.replace("°C", "").toFloatOrNull() }
    val dsTemperatures = currentSensorData.filterKeys { it.startsWith("ds18b20_") && it.endsWith("_temperature") }
        .mapNotNull { it.value.replace("°C", "").toFloatOrNull() }
    val allTemperatures = dhtTemperatures + dsTemperatures
    val averageTemperature = if (allTemperatures.isNotEmpty()) {
        String.format(Locale.getDefault(), "%.1f°C", allTemperatures.average())
    } else "N/A"

    val dhtHumidities = currentSensorData.filterKeys { it.startsWith("dht22_") && it.endsWith("_humidity") }
        .mapNotNull { it.value.replace("%", "").toFloatOrNull() }
    val averageHumidity = if (dhtHumidities.isNotEmpty()) {
        String.format(Locale.getDefault(), "%.1f%%", dhtHumidities.average())
    } else "N/A"

    val mainTemperature = currentSensorData["dht22_1_temperature"] ?: currentSensorData["ds18b20_1_temperature"] ?: "N/A"
    val lastUpdatedText = currentSensorData["lastUpdated"] ?: "N/A"

    // --- Logic for dynamic background (Day/Night) ---
    var isDayTime by remember { mutableStateOf(isCurrentlyDayTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalTime.now()
            val nextMinuteStart = now.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES)
            val delayMillis = now.until(nextMinuteStart, ChronoUnit.MILLIS)
            delay(delayMillis) // Wait until the start of the next minute
            isDayTime = isCurrentlyDayTime()
        }
    }

    // Background images (User needs to provide these in res/drawable)
    val dayBackgroundImage = painterResource(id = R.drawable.jungle_background)
    val nightBackgroundImage = painterResource(id = R.drawable.night_background_image)

    val currentBackgroundImage = if (isDayTime) dayBackgroundImage else nightBackgroundImage

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Consider externalizing this string to strings.xml
                    Text(
                        text = "Terrario: ${currentTerrarium?.name ?: terrariumId}",
                        color = Color.Black // Changed text color to black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        // Consider externalizing this description to strings.xml
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = Color.Black // Changed icon tint to black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement more options if necessary */ }) {
                        // Consider externalizing this description to strings.xml
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Más información",
                            tint = Color.Black // Changed icon tint to black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent) // Transparent TopAppBar
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply Scaffold padding here
        ) {
            // 1. Background Image (bottom layer)
            Image(
                painter = currentBackgroundImage,
                contentDescription = "Fondo del terrario", // Consider externalizing
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Scale to fill, cropping if necessary
            )

            // Removed the intermediate transparent Card as per user request

            // 3. Existing functional cards (top layer) - this is the current Column content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Keep original padding for content
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp)) // Top space

                // Show a loading indicator only if we are NOT using mock data and the terrarium has not yet loaded
                if (terrariumState == null && !useMockData) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                        // Consider externalizing this string to strings.xml
                        Text(
                            text = "Cargando datos del terrario...",
                            modifier = Modifier.padding(top = 80.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Main screen content, visible when terrarium is loaded or in mock mode
                    AnimatedVisibility(
                        visible = currentTerrarium != null, // Visible if currentTerrarium has data (real or mock)
                        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 500))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // General Summary Card with terrarium photo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .animateContentSize(animationSpec = tween(durationMillis = 300)), // Size animation
                                shape = RoundedCornerShape(24.dp), // Increased rounded corners
                                elevation = CardDefaults.cardElevation(8.dp), // Higher elevation to make it stand out
                                // Use a darker, transparent color for the primary container
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A5C70).copy(alpha = 0.8f)) // Darker, transparent blue-grey
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp) // More internal padding
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            // Consider externalizing this string to strings.xml
                                            Text(
                                                text = "Gestión del Terrario",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.ExtraBold, // Bolder
                                                color = Color.White // Text color white
                                            )
                                            Spacer(modifier = Modifier.height(6.dp)) // More space
                                            // Consider externalizing this string to strings.xml
                                            Text(
                                                text = "Monitorea y controla tu terrario en tiempo real.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.LightGray // Text color light gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(20.dp)) // More space
                                        // Terrarium photo (ALWAYS FROM DRAWABLE)
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp) // Slightly larger size
                                                .clip(RoundedCornerShape(24.dp)) // Ensures the image is clipped with rounded corners
                                                .background(Color(0xFF81C784).copy(alpha = 0.2f)), // Subtle background that matches the theme
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // IMPORTANT: CHANGE 'R.drawable.ic_baseline_cloud_24' to your REAL terrarium image ID!
                                            // This is a placeholder.
                                            Image(
                                                painter = painterResource(id = R.drawable.terrario), // <-- Your image goes here!
                                                contentDescription = "Imagen del Terrario", // Consider externalizing
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(24.dp)), // Clip image with rounded corners
                                                contentScale = ContentScale.Crop // Scale to fill space
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp)) // More space
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info, // Example icon, you can change it
                                            contentDescription = "Estado de conexión", // Consider externalizing
                                            // Use theme colors for connection status
                                            tint = if (currentMqttConnected) Color(0xFF4CAF50) else Color(0xFFD32F2F), // Green for connected, Red for disconnected
                                            modifier = Modifier.size(28.dp) // Slightly larger icon
                                        )
                                        Spacer(modifier = Modifier.width(10.dp)) // More space
                                        Text(
                                            // Consider externalizing these strings to strings.xml
                                            text = if (currentMqttConnected) "Conectado al ESP32" else "Desconectado del ESP32",
                                            style = MaterialTheme.typography.titleMedium, // More prominent text
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (currentMqttConnected) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                        )
                                    }
                                }
                            }

                            // Prominent Temperature and Humidity Section (Improved)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .animateContentSize(animationSpec = tween(durationMillis = 300)),
                                shape = RoundedCornerShape(24.dp), // Increased rounded corners
                                elevation = CardDefaults.cardElevation(4.dp), // Higher elevation
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3A4B).copy(alpha = 0.7f)) // Darker, transparent blue-grey
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp) // More padding
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = mainTemperature,
                                                style = MaterialTheme.typography.displaySmall, // Larger text size
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White // Text color white
                                            )
                                            Text(
                                                text = "Temperatura Actual", // Consider externalizing
                                                style = MaterialTheme.typography.bodyLarge, // Larger text
                                                color = Color.LightGray // Text color light gray
                                            )
                                        }
                                        Icon(
                                            painter = getSensorIcon("temperature"),
                                            contentDescription = "Temperatura", // Consider externalizing
                                            tint = Color.White, // Icon color white
                                            modifier = Modifier.size(64.dp) // Larger icon
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp)) // Space between main temperature and averages

                                    Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.3f)) // Separator with transparency

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Average Humidity
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                painter = getSensorIcon("humidity"),
                                                contentDescription = "Humedad Promedio", // Consider externalizing
                                                tint = Color.White, // Icon color white
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = averageHumidity,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White // Text color white
                                            )
                                            Text(
                                                text = "Hum. Promedio", // Consider externalizing
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray // Text color light gray
                                            )
                                        }

                                        // Average Temperature (if different from main, or as a second indicator)
                                        // You could decide to show it only if it's significantly different from mainTemperature
                                        // Or simply as a "general average" vs "current"
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                painter = getSensorIcon("temperature"),
                                                contentDescription = "Temperatura Promedio", // Consider externalizing
                                                tint = Color.White, // Icon color white
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = averageTemperature,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White // Text color white
                                            )
                                            Text(
                                                text = "Temp. Promedio", // Consider externalizing
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray // Text color light gray
                                            )
                                        }
                                    }
                                }
                            }

                            // Last Updated Card (New addition for better visibility)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .animateContentSize(animationSpec = tween(durationMillis = 300)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF607D8B).copy(alpha = 0.6f)) // Lighter transparent blue-grey
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_mode_fan_24), // Using a clock icon
                                        contentDescription = "Última Actualización",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Última Actualización: $lastUpdatedText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }


                            // Filter Section
                            Spacer(modifier = Modifier.height(8.dp))
                            // Consider externalizing this string to strings.xml
                            Text(
                                text = "Filtrar por:",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White, // Text color white
                                modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 4.dp)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TerrariumCategory.values().forEach { category ->
                                    CategoryFilterChip(
                                        category = category,
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // --- Sensor Data Display (Refactored) ---
                            // DHT22 Sensors Group
                            AnimatedVisibility(
                                visible = selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.SENSORS || selectedCategory == TerrariumCategory.CLIMATE_WATER,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 300))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Temperatura Hambiente",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    // Cuadrícula 2x2 para los sensores DHT22
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Dht22SensorGroupCard(sensorNumber = 1, sensorData = currentSensorData)
                                            Dht22SensorGroupCard(sensorNumber = 2, sensorData = currentSensorData)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Dht22SensorGroupCard(sensorNumber = 3, sensorData = currentSensorData)
                                            Dht22SensorGroupCard(sensorNumber = 4, sensorData = currentSensorData)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            // DS18B20 Sensors Group y Otros Sensores
                            AnimatedVisibility(
                                visible = selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.SENSORS || selectedCategory == TerrariumCategory.OTHER_SENSORS,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 300))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Temperatura del Suelo",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    Ds18b20SensorGroupCard(sensorData = currentSensorData)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Otros Sensores",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    OtherSensorsGroupCard(sensorData = currentSensorData)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            // Actuator Controls
                            AnimatedVisibility(
                                visible = selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.LIGHTS || selectedCategory == TerrariumCategory.CLIMATE_WATER,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 300))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Controles de Actuadores",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    // Cuadrícula de actuadores en 2 columnas
                                    val actuatorList = currentActuatorStates
                                        .filterKeys { it != "water_pump_active" }
                                        .toList()
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        actuatorList.chunked(2).forEach { rowActuators ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                rowActuators.forEach { (key, isActive) ->
                                                    ActuatorControlCard(
                                                        actuatorKey = key,
                                                        isActive = isActive,
                                                        onToggle = { newState ->
                                                            if (useMockData) {
                                                                mockActuatorStates = mockActuatorStates.toMutableMap().apply {
                                                                    this[key] = newState
                                                                }
                                                            } else {
                                                                currentTerrarium?.let { terrarium ->
                                                                    viewModel.toggleActuator(terrarium.id, key, newState)
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Nueva sección destacada para el dispensador de comida
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .animateContentSize(animationSpec = tween(durationMillis = 300)),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107).copy(alpha = 0.8f)) // Color amarillo para destacar
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    FoodDispenserCard(
                                        isActive = currentActuatorStates["food_dispenser_active"] ?: false,
                                        loads = currentSensorData["food_dispenser_loads"]?.toIntOrNull() ?: 0,
                                        onDispense = { newState ->
                                            if (useMockData) {
                                                mockActuatorStates = mockActuatorStates.toMutableMap().apply {
                                                    this["food_dispenser_active"] = newState
                                                }
                                            } else {
                                                currentTerrarium?.let { terrarium ->
                                                    viewModel.toggleActuator(terrarium.id, "food_dispenser_active", newState)
                                                }
                                            }
                                        },
                                        onRecharge = {
                                            // Aquí puedes poner la lógica para recargar el dispensador.
                                            // Por ejemplo, podrías mostrar un diálogo, enviar un comando, etc.
                                            // Si no tienes lógica aún, puedes dejarlo vacío.
                                        }
                                    )
                                }
                            }

                            // Sistema de Lluvia (Rain System)
                            AnimatedVisibility(
                                visible = selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.RAIN_SYSTEM,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 300))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Sistema de Lluvia",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    RainSystemCard(
                                        isActive = currentActuatorStates["water_pump_active"] ?: false,
                                        waterDistance = currentSensorData["hc_sr04_1_distance"]?.replace(" cm", "")?.toFloatOrNull(),
                                        onToggle = { newState ->
                                            if (useMockData) {
                                                mockActuatorStates = mockActuatorStates.toMutableMap().apply {
                                                    this["water_pump_active"] = newState
                                                }
                                            } else {
                                                currentTerrarium?.let { terrarium ->
                                                    viewModel.toggleActuator(terrarium.id, "water_pump_active", newState)
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// Mejoras en los filtros: iconos y colores por categoría
@Composable
fun CategoryFilterChip(
    category: TerrariumCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon: Painter = when (category) {
        TerrariumCategory.ALL -> painterResource(id = R.drawable.ic_baseline_cloud_24)
        TerrariumCategory.SENSORS -> painterResource(id = R.drawable.ic_baseline_thermostat_24)
        TerrariumCategory.LIGHTS -> painterResource(id = R.drawable.baseline_lightbulb_24)
        TerrariumCategory.CLIMATE_WATER -> painterResource(id = R.drawable.ic_outline_water_pump_24)
        TerrariumCategory.RAIN_SYSTEM -> painterResource(id = R.drawable.ic_outline_water_pump_24) // Usa el mismo ícono o uno diferente si tienes
        TerrariumCategory.OTHER_SENSORS -> painterResource(id = R.drawable.ic_outline_settings_power_24)
    }
    val chipColor = when (category) {
        TerrariumCategory.ALL -> Color(0xFF607D8B)
        TerrariumCategory.SENSORS -> Color(0xFF90CAF9)
        TerrariumCategory.LIGHTS -> Color(0xFFFFF176)
        TerrariumCategory.CLIMATE_WATER -> Color(0xFF80DEEA)
        TerrariumCategory.RAIN_SYSTEM -> Color(0xFF4FC3F7) // Azul para el sistema de lluvia
        TerrariumCategory.OTHER_SENSORS -> Color(0xFFA5D6A7)
    }

    // Animaciones para el borde y sombra
    val borderWidth by animateDpAsState(targetValue = if (selected) 3.dp else 1.dp)
    val borderColor by animateColorAsState(targetValue = if (selected) chipColor else chipColor.copy(alpha = 0.5f))
    val shadowElevation by animateDpAsState(targetValue = if (selected) 8.dp else 0.dp)
    val backgroundColor by animateColorAsState(targetValue = if (selected) chipColor.copy(alpha = 0.95f) else chipColor.copy(alpha = 0.7f))

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(category.displayName, color = Color.White)
            }
        },
        modifier = Modifier
            .shadow(shadowElevation, RoundedCornerShape(50))
            .border(borderWidth, borderColor, RoundedCornerShape(50)),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = backgroundColor,
            selectedLabelColor = Color.White,
            containerColor = backgroundColor,
            labelColor = Color.White
        ),
        border = null // El borde se maneja por el modifier
    )
}


// Helper function to get health color based on value and type
@Composable
fun getHealthColor(value: Float?, sensorType: String): Color {
    if (value == null) return Color.LightGray.copy(alpha = 0.6f) // For N/A values

    return when (sensorType) {
        "temperature" -> {
            when {
                value < 20.0f -> Color(0xFFADD8E6) // Light Blue (Cold)
                value > 30.0f -> Color(0xFFFFA07A) // Light Salmon (Hot)
                else -> Color(0xFF90EE90) // Light Green (Normal)
            }
        }
        "humidity" -> {
            when {
                value < 50.0f -> Color(0xFFFFA07A) // Light Salmon (Dry)
                value > 80.0f -> Color(0xFFADD8E6) // Light Blue (Humid)
                else -> Color(0xFF90EE90) // Light Green (Normal)
            }
        }
        "distance" -> {
            when {
                value < 10.0f -> Color(0xFFFFA07A) // Light Salmon (Too close)
                value > 50.0f -> Color(0xFFADD8E6) // Light Blue (Too far)
                else -> Color(0xFF90EE90) // Light Green (Normal)
            }
        }
        "power" -> {
            when {
                value > 100.0f -> Color(0xFFFFA07A) // Light Salmon (High power)
                else -> Color(0xFF90EE90) // Light Green (Normal)
            }
        }
        else -> Color.White // Default
    }
}


@Composable
fun getSensorIcon(sensorType: String): Painter {
    return when (sensorType) {
        "temperature" -> painterResource(id = R.drawable.ic_baseline_thermostat_24) // Placeholder, replace with actual icon
        "humidity" -> painterResource(id = R.drawable.ic_outline_humidity_high_24) // Placeholder, replace with actual icon
        "distance" -> painterResource(id = R.drawable.ic_outline_distance_24) // Placeholder, replace with actual icon
        "power" -> painterResource(id = R.drawable.ic_outline_settings_power_24) // Placeholder, replace with actual icon
        else -> painterResource(id = R.drawable.ic_baseline_cloud_24) // Generic placeholder
    }
}

// Helper function to determine if it's day or night (adjust hours as needed)
fun isCurrentlyDayTime(): Boolean {
    val currentHour = LocalTime.now().hour
    return currentHour in 6..19 // Between 6 AM and 7 PM
}

// --- Mock Data for Previews ---
fun createMockTerrarium(id: String): Terrarium {
    return Terrarium(
        id = id,
        name = "Terrario de Prueba",
        description = "Este es un terrario de prueba para desarrollo de UI.",
        dht22_1_temperature = 25.0f,
        dht22_1_humidity = 70.0f,
        dht22_2_temperature = 26.5f,
        dht22_2_humidity = 65.0f,
        dht22_3_temperature = 19.0f, // Example of low temp
        dht22_3_humidity = 85.0f, // Example of high humidity
        dht22_4_temperature = 32.0f, // Example of high temp
        dht22_4_humidity = 45.0f, // Example of low humidity
        ds18b20_1_temperature = 23.0f,
        ds18b20_2_temperature = 22.5f,
        ds18b20_3_temperature = 24.5f,
        ds18b20_4_temperature = 23.8f,
        ds18b20_5_temperature = 22.0f,
        hc_sr04_1_distance = 5.0f, // Example of low distance
        pzem_1_power = 120.0f, // Example of high power
        // All actuators OFF at start for mock
        waterPumpActive = false,
        fan1Active = false,
        fan2Active = false,
        light1Active = false,
        light2Active = false,
        light3Active = false,
        heatPlate1Active = false,
        lastUpdated = System.currentTimeMillis()
    )
}

fun createMockSensorData(): Map<String, String> {
    return mapOf(
        "dht22_1_temperature" to "25.0°C",
        "dht22_1_humidity" to "70.0%",
        "dht22_2_temperature" to "26.5°C",
        "dht22_2_humidity" to "65.0%",
        "dht22_3_temperature" to "19.0°C", // Example of low temp
        "dht22_3_humidity" to "85.0%", // Example of high humidity
        "dht22_4_temperature" to "32.0°C", // Example of high temp
        "dht22_4_humidity" to "45.0%", // Example of low humidity
        "ds18b20_1_temperature" to "23.0°C",
        "ds18b20_2_temperature" to "22.5°C",
        "ds18b20_3_temperature" to "24.5°C",
        "ds18b20_4_temperature" to "23.8°C",
        "ds18b20_5_temperature" to "22.0°C",
        "hc_sr04_1_distance" to "5.0 cm", // Example of low distance
        "pzem_1_power" to "120.0 W", // Example of high power
        "lastUpdated" to "Hace 1 minuto" // More descriptive mock timestamp
    )
}

fun createMockActuatorStates(): Map<String, Boolean> {
    // All actuators OFF at start for mock
    return mapOf(
        "water_pump_active" to false,
        "fan1_active" to false,
        "fan2_active" to false,
        "light1_active" to false,
        "light2_active" to false,
        "light3_active" to false,
        "heat_plate1_active" to false
    )
}


@Preview(showBackground = true)
@Composable
fun TerrariumDetailScreenPreview() {
    ReptiTrackTheme {
        // Use a test terrarium ID to activate mock mode in the Preview
        TerrariumDetailScreen(terrariumId = "placeholder_terrarium_id", onBackClick = {})
    }
}
