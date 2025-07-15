package com.waldoz_x.reptitrack.ui.screens.countryregion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waldoz_x.reptitrack.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale // Necesario para obtener los países
import javax.inject.Inject

@HiltViewModel
class CountryRegionSelectionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Lista completa de todos los países/regiones disponibles
    private val allCountries: List<String> = Locale.getAvailableLocales()
        .map { it.displayCountry }
        .filter { it.isNotBlank() } // Filtra cadenas vacías
        .distinct() // Elimina duplicados
        .sorted() // Ordena alfabéticamente

    // Estado de la consulta de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Flow de países/regiones filtrados basado en la consulta de búsqueda
    val filteredCountries: StateFlow<List<String>> = searchQuery
        .map { query ->
            if (query.isBlank()) {
                allCountries
            } else {
                allCountries.filter { it.contains(query, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = allCountries // Valor inicial: todos los países
        )

    // Observa el país/región seleccionado desde el repositorio de preferencias
    val selectedCountryRegion: StateFlow<String?> = userPreferencesRepository.selectedCountryRegion
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Se inicializa a null, se cargará desde DataStore
        )

    // Actualiza la consulta de búsqueda
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Guarda el país/región seleccionado en el repositorio de preferencias
    fun setSelectedCountryRegion(countryRegion: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedCountryRegion(countryRegion)
        }
    }
}
