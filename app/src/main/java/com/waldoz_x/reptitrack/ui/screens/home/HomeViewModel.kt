package com.waldoz_x.reptitrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.source.remote.HiveMqttClient
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.domain.usecase.GetAllTerrariumsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllTerrariumsUseCase: GetAllTerrariumsUseCase,
    private val hiveMqttClient: HiveMqttClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    val mqttConnectionState: StateFlow<Boolean> = hiveMqttClient.isConnected
    val mqttReceivedMessages: StateFlow<Pair<String, String>?> = hiveMqttClient.receivedMessages

    init {
        loadTerrariums()
        viewModelScope.launch {
            hiveMqttClient.connect()
            // Suscribirse a un tópico general para todos los terrarios o a tópicos específicos
            // Ejemplo: "terrariums/data/#" para recibir datos de todos los terrarios
            hiveMqttClient.subscribeToTopic("terrariums/data/#") // Suscripción a todos los datos de terrarios
        }
    }

    fun loadTerrariums() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                getAllTerrariumsUseCase().collectLatest { terrariums ->
                    _uiState.value = HomeUiState.Success(terrariums)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Error desconocido al cargar terrarios")
            }
        }
    }

    fun publishMqttCommand(topic: String, message: String) {
        viewModelScope.launch {
            hiveMqttClient.publishMessage(topic, message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            hiveMqttClient.disconnect()
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val terrariums: List<Terrarium>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
