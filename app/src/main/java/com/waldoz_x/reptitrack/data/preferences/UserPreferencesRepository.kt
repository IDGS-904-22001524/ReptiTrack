// com.waldoz_x.reptitrack.data.preferences/UserPreferencesRepository.kt
package com.waldoz_x.reptitrack.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extensión para crear una instancia de DataStore a nivel de aplicación
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "UserPrefsRepo"

    // Claves para las preferencias
    private object PreferencesKeys {
        val AUTO_UPDATE_MOBILE_DATA = booleanPreferencesKey("auto_update_mobile_data")
        val AUTO_UPDATE_WIFI = booleanPreferencesKey("auto_update_wifi")
        val SELECTED_TIME_ZONE_ID = stringPreferencesKey("selected_time_zone_id")
        val SELECTED_COUNTRY_REGION = stringPreferencesKey("selected_country_region")
        val FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")
        val MQTT_USERNAME = stringPreferencesKey("mqtt_username") // ¡NUEVO! Clave para el usuario MQTT
        val MQTT_PASSWORD = stringPreferencesKey("mqtt_password") // ¡NUEVO! Clave para la contraseña MQTT
    }

    // Flow para leer el estado de la preferencia de "actualizar con datos móviles"
    val autoUpdateMobileData: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.AUTO_UPDATE_MOBILE_DATA] ?: false
            Log.d(TAG, "Leyendo preferencia 'auto_update_mobile_data': $value")
            value
        }

    // Flow para leer el estado de la preferencia de "actualizar con Wi-Fi"
    val autoUpdateWifi: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.AUTO_UPDATE_WIFI] ?: true
            Log.d(TAG, "Leyendo preferencia 'auto_update_wifi': $value")
            value
        }

    // Flow para leer la zona horaria seleccionada
    val selectedTimeZoneId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.SELECTED_TIME_ZONE_ID]
            Log.d(TAG, "Leyendo preferencia 'selected_time_zone_id': $value")
            value
        }

    // Flow para leer el país/región seleccionado
    val selectedCountryRegion: Flow<String?> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.SELECTED_COUNTRY_REGION]
            Log.d(TAG, "Leyendo preferencia 'selected_country_region': $value")
            value
        }

    // Flow para leer si el primer inicio ha sido completado
    val firstLaunchCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.FIRST_LAUNCH_COMPLETED] ?: false
            Log.d(TAG, "Leyendo preferencia 'first_launch_completed': $value")
            value
        }

    // ¡NUEVO! Flow para leer el nombre de usuario MQTT
    val mqttUsername: Flow<String?> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.MQTT_USERNAME]
            Log.d(TAG, "Leyendo preferencia 'mqtt_username': $value")
            value
        }

    // ¡NUEVO! Flow para leer la contraseña MQTT
    val mqttPassword: Flow<String?> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.MQTT_PASSWORD]
            Log.d(TAG, "Leyendo preferencia 'mqtt_password': ${value?.take(3)}...") // No logear la contraseña completa
            value
        }

    // Función suspend para guardar el estado de la preferencia de "actualizar con datos móviles"
    suspend fun saveAutoUpdateMobileDataPreference(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_UPDATE_MOBILE_DATA] = enabled
            Log.d(TAG, "Guardando preferencia 'auto_update_mobile_data': $enabled")
        }
    }

    // Función suspend para guardar el estado de la preferencia de "actualizar con Wi-Fi"
    suspend fun saveAutoUpdateWifiPreference(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_UPDATE_WIFI] = enabled
            Log.d(TAG, "Guardando preferencia 'auto_update_wifi': $enabled")
        }
    }

    // Función suspend para guardar la zona horaria seleccionada
    suspend fun saveSelectedTimeZoneId(timeZoneId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TIME_ZONE_ID] = timeZoneId
            Log.d(TAG, "Guardando preferencia 'selected_time_zone_id': $timeZoneId")
        }
    }

    // Función suspend para guardar el país/región seleccionado
    suspend fun saveSelectedCountryRegion(countryRegion: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_COUNTRY_REGION] = countryRegion
            Log.d(TAG, "Guardando preferencia 'selected_country_region': $countryRegion")
        }
    }

    // Función suspend para marcar que el primer inicio ha sido completado
    suspend fun setFirstLaunchCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH_COMPLETED] = completed
            Log.d(TAG, "Marcando 'first_launch_completed' como: $completed")
        }
    }

    // ¡NUEVO! Función suspend para guardar las credenciales MQTT
    suspend fun saveMqttCredentials(username: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MQTT_USERNAME] = username
            preferences[PreferencesKeys.MQTT_PASSWORD] = password
            Log.d(TAG, "Guardando credenciales MQTT para usuario: $username")
        }
    }
}
