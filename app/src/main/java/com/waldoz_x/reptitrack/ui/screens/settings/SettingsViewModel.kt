// com.waldoz_x.reptitrack.ui.screens.settings/SettingsViewModel.kt
package com.waldoz_x.reptitrack.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale // Importación necesaria para Locale
import java.util.TimeZone // Importación necesaria para TimeZone
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Estado del switch de "Actualizar con datos móviles", observado desde DataStore
    val autoUpdateMobileData: StateFlow<Boolean> = userPreferencesRepository.autoUpdateMobileData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Estado del switch de "Actualizar con Wi-Fi", observado desde DataStore
    val autoUpdateWifi: StateFlow<Boolean> = userPreferencesRepository.autoUpdateWifi
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Estado de la zona horaria seleccionada, observado desde DataStore
    val selectedTimeZoneId: StateFlow<String?> = userPreferencesRepository.selectedTimeZoneId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Se inicializa a null, se cargará desde DataStore
        )

    // Estado del país/región seleccionado, observado desde DataStore
    val selectedCountryRegion: StateFlow<String?> = userPreferencesRepository.selectedCountryRegion
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Se inicializa a null, se cargará desde DataStore
        )

    // Estado para saber si es el primer inicio de la app
    val firstLaunchCompleted: StateFlow<Boolean> = userPreferencesRepository.firstLaunchCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // ¡NUEVO! Estados para el nombre de usuario y contraseña MQTT
    val mqttUsername: StateFlow<String?> = userPreferencesRepository.mqttUsername
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val mqttPassword: StateFlow<String?> = userPreferencesRepository.mqttPassword
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Al inicializar el ViewModel, verifica si es el primer inicio y configura la zona horaria/país
        viewModelScope.launch {
            userPreferencesRepository.firstLaunchCompleted.collect { completed ->
                if (!completed) {
                    // Si es el primer inicio, configura la zona horaria y el país por defecto
                    val defaultTimeZone = TimeZone.getDefault().id
                    val defaultCountry = Locale.getDefault().displayCountry // Obtiene el nombre del país
                    userPreferencesRepository.saveSelectedTimeZoneId(defaultTimeZone)
                    userPreferencesRepository.saveSelectedCountryRegion(defaultCountry)
                    userPreferencesRepository.setFirstLaunchCompleted(true) // Marca como completado el primer inicio
                }
            }
        }
    }


    fun setAutoUpdateMobileData(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveAutoUpdateMobileDataPreference(enabled)
        }
    }

    fun setAutoUpdateWifi(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveAutoUpdateWifiPreference(enabled)
        }
    }

    fun setSelectedTimeZoneId(timeZoneId: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedTimeZoneId(timeZoneId)
            // Cuando se selecciona una zona horaria, intenta inferir el país/región
            val countryCode = TimeZone.getTimeZone(timeZoneId).id.split("/").firstOrNull() // Intenta obtener el código de país de la zona horaria (ej. America/Mexico_City -> America)
            val countryName = countryCode?.let { code ->
                // Mapea el código de país (ej. "America") a un nombre de país real si es posible
                Locale.getAvailableLocales().find { it.country == code }?.displayCountry
            } ?: Locale.getDefault().displayCountry // Si no se encuentra, usa el país por defecto del dispositivo
            userPreferencesRepository.saveSelectedCountryRegion(countryName)
        }
    }

    // Función para guardar el país/región seleccionado manualmente
    fun setSelectedCountryRegion(countryRegion: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedCountryRegion(countryRegion)
        }
    }

    // ¡NUEVO! Función para guardar credenciales MQTT
    fun saveMqttCredentials(username: String, password: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveMqttCredentials(username, password)
        }
    }
}
