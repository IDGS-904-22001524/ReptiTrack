package com.waldoz_x.reptitrack.ui.screens.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
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
import com.waldoz_x.reptitrack.R
import com.waldoz_x.reptitrack.ui.components.TerrariumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    navigateToTerrariumDetail: (String) -> Unit,
    navigateToSettings: () -> Unit, // Callback para navegar a ajustes
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isMqttConnected by viewModel.mqttConnectionState.collectAsState()
    val isFirebaseConnected by viewModel.firebaseConnectionState.collectAsState() // NUEVO: Recoge el estado de Firebase
    val currentUserData by viewModel.currentUserData.collectAsState() // NUEVO: Recoge los datos del usuario

    HomeScreen(
        uiState = uiState,
        onTerrariumClick = navigateToTerrariumDetail,
        onRetryClick = viewModel::loadTerrariums,
        isMqttConnected = isMqttConnected,
        isFirebaseConnected = isFirebaseConnected, // NUEVO: Pasa el estado de Firebase
        currentUserData = currentUserData, // NUEVO: Pasa los datos del usuario
        onSettingsClick = navigateToSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTerrariumClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    isMqttConnected: Boolean,
    isFirebaseConnected: Boolean, // NUEVO: Parámetro para el estado de Firebase
    currentUserData: UserData?, // NUEVO: Parámetro para los datos del usuario
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bienvenido a ReptilTrack", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.onPrimary)
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
                painter = painterResource(id = R.drawable.jungle_background),
                contentDescription = "Fondo de jungla para terrarios",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
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
                    .padding(paddingValues)
            ) {
                // Título principal "ReptilTrack" y "X dispositivos"
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "ReptilTrack",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    val numberOfDevices = if (uiState is HomeUiState.Success) {
                        uiState.terrariums.size
                    } else {
                        0
                    }
                    Text(
                        text = "$numberOfDevices dispositivos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Sección de información del usuario
                currentUserData?.let { userData ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(
                            text = "Usuario: ${if (userData.isGuest) "Invitado (${userData.id.take(6)}...)" else userData.email ?: userData.id}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Sección de estado de MQTT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp), // Reducido el padding vertical
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

                // Sección de estado de Firebase
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp), // Reducido el padding vertical
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Firebase Conectado: ",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (isFirebaseConnected) "Sí" else "No",
                        color = if (isFirebaseConnected) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
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
                                        onClick = onTerrariumClick
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
