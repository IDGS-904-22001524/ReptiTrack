package com.waldoz_x.reptitrack.ui.screens.provisioning

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit, // Parámetro: función que no recibe argumentos y no devuelve nada (Unit),
    // llamada cuando se presiona "volver
    viewModel: CredentialsViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("reptitrack") }
    var proof by remember { mutableStateOf("xp4tzq7") }
    val context = LocalContext.current

    val round by viewModel.provisioningRound.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.iniciarNuevaSesion() // Esta función llama a provisioningRepository.clear()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Credenciales") },
                    navigationIcon = { // // navigationIcon es un composable que muestra un icono de navegación (como un botón de volver)
                        IconButton(  // IconButton es un botón que contiene un icono    y tiene la función onClick
                            // onClick es la acción a ejecutar al pulsar el botón
                            onClick = onBackClick)
                        {
                            // Icono de flecha de retroceso que se ajusta automáticamente según idioma (RTL(Right-to-Left) o LTR (Left-to-Right))
                            Icon(
                                // Objeto que contiene el icono de flecha 'back' autoajustable
                                Icons.AutoMirrored.Filled.ArrowBack,
                                // Descripción para accesibilidad (lectores de pantalla)
                                contentDescription = "Volver")
                        }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Ingresa tus credenciales",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Introduce las credenciales de tu dispositivo ReptiTrack",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(4.dp)
                        .height(28.dp),
                    shadowElevation = 0.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Paso $round/2",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            "Usuario",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),

                )

                OutlinedTextField(
                    value = proof,
                    onValueChange = { proof = it },
                    label = {
                        Text(
                            "Proof of Possession",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            //
            Button(
                onClick = {
                    if (viewModel.areCredentialsValid(username, proof)) {
                        viewModel.setCredentials(username, proof)
                        onNextClick()
                    } else {
                        Toast.makeText(
                            context,
                            "Por favor completa todos los campos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.widthIn(min = 200.dp) // Ancho mínimo pero se expande si es necesario
            ) {
                Text(
                    "Continuar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "¿Dónde encuentro mis credenciales?",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        "Las credenciales se encuentran en el manual de tu dispositivo " +
                                "o en una etiqueta adherida al mismo. Estas son necesarias para " +
                                "autenticar y asegurar que solo tú puedas configurar el dispositivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

        }
    }
}