
package com.waldoz_x.reptitrack.ui.screens.provisioning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.repository.ProvisioningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CredentialsViewModel @Inject constructor(
    private val provisioningRepository: ProvisioningRepository
) : ViewModel() {
    val username: StateFlow<String?> = provisioningRepository.username.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val proof: StateFlow<String?> = provisioningRepository.proof.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val provisioningRound: StateFlow<Int> = provisioningRepository.provisioningRound



    fun iniciarNuevaSesion() {
        provisioningRepository.desconectarDispositivo()
        provisioningRepository.clear()
        provisioningRepository.clearDisconnectedFlag()
    }

    fun setCredentials(username: String, proof: String) {
        viewModelScope.launch {
            provisioningRepository.saveCredentials(username, proof)
        }
    }
    fun areCredentialsValid(username: String?, proof: String?): Boolean {
        return !username.isNullOrBlank() && !proof.isNullOrBlank()
    }
}