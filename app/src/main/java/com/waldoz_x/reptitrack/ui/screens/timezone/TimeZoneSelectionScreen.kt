// com.waldoz_x.reptitrack.ui.screens.timezone/TimeZoneSelectionScreen.kt
package com.waldoz_x.reptitrack.ui.screens.timezone

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
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeZoneSelectionScreen(
    onBackClick: () -> Unit,
    viewModel: TimeZoneViewModel = hiltViewModel()
) {
    val timeZones by viewModel.filteredTimeZones.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTimeZoneId by viewModel.selectedTimeZoneId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Zona Horaria") },
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
                label = { Text("Buscar zona horaria") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Lista de zonas horarias
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(timeZones) { timeZoneId ->
                    TimeZoneItem(
                        timeZoneId = timeZoneId,
                        isSelected = timeZoneId == selectedTimeZoneId,
                        onTimeZoneSelected = { viewModel.setSelectedTimeZoneId(it) }
                    )
                    HorizontalDivider() // ¡ACTUALIZADO!
                }
            }
        }
    }
}

@Composable
fun TimeZoneItem(
    timeZoneId: String,
    isSelected: Boolean,
    onTimeZoneSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTimeZoneSelected(timeZoneId) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = timeZoneId,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = isSelected,
            onClick = { onTimeZoneSelected(timeZoneId) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimeZoneSelectionScreenPreview() {
    MaterialTheme {
        TimeZoneSelectionScreen(onBackClick = {})
    }
}
