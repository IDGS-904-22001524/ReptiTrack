package com.waldoz_x.reptitrack.data.repository

import com.waldoz_x.reptitrack.data.model.TerrariumDto
import com.waldoz_x.reptitrack.data.source.remote.TerrariumFirebaseDataSource
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.domain.repository.TerrariumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Esta anotación es crucial para que Dagger Hilt provea una única instancia
class TerrariumRepositoryImpl @Inject constructor( // Este constructor @Inject es crucial para la inyección de dependencias
    private val firebaseDataSource: TerrariumFirebaseDataSource // Inyecta la fuente de datos de Firebase
) : TerrariumRepository { // Asegúrate de que implementa la interfaz TerrariumRepository

    /**
     * Obtiene un flujo de todos los terrarios para un usuario específico desde la fuente de datos de Firebase.
     * Mapea los objetos DTO (Data Transfer Objects) a objetos de dominio.
     * @param userId El ID del usuario para el que se obtendrán los terrarios.
     * @return Flow que emite una lista de objetos Terrarium.
     */
    override fun getAllTerrariums(userId: String): Flow<List<Terrarium>> { // ¡CORREGIDO! Ahora acepta userId
        return firebaseDataSource.getAllTerrariums(userId).map { dtoList -> // Pasa userId a la fuente de datos
            dtoList.map { it.toDomain() } // Convierte cada TerrariumDto a Terrarium
        }
    }

    /**
     * Obtiene un terrario específico por su ID para un usuario desde la fuente de datos de Firebase.
     * @param userId El ID del usuario actual.
     * @param id El ID del terrario a obtener.
     * @return El objeto Terrarium si se encuentra, o null si no.
     */
    override fun getTerrariumById(userId: String, id: String): Flow<Terrarium?> { // ¡CORREGIDO! Ahora acepta userId
        return firebaseDataSource.getTerrariumById(userId, id).map { terrariumDto -> // Pasa userId y id a la fuente de datos
            terrariumDto?.toDomain()
        }
    }

    /**
     * Añade un nuevo terrario para un usuario a la fuente de datos de Firebase.
     * @param userId El ID del usuario actual.
     * @param terrarium El objeto Terrarium a añadir.
     */
    override suspend fun addTerrarium(userId: String, terrarium: Terrarium) { // ¡CORREGIDO! Ahora acepta userId
        firebaseDataSource.addTerrarium(userId, TerrariumDto.fromDomain(terrarium)) // Pasa userId y DTO
    }

    /**
     * Actualiza un terrario existente para un usuario en la fuente de datos de Firebase.
     * @param userId El ID del usuario actual.
     * @param terrarium El objeto Terrarium con los datos actualizados.
     */
    override suspend fun updateTerrarium(userId: String, terrarium: Terrarium) { // ¡CORREGIDO! Ahora acepta userId
        firebaseDataSource.updateTerrarium(userId, TerrariumDto.fromDomain(terrarium)) // Pasa userId y DTO
    }

    /**
     * Elimina un terrario por su ID para un usuario de la fuente de datos de Firebase.
     * @param userId El ID del usuario actual.
     * @param id El ID del terrario a eliminar.
     */
    override suspend fun deleteTerrarium(userId: String, id: String) { // ¡CORREGIDO! Ahora acepta userId
        firebaseDataSource.deleteTerrarium(userId, id) // Pasa userId y id
    }

    /**
     * Actualiza el estado de un actuador específico para un terrario dado y un usuario.
     * @param userId El ID del usuario actual.
     * @param terrariumId El ID del terrario cuyo actuador se va a actualizar.
     * @param actuatorKey La clave del actuador (ej. "water_pump_active").
     * @param newState El nuevo estado del actuador (true para activo, false para inactivo).
     */
    override suspend fun updateTerrariumActuatorState(userId: String, terrariumId: String, actuatorKey: String, newState: Boolean) { // ¡CORREGIDO! Ahora acepta userId
        val currentTerrarium = firebaseDataSource.getTerrariumById(userId, terrariumId).map { terrariumDto -> // Pasa userId y terrariumId
            terrariumDto?.toDomain()
        }.firstOrNull()

        currentTerrarium?.let { terrarium ->
            val updatedTerrarium = when (actuatorKey) {
                "water_pump_active" -> terrarium.copy(waterPumpActive = newState)
                "fan1_active" -> terrarium.copy(fan1Active = newState)
                "fan2_active" -> terrarium.copy(fan2Active = newState)
                "light1_active" -> terrarium.copy(light1Active = newState)
                "light2_active" -> terrarium.copy(light2Active = newState)
                "light3_active" -> terrarium.copy(light3Active = newState)
                "heat_plate1_active" -> terrarium.copy(heatPlate1Active = newState)
                else -> terrarium // No se reconoce el actuador, devuelve el mismo terrario
            }
            firebaseDataSource.updateTerrarium(userId, TerrariumDto.fromDomain(updatedTerrarium)) // Pasa userId y DTO
        }
    }
}
