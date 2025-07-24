package com.waldoz_x.reptitrack.ui.screens.provisioning

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waldoz_x.reptitrack.presentation.provisioning.ProvisioningViewModel
import kotlinx.coroutines.delay

@Composable
fun ProvisioningProcessScreen(
    viewModel: ProvisioningViewModel = hiltViewModel(),
    onProvisioningSuccess: () -> Unit,
    onProvisioningFailed: () -> Unit
) {
    val dbStep by viewModel.dbStep.collectAsState()
    val mqttStep by viewModel.mqttStep.collectAsState()
    val wifiStep by viewModel.wifiStep.collectAsState()
    val allDone by viewModel.allDone.collectAsState()

    // Inicia la provisión al cargar la pantalla
    LaunchedEffect(Unit) {
        viewModel.startProvisioning()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Configurando dispositivo...",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProvisionStepRow("Base de Datos", dbStep)
        ProvisionStepRow("MQTT", mqttStep)
        ProvisionStepRow("Wi-Fi", wifiStep)

        Spacer(modifier = Modifier.height(32.dp))

        if (allDone) {
            Button(
                onClick = onProvisioningSuccess,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Continuar")
            }
        }

        if (listOf(dbStep, mqttStep, wifiStep).any { it == ProvisioningViewModel.StepState.ERROR }) {
            LaunchedEffect("provisioning_failed") {
                delay(1500)
                onProvisioningFailed()
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
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                ProvisioningViewModel.StepState.SUCCESS -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50), // Verde exitoso
                    modifier = Modifier.size(20.dp)
                )
                ProvisioningViewModel.StepState.ERROR -> Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFF44336), // Rojo de error
                    modifier = Modifier.size(20.dp)
                )
                ProvisioningViewModel.StepState.IDLE -> Spacer(modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
