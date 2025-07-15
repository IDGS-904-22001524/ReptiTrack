package com.waldoz_x.reptitrack.ui.screens.countryregion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale // Necesario para obtener los países

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryRegionSelectionScreen(
    onBackClick: () -> Unit,
    viewModel: CountryRegionSelectionViewModel = hiltViewModel() // Inyecta el ViewModel
) {
    val countries by viewModel.filteredCountries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCountryRegion by viewModel.selectedCountryRegion.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar País/Región") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Buscar país o región") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Lista de países/regiones
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(countries) { country ->
                    CountryRegionItem(
                        countryRegion = country,
                        isSelected = country == selectedCountryRegion,
                        onCountryRegionSelected = { viewModel.setSelectedCountryRegion(it) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun CountryRegionItem(
    countryRegion: String,
    isSelected: Boolean,
    onCountryRegionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCountryRegionSelected(countryRegion) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = countryRegion,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = isSelected,
            onClick = { onCountryRegionSelected(countryRegion) }
        )
    }
}

// Preview para la pantalla de selección de País/Región
@Preview(showBackground = true)
@Composable
fun CountryRegionSelectionScreenPreview() {
    MaterialTheme {
        CountryRegionSelectionScreen(onBackClick = {})
    }
}
