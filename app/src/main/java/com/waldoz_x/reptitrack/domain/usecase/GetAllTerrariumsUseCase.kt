package com.waldoz_x.reptitrack.domain.usecase

import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.domain.repository.TerrariumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTerrariumsUseCase @Inject constructor(
    private val repository: TerrariumRepository
) {
    operator fun invoke(): Flow<List<Terrarium>> {
        return repository.getAllTerrariums()
    }
}