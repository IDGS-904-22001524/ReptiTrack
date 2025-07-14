// com.waldoz_x.reptitrack.data.source.remote/TerrariumFirebaseDataSource.kt
package com.waldoz_x.reptitrack.data.source.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.waldoz_x.reptitrack.data.model.TerrariumDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // ¡Esta anotación es crucial!
class TerrariumFirebaseDataSource @Inject constructor( // ¡Este constructor @Inject es crucial!
    private val firestore: FirebaseFirestore
) {
    private val terrariumsCollection = firestore.collection("terrariums")
    private val TAG = "TerrariumFirebaseDS"

    fun getAllTerrariums(): Flow<List<TerrariumDto>> = callbackFlow {
        val subscription = terrariumsCollection
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error al escuchar terrarios: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val terrariums = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(TerrariumDto::class.java)
                    }
                    trySend(terrariums).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getTerrariumById(id: String): TerrariumDto? {
        return try {
            terrariumsCollection.document(id).get().await().toObject(TerrariumDto::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener terrario por ID ($id): ${e.message}", e)
            null
        }
    }

    suspend fun addTerrarium(terrarium: TerrariumDto) {
        try {
            terrariumsCollection.document(terrarium.id).set(terrarium).await()
            Log.d(TAG, "Terrario añadido/actualizado: ${terrarium.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al añadir terrario: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateTerrarium(terrarium: TerrariumDto) {
        addTerrarium(terrarium)
    }

    suspend fun deleteTerrarium(id: String) {
        try {
            terrariumsCollection.document(id).delete().await()
            Log.d(TAG, "Terrario eliminado: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar terrario ($id): ${e.message}", e)
            throw e
        }
    }
}
