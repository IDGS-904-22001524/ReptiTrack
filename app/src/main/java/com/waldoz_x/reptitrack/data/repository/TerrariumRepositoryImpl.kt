// com.waldoz_x.reptitrack.data.repository/TerrariumRepositoryImpl.kt
package com.waldoz_x.reptitrack.data.repository

import com.waldoz_x.reptitrack.data.model.TerrariumDto
import com.waldoz_x.reptitrack.data.source.remote.TerrariumFirebaseDataSource
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.domain.repository.TerrariumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // ¡Esta anotación es crucial!
class TerrariumRepositoryImpl @Inject constructor( // ¡Este constructor @Inject es crucial!
    private val firebaseDataSource: TerrariumFirebaseDataSource
) : TerrariumRepository { // ¡Asegúrate de que implementa la interfaz!

    override fun getAllTerrariums(): Flow<List<Terrarium>> {
        return firebaseDataSource.getAllTerrariums().map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun getTerrariumById(id: String): Terrarium? {
        return firebaseDataSource.getTerrariumById(id)?.toDomain()
    }

    override suspend fun addTerrarium(terrarium: Terrarium) {
        firebaseDataSource.addTerrarium(TerrariumDto.fromDomain(terrarium))
    }

    override suspend fun updateTerrarium(terrarium: Terrarium) {
        firebaseDataSource.updateTerrarium(TerrariumDto.fromDomain(terrarium))
    }

    override suspend fun deleteTerrarium(id: String) {
        firebaseDataSource.deleteTerrarium(id)
    }
}
