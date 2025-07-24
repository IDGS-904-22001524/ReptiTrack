package com.waldoz_x.reptitrack.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

// Composable de ruta para la pantalla de inicio.
// Este Composable es el que se llama desde AppNavHost.
@Composable
fun HomeRoutes( // Mantengo HomeRoute, asumiendo que ya resolviste el conflicto de nombres
    navigateToTerrariumDetail: (String) -> Unit, // Callback para navegar al detalle del terrario
    navigateToSettings: () -> Unit, // ¡NUEVO! Callback para navegar a ajustes
    onAddTerrarium: () -> Unit, // CAMBIA ESTE NOMBRE
    viewModel: HomeViewModel = hiltViewModel() // Inyecta el HomeViewModel
) {
    // Recoge el estado de la UI del ViewModel como un State
    val uiState by viewModel.uiState.collectAsState()
    val isMqttConnected by viewModel.mqttConnectionState.collectAsState()
    val receivedMqttMessage by viewModel.mqttReceivedMessages.collectAsState()

    // Llama al Composable principal de la pantalla, pasando el estado y los callbacks
    HomeScreen(
        uiState = uiState,
        onTerrariumClick = navigateToTerrariumDetail,
        onRetryClick = viewModel::loadTerrariums,
        isMqttConnected = isMqttConnected,
        onSettingsClick = navigateToSettings, // ¡NUEVO! Pasa el callback de ajustes
        onAddTerrariumClick = onAddTerrarium
    )
}
