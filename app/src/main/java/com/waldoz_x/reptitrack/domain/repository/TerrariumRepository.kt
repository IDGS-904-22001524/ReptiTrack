package com.waldoz_x.reptitrack.domain.repository

import com.waldoz_x.reptitrack.domain.model.Terrarium
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del Repositorio de Terrarios.
 * Define las operaciones disponibles para interactuar con los datos de los terrarios.
 * Esta interfaz abstrae la fuente de datos subyacente (ej. Firestore).
 */
interface TerrariumRepository {

    /**
     * Obtiene un flujo de todos los terrarios para un usuario específico.
     * @param userId El ID del usuario para el que se obtendrán los terrarios.
     * @return Flow que emite una lista de objetos Terrarium.
     */
    fun getAllTerrariums(userId: String): Flow<List<Terrarium>>

    /**
     * Obtiene un terrario específico por su ID para un usuario.
     * @param userId El ID del usuario actual.
     * @param id El ID del terrario a obtener.
     * @return Flow que emite el objeto Terrarium si se encuentra, o null si no.
     * Este Flow permite observar cambios en tiempo real.
     */
    fun getTerrariumById(userId: String, id: String): Flow<Terrarium?> // ¡CORREGIDO! Ahora acepta userId

    /**
     * Añade un nuevo terrario para un usuario.
     * @param userId El ID del usuario actual.
     * @param terrarium El objeto Terrarium a añadir.
     */
    suspend fun addTerrarium(userId: String, terrarium: Terrarium) // ¡CORREGIDO! Ahora acepta userId

    /**
     * Actualiza un terrario existente para un usuario.
     * @param userId El ID del usuario actual.
     * @param terrarium El objeto Terrarium con los datos actualizados.
     */
    suspend fun updateTerrarium(userId: String, terrarium: Terrarium) // ¡CORREGIDO! Ahora acepta userId

    /**
     * Elimina un terrario por su ID para un usuario.
     * @param userId El ID del usuario actual.
     * @param id El ID del terrario a eliminar.
     */
    suspend fun deleteTerrarium(userId: String, id: String) // ¡CORREGIDO! Ahora acepta userId

    /**
     * Actualiza el estado de un actuador específico para un terrario dado y un usuario.
     * @param userId El ID del usuario actual.
     * @param terrariumId El ID del terrario cuyo actuador se va a actualizar.
     * @param actuatorKey La clave del actuador (ej. "water_pump_active").
     * @param newState El nuevo estado del actuador (true para activo, false para inactivo).
     */
    suspend fun updateTerrariumActuatorState(userId: String, terrariumId: String, actuatorKey: String, newState: Boolean) // ¡CORREGIDO! Ahora acepta userId
}
