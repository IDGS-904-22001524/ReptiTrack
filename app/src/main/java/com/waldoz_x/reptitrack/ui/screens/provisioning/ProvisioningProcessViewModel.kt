package com.waldoz_x.reptitrack.ui.screens.provisioning

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.repository.ProvisioningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth


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

    val checkpointReached: StateFlow<Boolean> = provisioningRepository.checkpointReached

    val provisioningError = provisioningRepository.provisioningError


    fun iniciarNuevaSesion() {
        provisioningRepository.desconectarDispositivo()
        provisioningRepository.clear()
        provisioningRepository.clearDisconnectedFlag()
    }


    fun startProvisioning() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
                _dbStep.value = StepState.LOADING
                provisioningRepository.sendDatabaseConfig(userId.trim())  // Ejecutamos la funcion que envia los datos
                _dbStep.value = StepState.SUCCESS

                _mqttStep.value = StepState.LOADING
                provisioningRepository.sendMqttConfig() // Ejecutamos la funcion que envia los datos
                _mqttStep.value = StepState.SUCCESS

                _wifiStep.value = StepState.LOADING

                val ssid = provisioningRepository.ssid.value.trim() ?: throw IllegalStateException("SSID vacío")
                val password = provisioningRepository.password.value.trim() ?: ""

                provisioningRepository.sendWifiCredentials(ssid, password) // Ejecutamos la funcion que envia los datos
                _wifiStep.value = StepState.SUCCESS

                // Simula espera de confirmación MQTT
                delay(1000)
                _allDone.value = true

            } catch (e: Exception) {
                Log.e("ProvisioningViewModel", "Error en el paso de provisioning: ${e.message}")

                /*
                 * Este bloque catch captura cualquier excepción lanzada dentro del proceso de aprovisionamiento
                 * Algunas funciones como sendDatabaseConfig, sendMqttConfig y sendWifiCredentials son funciones suspendidas
                 * que internamente usan `suspendCancellableCoroutine { ... cont.resumeWith(Result.failure(...)) }`
                 * para reportar errores.

                 * Cuando ocurre un error dentro del ProvisioningRepository (por ejemplo, fallo en la sesión BLE,
                 * red no encontrada, clave incorrecta, etc.), se llama a `cont.resumeWith(Result.failure(Exception("mensaje")))`
                 *
                 * Esta llamada propaga la excepción y hace que la coroutine se reanude con un error,
                 * el cual es capturado aquí gracias al bloque `try-catch`.

             */
                when {
                    _dbStep.value == StepState.LOADING -> _dbStep.value = StepState.ERROR
                    _mqttStep.value == StepState.LOADING -> _mqttStep.value = StepState.ERROR
                    _wifiStep.value == StepState.LOADING -> _wifiStep.value = StepState.ERROR
                }
            }
        }
    }

}
