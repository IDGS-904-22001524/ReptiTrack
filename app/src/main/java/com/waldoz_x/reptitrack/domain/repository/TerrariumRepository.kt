package com.waldoz_x.reptitrack.domain.repository

import com.waldoz_x.reptitrack.domain.model.Terrarium
import kotlinx.coroutines.flow.Flow

interface TerrariumRepository {
    fun getAllTerrariums(): Flow<List<Terrarium>>
    suspend fun getTerrariumById(id: String): Terrarium?
    suspend fun addTerrarium(terrarium: Terrarium)
    suspend fun updateTerrarium(terrarium: Terrarium)
    suspend fun deleteTerrarium(id: String)
}