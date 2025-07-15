// com.waldoz_x.reptitrack.ui.screens.settings/SettingsScreen.kt
package com.waldoz_x.reptitrack.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waldoz_x.reptitrack.R
import com.waldoz_x.reptitrack.ui.theme.ReptiTrackTheme
import java.util.TimeZone
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    navigateToTimeZoneSelection: () -> Unit,
    navigateToCountryRegionSelection: () -> Unit,
    navigateToMqttSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val autoUpdateMobileData by viewModel.autoUpdateMobileData.collectAsState()
    val autoUpdateWifi by viewModel.autoUpdateWifi.collectAsState()
    val selectedTimeZoneId by viewModel.selectedTimeZoneId.collectAsState()
    val selectedCountryRegion by viewModel.selectedCountryRegion.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implementar función de comentarios/chat */ }) {
                        Icon(painter = painterResource(id = R.drawable.ic_outline_chat_bubble_24), contentDescription = "Comentarios")
                    }
                }
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            // Sección de Cuenta
            SettingsSection(title = "Cuenta") {
                SettingsCard {
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_baseline_account_circle_24),
                        text = "Iniciar sesión",
                        onClick = { /* TODO: Implementar inicio de sesión (requiere Firebase Auth) */ },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a") }
                    )
                }
            }

            // Opción para configurar credenciales MQTT
            SettingsCard {
                SettingsItem(
                    icon = painterResource(id = R.drawable.ic_baseline_cloud_24),
                    text = "Configurar MQTT",
                    onClick = navigateToMqttSettings,
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a") }
                )
            }


            // Sección de Compartir
            SettingsSection(title = "Compartir") {
                SettingsCard {
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_baseline_share_24),
                        text = "Se ha compartido",
                        onClick = { /* TODO: Implementar compartir */ },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) // ¡ACTUALIZADO!
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_baseline_qr_code_24),
                        text = "Mi código QR",
                        onClick = { /* TODO: Implementar QR */ },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a") }
                    )
                }
            }

            // Sección de Actualizaciones
            SettingsSection(title = "Actualizaciones") {
                SettingsCard {
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_outline_cell_wifi_24),
                        text = "Actualizar con datos móviles",
                        onClick = { viewModel.setAutoUpdateMobileData(!autoUpdateMobileData) },
                        trailingContent = {
                            Switch(
                                checked = autoUpdateMobileData,
                                onCheckedChange = { viewModel.setAutoUpdateMobileData(it) }
                            )
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) // ¡ACTUALIZADO!
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_baseline_wifi_24),
                        text = "Actualizar plug-ins vía Wi-Fi",
                        onClick = { viewModel.setAutoUpdateWifi(!autoUpdateWifi) },
                        trailingContent = {
                            Switch(
                                checked = autoUpdateWifi,
                                onCheckedChange = { viewModel.setAutoUpdateWifi(it) }
                            )
                        }
                    )
                }
            }

            // Sección de Zona Horaria
            SettingsSection(title = "Zona Horaria") {
                SettingsCard {
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_baseline_schedule_24),
                        text = "Zona Horaria",
                        onClick = navigateToTimeZoneSelection,
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedTimeZoneId ?: TimeZone.getDefault().id,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a")
                            }
                        }
                    )
                }
            }

            // Sección de País/Región
            SettingsSection(title = "País/Región") {
                SettingsCard {
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_outline_flag_2_24),
                        text = "País/Región",
                        onClick = navigateToCountryRegionSelection,
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedCountryRegion ?: Locale.getDefault().displayCountry,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a")
                            }
                        }
                    )
                }
            }

            // Sección de Información
            SettingsSection(title = "Información") {
                SettingsCard {
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_outline_search_24),
                        text = "Buscar actualizaciones",
                        onClick = { /* TODO: Implementar búsqueda de actualizaciones */ },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) // ¡ACTUALIZADO!
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_baseline_smartphone_24),
                        text = "Actualizaciones del dispositivo",
                        onClick = { /* TODO: Implementar actualizaciones del dispositivo */ },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) // ¡ACTUALIZADO!
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_baseline_help_24),
                        text = "Ayuda",
                        onClick = { /* TODO: Implementar ayuda */ },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) // ¡ACTUALIZADO!
                    SettingsItem(
                        icon = painterResource(id = R.drawable.ic_baseline_info_24),
                        text = "Acerca de",
                        onClick = { /* TODO: Implementar Acerca de */ },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir a") }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: Any?,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon is ImageVector) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            } else if (icon is Painter) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        trailingContent?.invoke()
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ReptiTrackTheme {
        SettingsScreen(
            onBackClick = {},
            navigateToTimeZoneSelection = {},
            navigateToCountryRegionSelection = {},
            navigateToMqttSettings = {}
        )
    }
}
