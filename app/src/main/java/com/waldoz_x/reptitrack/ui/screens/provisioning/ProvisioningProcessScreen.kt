package com.waldoz_x.reptitrack.ui.screens.provisioning

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvisioningProcessScreen(
    viewModel: ProvisioningViewModel = hiltViewModel(),
    onProvisioningSuccess: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCompleted: () -> Unit
)


 {
    val dbStep by viewModel.dbStep.collectAsState()
    val mqttStep by viewModel.mqttStep.collectAsState()
    val wifiStep by viewModel.wifiStep.collectAsState()
    val allDone by viewModel.allDone.collectAsState()
    val checkpointReached by viewModel.checkpointReached.collectAsState()
     val context =  LocalContext.current
     val provisioningError by viewModel.provisioningError.collectAsState()

     fun reintentarProceso() {
         viewModel.iniciarNuevaSesion()
     }

     val hasError = listOf(dbStep, mqttStep, wifiStep).any { it == ProvisioningViewModel.StepState.ERROR }

    LaunchedEffect(Unit) {
        viewModel.startProvisioning()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Proceso de Provisión", style = MaterialTheme.typography.titleLarge)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    hasError -> "Error al enviar credenciales"
                    allDone -> "Credenciales enviadas con éxito"
                    else -> "Enviando credenciales..."
                },
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = when {
                        hasError -> Color(0xFFF44336)
                        allDone -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.primary
                    }
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // Pasos
            ProvisionStepRow("Base de Datos", dbStep)
            ProvisionStepRow("MQTT", mqttStep)
            ProvisionStepRow("Wi-Fi", wifiStep)

            Spacer(modifier = Modifier.height(32.dp))

            if (allDone && !hasError) {
                Button(
                    onClick = {
                        if (checkpointReached) {
                            onNavigateToCompleted()
                        } else {
                            onProvisioningSuccess()
                        }
                    },
                    modifier = Modifier.widthIn(min = 200.dp) // Ancho mínimo pero se expande si es necesario
                ) {
                    Text("Continuar",
                        style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 18.sp
                    ))
                }
            }

            if (hasError) {
                Text(
                // Esta línea usa el operador Elvis ?: para proporcionar un valor por defecto
                // si la variable 'provisioningError' es null.
                //
                // La sintaxis general es:
                //    valor ?: valorPorDefecto
                //
                // Esto significa:
                //    - Si 'valor' NO es null, se usa 'valor'.
                //    - Si 'valor' ES null, se usa 'valorPorDefecto'.
                //
                // En este caso, si 'provisioningError' es null, se usará el texto
                // "No se pudo completar la provisión".
                //

                // NOTA: El operador Elvis (?:) solo comprueba si el valor de la izquierda es null,
                // y devuelve el valor de la derecha en ese caso.
                // No es un operador ternario general como en otros lenguajes que permite evaluar
                // cualquier condición booleana; sólo maneja el caso null de forma concisa.
                // condición ? valor_si_true : valor_si_false

                    provisioningError ?: "No se pudo completar la provisión"
                    ,
                    color = Color(0xFFF44336),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
                Button(
                    onClick = {
                        Toast.makeText(context, "Hubo un problema. Reinicia el dispositivo conectado", Toast.LENGTH_SHORT).show();
                        onNavigateToHome()
                        reintentarProceso()
                           },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    modifier = Modifier.widthIn(min = 200.dp) // Ancho mínimo pero se expande si es necesario
                ) {
                    Text("Reintentar", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProvisionStepRow(label: String, state: ProvisioningViewModel.StepState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(modifier = Modifier.width(24.dp)) {
            when (state) {
                ProvisioningViewModel.StepState.LOADING -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                ProvisioningViewModel.StepState.SUCCESS -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                ProvisioningViewModel.StepState.ERROR -> Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                ProvisioningViewModel.StepState.IDLE -> Spacer(modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
