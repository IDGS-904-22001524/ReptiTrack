package com.waldoz_x.reptitrack.ui.screens.provisioning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckpointScreen(
    viewModel: CheckpointViewModel = hiltViewModel(),
    onContinueClick: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Punto de Control", style = MaterialTheme.typography.titleLarge)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Llevas la mitad de la configuración!",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Pulsa el botón para continuar con la segunda parte\n" +
                        "Si ocurre algún error en este paso, tendrás que iniciar la configuración desde cero",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.markCheckpointPassed()
                    onContinueClick()
                },
                modifier = Modifier.widthIn(min = 200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Text(
                    "Continuar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
