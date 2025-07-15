package com.waldoz_x.reptitrack.ui.screens.mqtt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.preferences.UserPreferencesRepository
import com.waldoz_x.reptitrack.data.source.remote.HiveMqttClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MqttSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val hiveMqttClient: HiveMqttClient
) : ViewModel() {

    val username: StateFlow<String?> = userPreferencesRepository.mqttUsername
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val password: StateFlow<String?> = userPreferencesRepository.mqttPassword
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _connectionStatus = MutableStateFlow("Desconectado")
    val connectionStatus: StateFlow<String> = _connectionStatus

    fun saveMqttCredentials(username: String, password: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveMqttCredentials(username, password)
            _connectionStatus.value = "Credenciales guardadas"
        }
    }

    fun testMqttConnection(username: String, password: String) {
        viewModelScope.launch {
            _connectionStatus.value = "Conectando..."
            try {
                hiveMqttClient.disconnect()
                hiveMqttClient.connect(username, password)
                if (hiveMqttClient.isConnected.value) {
                    _connectionStatus.value = "Conectado"
                } else {
                    _connectionStatus.value = "Fallo al conectar"
                }
            } catch (e: Exception) {
                _connectionStatus.value = "Error: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                hiveMqttClient.disconnect()
            }
        }
    }

    // ¡NUEVO! Función para usar las credenciales predeterminadas
    fun useDefaultMqttCredentials() {
        viewModelScope.launch {

            val defaultUsername = hiveMqttClient.DEFAULT_USERNAME
            val defaultPassword = hiveMqttClient.DEFAULT_PASSWORD
            userPreferencesRepository.saveMqttCredentials(defaultUsername, defaultPassword)
            _connectionStatus.value = "Usando predeterminadas"
        }
    }
}
