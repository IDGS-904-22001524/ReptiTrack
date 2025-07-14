// com.waldoz_x.reptitrack.ui.screens.home/HomeScreen.kt
package com.waldoz_x.reptitrack.ui.screens.home

import android.util.Log // Importación necesaria para Log.d
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.R // Asegúrate de que R esté disponible para drawables
import com.waldoz_x.reptitrack.ui.components.TerrariumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    navigateToTerrariumDetail: (String) -> Unit, // Callback para navegar al detalle del terrario
    viewModel: HomeViewModel = hiltViewModel() // Inyecta el HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isMqttConnected by viewModel.mqttConnectionState.collectAsState()
    val receivedMqttMessage by viewModel.mqttReceivedMessages.collectAsState()

    HomeScreen(
        uiState = uiState,
        onTerrariumClick = navigateToTerrariumDetail,
        onRetryClick = viewModel::loadTerrariums,
        isMqttConnected = isMqttConnected,
        receivedMqttMessage = receivedMqttMessage,
        onPublishCommand = viewModel::publishMqttCommand
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTerrariumClick: (String) -> Unit, // Callback para cuando se hace clic en un terrario
    onRetryClick: () -> Unit,
    isMqttConnected: Boolean,
    receivedMqttMessage: Pair<String, String>?,
    onPublishCommand: (String, String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bienvenido a ReptilTrack", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), // Fondo transparente
                actions = {
                    IconButton(onClick = { /* TODO: Acción para añadir un terrario */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir Terrario", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Acción para añadir un terrario */ }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Terrario")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo de imagen
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Fondo de jungla para terrarios",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Escala la imagen para cubrir el Box
            )
            // Capa de superposición para mejorar la legibilidad del texto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                            startY = 0f,
                            endY = 300f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Aplica el padding de la barra superior
            ) {
                // Título principal y "X dispositivos"
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "ReptilTrack",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White // Texto blanco para contraste con el fondo oscuro
                    )
                    // CORREGIDO: Acceso seguro al tamaño de la lista de terrarios
                    val numberOfDevices = if (uiState is HomeUiState.Success) {
                        uiState.terrariums.size
                    } else {
                        0 // O cualquier valor por defecto cuando no está en estado Success
                    }
                    Text(
                        text = "$numberOfDevices dispositivos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Sección de estado de MQTT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MQTT Conectado: ",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (isMqttConnected) "Sí" else "No",
                        color = if (isMqttConnected) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                receivedMqttMessage?.let { (topic, message) ->
                    Text(
                        text = "Último mensaje: $topic -> $message",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Dispositivos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when (uiState) {
                    is HomeUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is HomeUiState.Success -> {
                        if (uiState.terrariums.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay terrarios registrados. Toca '+' para añadir uno.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(uiState.terrariums) { terrarium ->
                                    TerrariumCard(
                                        terrarium = terrarium,
                                        onClick = { /* No hay navegación de detalle por ahora, solo log */
                                            Log.d("HomeScreen", "Clicked on terrarium: ${terrarium.name}")
                                            // onTerrariumClick(terrarium.id) // Descomentar cuando la pantalla de detalle esté lista
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is HomeUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Error al cargar terrarios: ${uiState.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Button(onClick = onRetryClick) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
