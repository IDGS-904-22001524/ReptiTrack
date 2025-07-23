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

// Definition of filter categories
enum class TerrariumCategory(val displayName: String) {
    ALL("Todo"),
    SENSORS("Sensores"),
    LIGHTS("Iluminación"),
    CLIMATE_WATER("Clima y Agua"),
    OTHER_SENSORS("Otros Sensores")
}

// This is the terrarium detail screen
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TerrariumDetailScreen(
    terrariumId: String, // Receives the terrarium ID
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TerrariumDetailViewModel = hiltViewModel() // Inject the ViewModel
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
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category },
                                        label = { Text(category.displayName, color = if (selectedCategory == category) Color.White else Color.LightGray) }, // Text color adjusted
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.8f), // Green for selected with transparency
                                            selectedLabelColor = Color.White, // White text for selected
                                            containerColor = Color(0xFF2D3A4B).copy(alpha = 0.7f), // Darker, transparent blue-grey
                                            labelColor = Color.LightGray // Text color for unselected
                                        ),
                                        border = if (selectedCategory == category) null else BorderStroke(
                                            width = 1.dp,
                                            color = Color(0xFF4CAF50).copy(alpha = 0.4f) // More subtle green border
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Dynamic sections based on filter
                            if (selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.SENSORS) {
                                TerrariumSection(title = "Sensores de Temperatura y Humedad") { // Consider externalizing
                                    // Consider externalizing this string to strings.xml
                                    Text(
                                        text = "Monitoreo de ambiente interno por zonas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray, // Text color light gray
                                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        maxItemsInEachRow = 2
                                    ) {
                                        SensorCard(label = getSensorDisplayName("dht22_1_temperature"), value = currentSensorData["dht22_1_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("dht22_1_humidity"), value = currentSensorData["dht22_1_humidity"] ?: "N/A", icon = getSensorIcon("humidity"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("dht22_2_temperature"), value = currentSensorData["dht22_2_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("dht22_2_humidity"), value = currentSensorData["dht22_2_humidity"] ?: "N/A", icon = getSensorIcon("humidity"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("dht22_3_temperature"), value = currentSensorData["dht22_3_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("dht22_3_humidity"), value = currentSensorData["dht22_3_humidity"] ?: "N/A", icon = getSensorIcon("humidity"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("dht22_4_temperature"), value = currentSensorData["dht22_4_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("dht22_4_humidity"), value = currentSensorData["dht22_4_humidity"] ?: "N/A", icon = getSensorIcon("humidity"), modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.SENSORS) {
                                TerrariumSection(title = "Sensores de Temperatura del Suelo") { // Consider externalizing
                                    // Consider externalizing this string to strings.xml
                                    Text(
                                        text = "Lecturas de temperatura en diferentes puntos del sustrato.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray, // Text color light gray
                                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        maxItemsInEachRow = 2
                                    ) {
                                        SensorCard(label = getSensorDisplayName("ds18b20_1_temperature"), value = currentSensorData["ds18b20_1_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("ds18b20_2_temperature"), value = currentSensorData["ds18b20_2_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("ds18b20_3_temperature"), value = currentSensorData["ds18b20_3_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("ds18b20_4_temperature"), value = currentSensorData["ds18b20_4_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("ds18b20_5_temperature"), value = currentSensorData["ds18b20_5_temperature"] ?: "N/A", icon = getSensorIcon("temperature"), modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.OTHER_SENSORS) {
                                TerrariumSection(title = "Otros Sensores") { // Consider externalizing
                                    // Consider externalizing this string to strings.xml
                                    Text(
                                        text = "Información adicional del entorno del terrario.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray, // Text color light gray
                                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        maxItemsInEachRow = 2
                                    ) {
                                        SensorCard(label = getSensorDisplayName("hc_sr04_1_distance"), value = currentSensorData["hc_sr04_1_distance"] ?: "N/A", icon = getSensorIcon("distance"), modifier = Modifier.weight(1f))
                                        SensorCard(label = getSensorDisplayName("pzem_1_power"), value = currentSensorData["pzem_1_power"] ?: "N/A", icon = getSensorIcon("power"), modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.LIGHTS) {
                                TerrariumSection(title = "Actuadores de Iluminación") { // Consider externalizing
                                    // Consider externalizing this string to strings.xml
                                    Text(
                                        text = "Control de las fuentes de luz del terrario.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray, // Text color light gray
                                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        maxItemsInEachRow = 2
                                    ) {
                                        ActuatorCard(
                                            label = getActuatorDisplayName("light1_active"),
                                            isActive = currentActuatorStates["light1_active"] ?: false,
                                            onToggle = { newState ->
                                                if (useMockData) {
                                                    mockActuatorStates = mockActuatorStates.toMutableMap().apply { this["light1_active"] = newState }
                                                } else {
                                                    viewModel.toggleActuator(terrariumId, "light1_active", newState)
                                                }
                                            },
                                            icon = getActuatorIcon("light"),
                                            modifier = Modifier.weight(1f)
                                        )
                                        ActuatorCard(
                                            label = getActuatorDisplayName("light2_active"),
                                            isActive = currentActuatorStates["light2_active"] ?: false,
                                            onToggle = { newState ->
                                                if (useMockData) {
                                                    mockActuatorStates = mockActuatorStates.toMutableMap().apply { this["light2_active"] = newState }
                                                } else {
                                                    viewModel.toggleActuator(terrariumId, "light2_active", newState)
                                                }
                                            },
                                            icon = getActuatorIcon("light"),
                                            modifier = Modifier.weight(1f)
                                        )
                                        ActuatorCard(
                                            label = getActuatorDisplayName("light3_active"),
                                            isActive = currentActuatorStates["light3_active"] ?: false,
                                            onToggle = { newState ->
                                                if (useMockData) {
                                                    mockActuatorStates = mockActuatorStates.toMutableMap().apply { this["light3_active"] = newState }
                                                } else {
                                                    viewModel.toggleActuator(terrariumId, "light3_active", newState)
                                                }
                                            },
                                            icon = getActuatorIcon("light"),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (selectedCategory == TerrariumCategory.ALL || selectedCategory == TerrariumCategory.CLIMATE_WATER) {
                                TerrariumSection(title = "Actuadores de Clima y Agua") { // Consider externalizing
                                    // Consider externalizing this string to strings.xml
                                    Text(
                                        text = "Control de las condiciones ambientales y suministro de agua.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray, // Text color light gray
                                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        maxItemsInEachRow = 2
                                    ) {
                                        ActuatorCard(
                                            label = getActuatorDisplayName("water_pump_active"),
                                            isActive = currentActuatorStates["water_pump_active"] ?: false,
                                            onToggle = { newState ->
                                                if (useMockData) {
                                                    mockActuatorStates = mockActuatorStates.toMutableMap().apply { this["water_pump_active"] = newState }
                                                } else {
                                                    viewModel.toggleActuator(terrariumId, "water_pump_active", newState)
                                                }
                                            },
                                            icon = getActuatorIcon("water_pump"),
                                            modifier = Modifier.weight(1f)
                                        )
                                        ActuatorCard(
                                            label = getActuatorDisplayName("fan1_active"),
                                            isActive = currentActuatorStates["fan1_active"] ?: false,
                                            onToggle = { newState ->
                                                if (useMockData) {
                                                    mockActuatorStates = mockActuatorStates.toMutableMap().apply { this["fan1_active"] = newState }
                                                } else {
                                                    viewModel.toggleActuator(terrariumId, "fan1_active", newState)
                                                }
                                            },
                                            icon = getActuatorIcon("fan"),
                                            modifier = Modifier.weight(1f)
                                        )
                                        ActuatorCard(
                                            label = getActuatorDisplayName("fan2_active"),
                                            isActive = currentActuatorStates["fan2_active"] ?: false,
                                            onToggle = { newState ->
                                                if (useMockData) {
                                                    mockActuatorStates = mockActuatorStates.toMutableMap().apply { this["fan2_active"] = newState }
                                                } else {
                                                    viewModel.toggleActuator(terrariumId, "fan2_active", newState)
                                                }
                                            },
                                            icon = getActuatorIcon("fan"),
                                            modifier = Modifier.weight(1f)
                                        )
                                        ActuatorCard(
                                            label = getActuatorDisplayName("heat_plate1_active"),
                                            isActive = currentActuatorStates["heat_plate1_active"] ?: false,
                                            onToggle = { newState ->
                                                if (useMockData) {
                                                    mockActuatorStates = mockActuatorStates.toMutableMap().apply { this["heat_plate1_active"] = newState }
                                                } else {
                                                    viewModel.toggleActuator(terrariumId, "heat_plate1_active", newState)
                                                }
                                            },
                                            icon = getActuatorIcon("heat_plate"),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable for the sensor card
@Composable
fun SensorCard(label: String, value: String, icon: Painter, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp) // Fixed height for grid consistency
            .animateContentSize(animationSpec = tween(durationMillis = 300)),
        shape = RoundedCornerShape(24.dp), // Increased rounded corners
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3A4B).copy(alpha = 0.7f)) // Darker, transparent blue-grey
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = label, // Consider externalizing
                tint = Color.White, // Icon color white
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label, // Consider externalizing
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = Color.LightGray, // Text color light gray
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (value == "N/A") Color.LightGray.copy(alpha = 0.6f) else Color.White, // Text color white, light gray for N/A
                textAlign = TextAlign.Center
            )
        }
    }
}

// Composable for the actuator card
@Composable
fun ActuatorCard(label: String, isActive: Boolean, onToggle: (Boolean) -> Unit, icon: Painter, modifier: Modifier = Modifier) {
    val cardBackgroundColor by animateColorAsState(
        targetValue = when {
            label == "Bomba Agua" && isActive -> Color(0xFF2196F3).copy(alpha = 0.9f) // Blue for active Water Pump with higher opacity
            label == "Luz Día" && isActive -> Color(0xFFFF9800).copy(alpha = 0.9f) // Orange for active Day Light with higher opacity
            label == "Luz Noche" && isActive -> Color(0xFF3F51B5).copy(alpha = 0.9f) // Dark blue for active Night Light with higher opacity
            label == "Luz UV" && isActive -> Color(0xFF9C27B0).copy(alpha = 0.9f) // Purple for active UV Light with higher opacity
            label == "Placa Calor" && isActive -> Color(0xFFF44336).copy(alpha = 0.9f) // Red for active Heat Plate with higher opacity
            isActive -> Color(0xFF81C784).copy(alpha = 0.9f) // Light green for other active ones (e.g., Fans) with higher opacity
            else -> Color(0xFF2D3A4B).copy(alpha = 0.7f) // Darker, transparent blue-grey for inactive
        },
        animationSpec = tween(durationMillis = 300)
    )
    val contentColor by animateColorAsState(
        targetValue = Color.White, // White text for all states
        animationSpec = tween(durationMillis = 300)
    )
    val switchTrackColor by animateColorAsState(
        targetValue = if (isActive) cardBackgroundColor else Color(0xFF616161), // Use card background when active, grey when inactive
        animationSpec = tween(durationMillis = 300)
    )
    val switchThumbColor by animateColorAsState(
        targetValue = Color.Black, // Changed thumb color to black for both states
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp) // Fixed height for grid consistency
            .animateContentSize(animationSpec = tween(durationMillis = 300)),
        shape = RoundedCornerShape(24.dp), // Increased rounded corners
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Box( // Use Box for more flexible positioning
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // General card padding
            contentAlignment = Alignment.Center // Center content within the Box
        ) {
            // Icon in the top-left corner
            Icon(
                painter = icon,
                contentDescription = label, // Consider externalizing
                tint = contentColor,
                modifier = Modifier
                    .size(48.dp) // Larger icon
                    .align(Alignment.TopStart) // Aligned top-left
            )

            // Text content (label and state) in the bottom-left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart) // Aligned bottom-left
                    .fillMaxWidth(0.7f) // Occupy 70% of the width to leave space for the switch
            ) {
                Text(
                    text = label, // Consider externalizing
                    style = MaterialTheme.typography.titleMedium, // Larger and more prominent text
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Consider externalizing these strings to strings.xml
                Text(
                    text = if (isActive) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f) // More subtle color for the state
                )
            }

            // Switch in the top-right corner
            Switch(
                checked = isActive,
                onCheckedChange = onToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd) // Aligned top-right
                    .size(56.dp), // Larger size for the switch
                colors = SwitchDefaults.colors(
                    checkedThumbColor = switchThumbColor,
                    checkedTrackColor = switchTrackColor,
                    uncheckedThumbColor = switchThumbColor,
                    uncheckedTrackColor = switchTrackColor
                )
            )
        }
    }
}

// Composable for sections
@Composable
fun TerrariumSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Text(
            text = title, // Consider externalizing
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White, // Text color white
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        content()
    }
}

// Helper functions to get user-friendly display names
@Composable
fun getSensorDisplayName(key: String): String {
    // Consider externalizing all these strings to strings.xml
    return when (key) {
        "dht22_1_temperature" -> "Temp. Ambiente 1"
        "dht22_1_humidity" -> "Hum. Ambiente 1"
        "dht22_2_temperature" -> "Temp. Ambiente 2"
        "dht22_2_humidity" -> "Hum. Ambiente 2"
        "dht22_3_temperature" -> "Temp. Ambiente 3"
        "dht22_3_humidity" -> "Hum. Ambiente 3"
        "dht22_4_temperature" -> "Temp. Ambiente 4"
        "dht22_4_humidity" -> "Hum. Ambiente 4"
        "ds18b20_1_temperature" -> "Temp. Suelo 1"
        "ds18b20_2_temperature" -> "Temp. Suelo 2"
        "ds18b20_3_temperature" -> "Temp. Suelo 3"
        "ds18b20_4_temperature" -> "Temp. Suelo 4"
        "ds18b20_5_temperature" -> "Temp. Suelo 5"
        "hc_sr04_1_distance" -> "Nivel Agua"
        "pzem_1_power" -> "Consumo Eléctrico"
        else -> key.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

@Composable
fun getActuatorDisplayName(key: String): String {
    // Consider externalizing all these strings to strings.xml
    return when (key) {
        "water_pump_active" -> "Bomba Agua"
        "fan1_active" -> "Ventilador 1"
        "fan2_active" -> "Ventilador 2"
        "light1_active" -> "Luz Día"
        "light2_active" -> "Luz Noche"
        "light3_active" -> "Luz UV"
        "heat_plate1_active" -> "Placa Calor"
        else -> key.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}


// Helper functions to get icons based on sensor/actuator key
@Composable
fun getSensorIcon(key: String): Painter {
    // IMPORTANT! Consider adding more specific icons for each sensor type
    // For example, a thermometer icon for temperature, a drop for humidity, etc.
    return when {
        key.contains("temperature") -> painterResource(id = R.drawable.ic_baseline_cloud_24) // Placeholder, look for a thermometer icon
        key.contains("humidity") -> painterResource(id = R.drawable.ic_outline_humidity_high_24)
        key.contains("distance") -> painterResource(id = R.drawable.ic_outline_sensors_24)
        key.contains("power") -> painterResource(id = R.drawable.ic_outline_settings_power_24)
        else -> painterResource(id = R.drawable.ic_baseline_info_24)
    }
}

@Composable
fun getActuatorIcon(key: String): Painter {
    // IMPORTANT! Consider adding more specific icons for each actuator type
    // For example, a lightbulb icon for light, a fan for fan, etc.
    return when {
        key.contains("light") -> painterResource(id = R.drawable.baseline_lightbulb_24)
        key.contains("fan") -> painterResource(id = R.drawable.outline_mode_fan_24)
        key.contains("water_pump") -> painterResource(id = R.drawable.ic_outline_water_pump_24)
        key.contains("heat_plate") -> painterResource(id = R.drawable.ic_outline_heat_24)
        else -> painterResource(id = R.drawable.ic_baseline_info_24)
    }
}

// --- Functions to generate test data (Mock Data) ---
fun createMockTerrarium(id: String): Terrarium {
    return Terrarium(
        id = id,
        name = "Terrario de Prueba",
        description = "Este es un terrario de prueba para desarrollo de UI.",
        dht22_1_temperature = 25.0f,
        dht22_1_humidity = 70.0f,
        dht22_2_temperature = 26.5f,
        dht22_2_humidity = 65.0f,
        dht22_3_temperature = 24.0f,
        dht22_3_humidity = 72.0f,
        dht22_4_temperature = 25.5f,
        dht22_4_humidity = 68.0f,
        ds18b20_1_temperature = 23.0f,
        ds18b20_2_temperature = 22.5f,
        ds18b20_3_temperature = 24.5f,
        ds18b20_4_temperature = 23.8f,
        ds18b20_5_temperature = 22.0f,
        hc_sr04_1_distance = 15.0f,
        pzem_1_power = 50.5f,
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
        "dht22_3_temperature" to "24.0°C",
        "dht22_3_humidity" to "72.0%",
        "dht22_4_temperature" to "25.5°C",
        "dht22_4_humidity" to "68.0%",
        "ds18b20_1_temperature" to "23.0°C",
        "ds18b20_2_temperature" to "22.5°C",
        "ds18b20_3_temperature" to "24.5°C",
        "ds18b20_4_temperature" to "23.8°C",
        "ds18b20_5_temperature" to "22.0°C",
        "hc_sr04_1_distance" to "15.0 cm",
        "pzem_1_power" to "50.5 W",
        "lastUpdated" to "Ahora (mock)"
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

// Function to determine if it's daytime or nighttime
fun isCurrentlyDayTime(): Boolean {
    val currentHour = LocalTime.now().hour
    // We consider "day" from 6 AM (inclusive) to 6 PM (exclusive)
    return currentHour >= 6 && currentHour < 18
}


@Preview(showBackground = true)
@Composable
fun TerrariumDetailScreenPreview() {
    ReptiTrackTheme {
        // Use a test terrarium ID to activate mock mode in the Preview
        TerrariumDetailScreen(terrariumId = "placeholder_terrarium_id", onBackClick = {})
    }
}
