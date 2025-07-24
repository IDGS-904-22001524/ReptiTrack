package com.waldoz_x.reptitrack.presentation.provisioning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.repository.ProvisioningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProvisioningViewModel @Inject constructor(
    private val provisioningRepository: ProvisioningRepository
) : ViewModel() {

    enum class StepState { LOADING, SUCCESS, ERROR, IDLE }

    private val _dbStep = MutableStateFlow(StepState.IDLE)
    val dbStep: StateFlow<StepState> = _dbStep

    private val _mqttStep = MutableStateFlow(StepState.IDLE)
    val mqttStep: StateFlow<StepState> = _mqttStep

    private val _wifiStep = MutableStateFlow(StepState.IDLE)
    val wifiStep: StateFlow<StepState> = _wifiStep

    private val _allDone = MutableStateFlow(false)
    val allDone: StateFlow<Boolean> = _allDone

    fun startProvisioning() {
        viewModelScope.launch {
            try {
                _dbStep.value = StepState.LOADING
                provisioningRepository.sendDatabaseConfig()
                _dbStep.value = StepState.SUCCESS

                _mqttStep.value = StepState.LOADING
                provisioningRepository.sendMqttConfig()
                _mqttStep.value = StepState.SUCCESS

                _wifiStep.value = StepState.LOADING

                val ssid = provisioningRepository.ssid.value ?: throw IllegalStateException("SSID vacío")
                val password = provisioningRepository.password.value ?: ""

                provisioningRepository.sendWifiCredentials(ssid, password)
                _wifiStep.value = StepState.SUCCESS

                // Simula espera de confirmación MQTT
                delay(1000)
                _allDone.value = true

            } catch (e: Exception) {
                when {
                    _dbStep.value == StepState.LOADING -> _dbStep.value = StepState.ERROR
                    _mqttStep.value == StepState.LOADING -> _mqttStep.value = StepState.ERROR
                    _wifiStep.value == StepState.LOADING -> _wifiStep.value = StepState.ERROR
                }
            }
        }
    }
}
