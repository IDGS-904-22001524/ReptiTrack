package com.waldoz_x.reptitrack.data.source.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.waldoz_x.reptitrack.data.model.TerrariumDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerrariumFirebaseDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val TAG = "TerrariumFirebaseDS"

    // Función auxiliar para obtener la colección de terrarios de un usuario específico
    private fun getUserTerrariumsCollection(userId: String) =
        firestore.collection("usuarios").document(userId).collection("terrarios")

    /**
     * Obtiene un flujo de todos los terrarios DTO para un usuario específico desde Firestore en tiempo real.
     * @param userId El ID del usuario actual.
     * @return Flow que emite una lista de objetos TerrariumDto.
     */
    fun getAllTerrariums(userId: String): Flow<List<TerrariumDto>> = callbackFlow {
        val subscription = getUserTerrariumsCollection(userId) // Usa la colección específica del usuario
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error al escuchar terrarios para el usuario $userId: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val terrariums = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(TerrariumDto::class.java)
                        } catch (ex: Exception) {
                            Log.e(TAG, "Error mapeando documento ${doc.id} a TerrariumDto para el usuario $userId: ${ex.message}", ex)
                            null
                        }
                    }
                    trySend(terrariums).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Obtiene un flujo de un terrario DTO específico por su ID para un usuario desde Firestore en tiempo real.
     * @param userId El ID del usuario actual.
     * @param terrariumId El ID del terrario a obtener.
     * @return Flow que emite el objeto TerrariumDto si se encuentra, o null si no.
     */
    fun getTerrariumById(userId: String, terrariumId: String): Flow<TerrariumDto?> = callbackFlow {
        val docRef = getUserTerrariumsCollection(userId).document(terrariumId) // Usa la colección específica del usuario
        val subscription = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error al escuchar terrario por ID ($terrariumId) para el usuario $userId: ${e.message}", e)
                close(e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val terrariumDto = snapshot.toObject(TerrariumDto::class.java)
                trySend(terrariumDto).isSuccess
            } else {
                trySend(null).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Añade un nuevo terrario DTO para un usuario específico a Firestore.
     * @param userId El ID del usuario actual.
     * @param terrarium El objeto TerrariumDto a añadir.
     */
    suspend fun addTerrarium(userId: String, terrarium: TerrariumDto) {
        try {
            getUserTerrariumsCollection(userId).document(terrarium.id).set(terrarium).await() // Usa la colección específica del usuario
            Log.d(TAG, "Terrario añadido/actualizado para el usuario $userId: ${terrarium.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al añadir terrario para el usuario $userId: ${e.message}", e)
            throw e
        }
    }

    /**
     * Actualiza un terrario DTO existente para un usuario específico en Firestore.
     * Simplemente llama a addTerrarium ya que Firestore.set() actualiza o crea.
     * @param userId El ID del usuario actual.
     * @param terrarium El objeto TerrariumDto con los datos actualizados.
     */
    suspend fun updateTerrarium(userId: String, terrarium: TerrariumDto) {
        addTerrarium(userId, terrarium) // set() se encarga de actualizar si el documento ya existe
    }

    /**
     * Elimina un terrario por su ID para un usuario específico de Firestore.
     * @param userId El ID del usuario actual.
     * @param terrariumId El ID del terrario a eliminar.
     */
    suspend fun deleteTerrarium(userId: String, terrariumId: String) {
        try {
            getUserTerrariumsCollection(userId).document(terrariumId).delete().await() // Usa la colección específica del usuario
            Log.d(TAG, "Terrario eliminado para el usuario $userId: $terrariumId")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar terrario ($terrariumId) para el usuario $userId: ${e.message}", e)
            throw e
        }
    }
}
