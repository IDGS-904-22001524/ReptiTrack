// com.waldoz_x.reptitrack.ui.screens.mqtt/MqttSettingsScreen.kt
package com.waldoz_x.reptitrack.ui.screens.mqtt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waldoz_x.reptitrack.R // Importación necesaria para R.drawable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MqttSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: MqttSettingsViewModel = hiltViewModel()
) {
    // Observa el estado de conexión desde el ViewModel
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val savedUsername by viewModel.username.collectAsState()
    val savedPassword by viewModel.password.collectAsState()

    // Estados locales para los campos de texto editables
    var editableUsername by remember { mutableStateOf("") }
    var editablePassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    // Usamos LaunchedEffect para inicializar los campos de texto con los valores guardados
    // Se ejecuta una vez cuando el composable entra en la composición y cuando username/password cambian
    LaunchedEffect(savedUsername) {
        // Solo actualiza si el valor guardado es diferente del actual editable
        if (savedUsername != null && editableUsername != savedUsername) {
            editableUsername = savedUsername ?: ""
        }
    }
    LaunchedEffect(savedPassword) {
        // Solo actualiza si el valor guardado es diferente del actual editable
        if (savedPassword != null && editablePassword != savedPassword) {
            editablePassword = savedPassword ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar MQTT") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ingresa tus credenciales MQTT",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = editableUsername,
                onValueChange = { editableUsername = it },
                label = { Text("Usuario MQTT") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editablePassword,
                onValueChange = { editablePassword = it },
                label = { Text("Contraseña MQTT") },
                singleLine = true,
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val imagePainter = if (passwordVisibility)
                        painterResource(id = R.drawable.ic_baseline_visibility_24)
                    else painterResource(id = R.drawable.ic_baseline_visibility_off_24)
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(painter = imagePainter, contentDescription = "Toggle password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.saveMqttCredentials(editableUsername, editablePassword)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Credenciales")
            }

            // Botón para comprobar la conexión
            Button(
                onClick = {
                    viewModel.testMqttConnection(editableUsername, editablePassword)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Comprobar Conexión")
            }

            // ¡NUEVO! Botón para usar credenciales predeterminadas
            Button(
                onClick = {
                    viewModel.useDefaultMqttCredentials()
                    // Opcional: Limpiar los campos editables para reflejar que se usarán los valores por defecto
                    editableUsername = ""
                    editablePassword = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Usar Credenciales Predeterminadas")
            }

            // Mostrar el estado de la conexión
            Text(
                text = "Estado de Conexión: ${connectionStatus}",
                color = when (connectionStatus) {
                    "Conectando..." -> MaterialTheme.colorScheme.onSurfaceVariant
                    "Conectado" -> MaterialTheme.colorScheme.primary
                    "Fallo al conectar" -> MaterialTheme.colorScheme.error
                    "Error" -> MaterialTheme.colorScheme.error
                    "Credenciales guardadas" -> MaterialTheme.colorScheme.onSurfaceVariant
                    "Usando predeterminadas" -> MaterialTheme.colorScheme.onSurfaceVariant // Nuevo estado
                    else -> MaterialTheme.colorScheme.onSurface // Para "Desconectado" u otros estados
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MqttSettingsScreenPreview() {
    MaterialTheme {
        MqttSettingsScreen(onBackClick = {})
    }
}
