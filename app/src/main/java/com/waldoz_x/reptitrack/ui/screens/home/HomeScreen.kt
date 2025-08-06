package com.waldoz_x.reptitrack.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waldoz_x.reptitrack.R
import com.waldoz_x.reptitrack.domain.model.Terrarium
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.waldoz_x.reptitrack.ui.components.TerrariumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    navigateToTerrariumDetail: (String) -> Unit,
    navigateToSettings: () -> Unit,
    onAddTerrarium: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isMqttConnected by viewModel.mqttConnectionState.collectAsState()
    val isFirebaseConnected by viewModel.firebaseConnectionState.collectAsState()
    val currentUserData by viewModel.currentUserData.collectAsState()

    HomeScreen(
        uiState = uiState,
        onTerrariumClick = navigateToTerrariumDetail,
        onRetryClick = { viewModel.loadTerrariums() },
        isMqttConnected = isMqttConnected,
        isFirebaseConnected = isFirebaseConnected,
        currentUserData = currentUserData,
        onSettingsClick = navigateToSettings,
        onAddTerrariumClick = onAddTerrarium
    )
}

@Composable
fun AddTerrariumCard(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // Hace la tarjeta cuadrada
            .clickable { onAddClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir Terrario",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Añadir Terrario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTerrariumClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    isMqttConnected: Boolean,
    isFirebaseConnected: Boolean,
    currentUserData: UserData?,
    onSettingsClick: () -> Unit,
    onAddTerrariumClick: () -> Unit
) {
    var showAddTerrariumDialog by remember { mutableStateOf(false) }
    var newTerrariumName by remember { mutableStateOf("") }
    val viewModel = hiltViewModel<HomeViewModel>()

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
            FloatingActionButton(onClick = onAddTerrariumClick) {
                Icon(
                    painterResource(id = R.drawable.ic_outline_settings_power_24),
                    contentDescription = "Configurar ESP32"
                )
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
                        .padding(horizontal = 16.dp, vertical = 4.dp),
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
                        .padding(horizontal = 16.dp, vertical = 4.dp),
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
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    maxItemsInEachRow = 2
                                ) {
                                    uiState.terrariums.forEach { terrarium ->
                                        TerrariumCard(
                                            terrarium = terrarium, // <-- Aquí pasa la instancia, no la clase
                                            onClick = onTerrariumClick,
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .aspectRatio(1f)
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                    // La tarjeta de añadir terrario aparece siempre al final
                                    AddTerrariumCard(
                                        onAddClick = { showAddTerrariumDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth(0.5f) // Siempre ocupa la mitad del ancho
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
                            if (uiState.terrariums.isEmpty()) {
                                item {
                                    Text(
                                        "No hay terrarios registrados. Toca '+' para añadir uno.",
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
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

        if (showAddTerrariumDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddTerrariumDialog = false
                    newTerrariumName = ""
                },
                title = { Text("Añadir nuevo terrario") },
                text = {
                    OutlinedTextField(
                        value = newTerrariumName,
                        onValueChange = { newTerrariumName = it },
                        label = { Text("Nombre del terrario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTerrariumName.isNotBlank()) {
                                viewModel.createTerrarium(newTerrariumName)
                                showAddTerrariumDialog = false
                                newTerrariumName = ""
                            }
                        }
                    ) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showAddTerrariumDialog = false
                            newTerrariumName = ""
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
