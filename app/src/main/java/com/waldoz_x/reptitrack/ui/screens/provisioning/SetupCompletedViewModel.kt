package com.waldoz_x.reptitrack.ui.screens.provisioning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.repository.ProvisioningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupCompletedViewModel @Inject constructor(
    private val provisioningRepository: ProvisioningRepository
) : ViewModel() {

    fun resetProvisioningState() {
        viewModelScope.launch {
            // Desconectar si aún hay un dispositivo activo
            provisioningRepository.desconectarDispositivo()

            // Limpiar credenciales Wi-Fi y ESPDevice
            provisioningRepository.clear()

            // Limpiar credenciales de usuario
            provisioningRepository.saveCredentials("", "")

            // Limpiar SSID y contraseña explícitamente
            provisioningRepository.saveWifiCredentials("", "")

            // Resetear checkpoint para iniciar flujo nuevo
            provisioningRepository.resetCheckpoint()

            // Limpiar flags de error y desconexión
            provisioningRepository.clearProvisioningError()
            provisioningRepository.clearDisconnectedFlag()

            //Reiniciar el contador
            provisioningRepository.resetProvisioningRound()

        }
    }
}
