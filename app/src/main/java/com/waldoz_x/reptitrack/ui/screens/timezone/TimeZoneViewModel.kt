package com.waldoz_x.reptitrack.ui.screens.timezone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.TimeZone // Necesario para obtener las zonas horarias
import javax.inject.Inject

@HiltViewModel
class TimeZoneViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Lista completa de todas las zonas horarias disponibles
    private val allTimeZones: List<String> = TimeZone.getAvailableIDs().toList().sorted()

    // Estado de la consulta de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Flow de zonas horarias filtradas basado en la consulta de búsqueda
    val filteredTimeZones: StateFlow<List<String>> = searchQuery
        .map { query ->
            if (query.isBlank()) {
                allTimeZones
            } else {
                allTimeZones.filter { it.contains(query, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = allTimeZones // Valor inicial: todas las zonas horarias
        )

    // Observa la zona horaria seleccionada desde el repositorio de preferencias
    val selectedTimeZoneId: StateFlow<String?> = userPreferencesRepository.selectedTimeZoneId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // O TimeZone.getDefault().id si quieres un valor por defecto
        )

    // Actualiza la consulta de búsqueda
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Guarda la zona horaria seleccionada en el repositorio de preferencias
    fun setSelectedTimeZoneId(timeZoneId: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedTimeZoneId(timeZoneId)
        }
    }
}
