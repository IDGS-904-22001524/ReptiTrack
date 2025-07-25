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
class CheckpointViewModel @Inject constructor(
    private val provisioningRepository: ProvisioningRepository,
) : ViewModel() {

    val checkpointReached: StateFlow<Boolean> = provisioningRepository.checkpointReached

    fun markCheckpointPassed() {
        viewModelScope.launch {
            provisioningRepository.markCheckpointReached()
            provisioningRepository.incrementProvisioningRound()
        }
    }
}