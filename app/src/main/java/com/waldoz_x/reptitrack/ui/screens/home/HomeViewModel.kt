package com.waldoz_x.reptitrack.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth // Importa FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore
import com.google.firebase.firestore.FieldValue // Importa FieldValue para timestamps
import com.waldoz_x.reptitrack.data.source.remote.HiveMqttClient
import com.waldoz_x.reptitrack.domain.model.Terrarium
import com.waldoz_x.reptitrack.domain.usecase.GetAllTerrariumsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.catch // Importa catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Para usar await en tareas de Firebase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllTerrariumsUseCase: GetAllTerrariumsUseCase,
    private val hiveMqttClient: HiveMqttClient,
    private val auth: FirebaseAuth, // Inyecta FirebaseAuth
    private val firestore: FirebaseFirestore // Inyecta FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    val mqttConnectionState: StateFlow<Boolean> = hiveMqttClient.isConnected
    val mqttReceivedMessages: StateFlow<Pair<String, String>?> = hiveMqttClient.receivedMessages

    private val _firebaseConnectionState = MutableStateFlow(false) // Estado de conexión de Firebase
    val firebaseConnectionState: StateFlow<Boolean> = _firebaseConnectionState

    private val _currentUserData = MutableStateFlow<UserData?>(null) // Datos del usuario actual
    val currentUserData: StateFlow<UserData?> = _currentUserData

    init {
        viewModelScope.launch {
            // 1. Manejar autenticación de Firebase
            // Este listener se activa cada vez que el estado de autenticación de Firebase cambia.
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    // Usuario autenticado (registrado o anónimo)
                    val userId = user.uid
                    Log.d("HomeViewModel", "Usuario autenticado: $userId")
                    // Cargar datos del usuario y actualizar el último inicio de sesión
                    updateUserLoginStatus(userId, user.isAnonymous, user.email)
                    // Carga terrarios para el usuario actual. Es crucial que esto se llame después de tener el userId.
                    loadTerrariums(userId)
                } else {
                    // No hay usuario, intentar iniciar sesión anónimamente.
                    // Esto ocurre en el primer inicio de la app o si el usuario cierra sesión.
                    Log.d("HomeViewModel", "No hay usuario, intentando iniciar sesión anónimamente...")
                    signInAnonymously()
                }
            }

            // 2. Conectar MQTT
            // La conexión MQTT se inicia independientemente de la autenticación de Firebase.
            hiveMqttClient.connect()
            // Suscribirse a un tópico general para todos los terrarios.
            hiveMqttClient.subscribeToTopic("terrariums/data/#")
        }
    }

    /**
     * Intenta iniciar sesión en Firebase de forma anónima.
     * Si tiene éxito, actualiza el estado del usuario en Firestore y carga los terrarios.
     */
    private fun signInAnonymously() = viewModelScope.launch {
        try {
            val result = auth.signInAnonymously().await()
            val user = result.user
            if (user != null) {
                Log.d("HomeViewModel", "Sesión anónima iniciada con UID: ${user.uid}")
                // Crear/actualizar el documento del usuario en Firestore para el usuario anónimo
                updateUserLoginStatus(user.uid, true, null)
                loadTerrariums(user.uid) // Carga terrarios para el nuevo usuario anónimo
            } else {
                Log.e("HomeViewModel", "Error: Usuario anónimo nulo después del inicio de sesión.")
                _firebaseConnectionState.value = false
                _uiState.value = HomeUiState.Error("No se pudo iniciar sesión anónimamente.")
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error al iniciar sesión anónimamente: ${e.message}", e)
            _firebaseConnectionState.value = false
            _uiState.value = HomeUiState.Error("Error de autenticación: ${e.localizedMessage ?: "Desconocido"}")
        }
    }

    /**
     * Actualiza el documento del usuario en Firestore con su estado de sesión (invitado, email)
     * y la marca de tiempo del último inicio de sesión.
     * También escucha los cambios en el documento del usuario para mantener `currentUserData` actualizado.
     * @param userId El ID del usuario actual.
     * @param isAnonymous Indica si el usuario es anónimo.
     * @param email El correo electrónico del usuario, si está registrado.
     */
    private fun updateUserLoginStatus(userId: String, isAnonymous: Boolean, email: String?) = viewModelScope.launch {
        try {
            val userDocRef = firestore.collection("usuarios").document(userId)
            val userData = hashMapOf(
                "isGuest" to isAnonymous,
                "email" to email,
                "ultimoInicioSesion" to FieldValue.serverTimestamp() // Usa un timestamp del servidor para mayor precisión
            )
            // Usa SetOptions.merge() para no sobrescribir otros campos existentes (como 'nombre')
            userDocRef.set(userData, com.google.firebase.firestore.SetOptions.merge()).await()
            Log.d("HomeViewModel", "Estado de usuario y último inicio de sesión actualizados para $userId")

            // Escucha en tiempo real los datos del usuario para la UI
            userDocRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeViewModel", "Error al escuchar datos del usuario: ${e.message}", e)
                    _currentUserData.value = null
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    _currentUserData.value = UserData(
                        id = userId,
                        name = data?.get("nombre") as? String, // Asume que 'nombre' podría existir si el usuario se registra
                        isGuest = data?.get("isGuest") as? Boolean ?: isAnonymous, // Prioriza el dato de Firestore, si no, usa el de Auth
                        email = data?.get("email") as? String ?: email, // Prioriza el dato de Firestore, si no, usa el de Auth
                        lastLogin = data?.get("ultimoInicioSesion") as? com.google.firebase.firestore.ServerTimestamp
                    )
                } else {
                    _currentUserData.value = null
                }
            }
            _firebaseConnectionState.value = true // Si la actualización inicial es exitosa, Firebase está conectado
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error al actualizar el estado de login del usuario: ${e.message}", e)
            _firebaseConnectionState.value = false
        }
    }

    /**
     * Carga los terrarios para el usuario actual.
     * Si no se proporciona un userId, intenta obtenerlo del usuario autenticado actualmente.
     * @param userId El ID del usuario para el que se cargarán los terrarios (opcional).
     */
    fun loadTerrariums(userId: String? = auth.currentUser?.uid) {
        if (userId == null) {
            Log.w("HomeViewModel", "No hay userId disponible para cargar terrarios.")
            _uiState.value = HomeUiState.Error("No hay usuario autenticado para cargar terrarios.")
            _firebaseConnectionState.value = false
            return
        }

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Pasa el userId al caso de uso GetAllTerrariumsUseCase
                getAllTerrariumsUseCase(userId)
                    .catch { e ->
                        // Captura errores del flujo de datos de terrarios
                        Log.e("HomeViewModel", "Error al cargar terrarios para $userId: ${e.message}", e)
                        _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Error desconocido al cargar terrarios")
                        _firebaseConnectionState.value = false
                    }
                    .collectLatest { terrariums ->
                        _uiState.value = HomeUiState.Success(terrariums)
                        _firebaseConnectionState.value = true // Si la carga es exitosa, Firebase está conectado
                    }
            } catch (e: Exception) {
                // Captura errores generales de la corrutina (ej. problemas de red antes de que el flujo se establezca)
                Log.e("HomeViewModel", "Excepción al cargar terrarios para $userId: ${e.message}", e)
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Error desconocido al cargar terrarios")
                _firebaseConnectionState.value = false
            }
        }
    }

    /**
     * Publica un mensaje MQTT en un tópico específico.
     * @param topic El tópico MQTT.
     * @param message El mensaje a publicar.
     */
    fun publishMqttCommand(topic: String, message: String) {
        viewModelScope.launch {
            hiveMqttClient.publishMessage(topic, message)
        }
    }

    /**
     * Se llama cuando el ViewModel ya no está en uso y será destruido.
     * Se usa para desconectar el cliente MQTT y liberar recursos.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            hiveMqttClient.disconnect()
        }
    }
}

/**
 * Clases selladas para representar los diferentes estados de la UI de la pantalla de inicio.
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val terrariums: List<Terrarium>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

/**
 * Clase de datos para representar la información relevante del usuario para la UI.
 * @param id El UID del usuario.
 * @param name El nombre del usuario (opcional).
 * @param isGuest Verdadero si el usuario es anónimo, falso si está registrado.
 * @param email El correo electrónico del usuario (opcional, solo si está registrado).
 * @param lastLogin La marca de tiempo del último inicio de sesión.
 */
data class UserData(
    val id: String,
    val name: String?,
    val isGuest: Boolean,
    val email: String?,
    val lastLogin: com.google.firebase.firestore.ServerTimestamp?
)
