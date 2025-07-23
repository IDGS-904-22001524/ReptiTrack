package com.waldoz_x.reptitrack.domain.usecase

import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.domain.repository.TerrariumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTerrariumsUseCase @Inject constructor(
    private val repository: TerrariumRepository
) {
    /**
     * Invoca el caso de uso para obtener todos los terrarios de un usuario específico.
     * @param userId El ID del usuario para el que se obtendrán los terrarios.
     * @return Un Flow que emite una lista de objetos Terrarium.
     */
    operator fun invoke(userId: String): Flow<List<Terrarium>> {
        // Pasa el userId al método getAllTerrariums del repositorio
        return repository.getAllTerrariums(userId)
    }
}
