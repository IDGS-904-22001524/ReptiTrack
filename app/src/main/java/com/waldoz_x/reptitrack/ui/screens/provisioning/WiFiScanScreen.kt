package com.waldoz_x.reptitrack.presentation.wifi

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.WiFiAccessPoint
import com.waldoz_x.reptitrack.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiScanScreen(
    viewModel: WiFiScanViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSendingScreen: () -> Unit
) {
    val wifiList by viewModel.wifiList.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle(initialValue = "")
    var showPasswordDialog by remember { mutableStateOf(false) }
    var selectedNetwork by remember { mutableStateOf<WiFiAccessPoint?>(null) }
    var password by remember { mutableStateOf("") }
    var ssidManual by remember { mutableStateOf("") }

    val context = LocalContext.current

    val deviceDisconnected by viewModel.deviceDisconnected.collectAsState()

    var showDisconnectedDialog by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }


    LaunchedEffect(mensaje) {
        if (mensaje.isNotBlank()) {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    LaunchedEffect(deviceDisconnected) {
        if (!deviceDisconnected) {
            Log.i("ProvisioningRepo", "Dispositivo conectado")
            showDisconnectedDialog = false
        } else {
            Log.e("ProvisioningRepo", "Dispositivo desconectado")
            showDisconnectedDialog = true
        }
    }


    if (showDisconnectedDialog) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("Dispositivo desconectado") },
            text = { Text("El dispositivo se ha desconectado, intentelo de nuevo") },
            confirmButton = {
                TextButton(onClick = {
                    showDisconnectedDialog = false
                    viewModel.clearDisconnectedFlag()
                    onNavigateHome()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    LaunchedEffect(true) {
        viewModel.escanearRedes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona Red Wi-Fi") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.iniciarNuevaSesion()
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Redes disponibles",
                    style = MaterialTheme.typography.titleLarge
                )
                if (!isScanning) {
                    IconButton(onClick = { viewModel.escanearRedes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar redes")
                    }
                }
            }

            if (isScanning) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(wifiList) { wifi ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedNetwork = wifi
                                    password = ""
                                    showPasswordDialog = true
                                },
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 2.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            WiFiNetworkItem(
                                wifi = wifi,
                                onClick = {
                                    selectedNetwork = wifi
                                    password = ""
                                    showPasswordDialog = true
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Agregar red",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedNetwork = null
                                    ssidManual = ""
                                    password = ""
                                    showPasswordDialog = true
                                }
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

// Verifica si el diálogo de contraseña debe mostrarse
    if (showPasswordDialog) {
        // Crea un AlertDialog (cuadro de diálogo modal)
        AlertDialog(
            // Configura qué pasa cuando se hace clic fuera del diálogo (lo cierra)
            onDismissRequest = { /* No cerrar al tocar fuera */ },
            // Título del diálogo
            title = {
                // Muestra el nombre de la red WiFi seleccionada o un texto por defecto
                Text(text = selectedNetwork?.wifiName ?: "Agregar Red Wi-Fi")
            },
            // Contenido principal del diálogo
            text = {
                // Columna para organizar los elementos verticalmente
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Si no hay red seleccionada (es una red manual)
                    if (selectedNetwork == null) {
                        // Campo de texto para ingresar el SSID manualmente
                        OutlinedTextField(
                            value = ssidManual,  // Valor actual del SSID
                            onValueChange = { ssidManual = it },  // Actualiza el valor cuando cambia
                            label = { Text("Nombre de red (SSID)") },  // Texto de etiqueta
                            singleLine = true,  // Restringe a una sola línea
                            modifier = Modifier.fillMaxWidth()  // Ocupa todo el ancho disponible
                        )
                        // Espaciador vertical de 8dp
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Si la red requiere contraseña (no es abierta)
                    if ((selectedNetwork?.security?.toShort() ?: 1) != ESPConstants.WIFI_OPEN) {
                        // Campo de texto para la contraseña
                        OutlinedTextField(
                            value = password,  // Valor actual de la contraseña
                            onValueChange = { password = it },  // Actualiza la contraseña
                            label = { Text("Contraseña") },  // Texto de etiqueta
                            // Transformación visual: muestra texto normal o puntos según passwordVisible
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,  // Restringe a una sola línea
                            modifier = Modifier.fillMaxWidth(),  // Ocupa todo el ancho
                            // Icono al final del campo ( contraseña oculta o visivle)
                            trailingIcon = {
                                // Botón de icono para mostrar/ocultar contraseña
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible }  // Alterna la visibilidad
                                ) {
                                    // Icono que cambia según la visibilidad
                                    Icon(
                                        // Selecciona el icono apropiado de los recursos
                                        painter = painterResource(
                                            id = if (passwordVisible)
                                                R.drawable.visibility_24px
                                            else
                                                R.drawable.visibility_off_24px
                                        ),
                                        // Texto descriptivo para accesibilidad
                                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            }
                        )
                    } else {
                        // Mensaje para redes abiertas (sin contraseña)
                        Text("Red abierta. No requiere contraseña.")
                    }
                }
            },
            // Botón de confirmación (Conectar)
            confirmButton = {
                TextButton(
                    onClick = {
                        // Obtiene el SSID (de la red seleccionada o manual)
                        val ssid = selectedNetwork?.wifiName ?: ssidManual
                        // Cierra el diálogo
                        showPasswordDialog = false
                        // Guardar credenciales en el ViewModel
                        viewModel.saveWifiCredentials(ssid, password)
                        onNavigateToSendingScreen() // Navega a la pantalla de envío

                    }
                ) {
                    Text("Provisionar")
                }
            },
            // Botón de cancelar
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}




@Composable
fun WiFiNetworkItem(
    wifi: WiFiAccessPoint,
    onClick: () -> Unit,
) {
    val rssiLevel = getRssiLevel(wifi.rssi)
    val isLocked = wifi.security != ESPConstants.WIFI_OPEN.toInt()
    val tipoSeguridad = if (isLocked) "Protegida" else "Abierta"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = wifi.wifiName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = tipoSeguridad,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            painter = painterResource(id = getWiFiIconRes(rssiLevel, isLocked)),
            contentDescription = tipoSeguridad,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun getRssiLevel(rssi: Int): Int {
    return when {
        rssi >= -55 -> 4 // Excelente señal
        rssi >= -65 -> 3 // Buena señal
        rssi >= -75 -> 2 // Señal media
        else -> 1 // Señal baja
    }
}

fun getWiFiIconRes(rssiLevel: Int, locked: Boolean): Int {
    return when (rssiLevel) {
        1 -> if (locked) R.drawable.network_wifi_1_bar_locked_24px else R.drawable.network_wifi_1_bar_24px
        2 -> if (locked) R.drawable.network_wifi_2_bar_locked_24px else R.drawable.network_wifi_2_bar_24px
        3 -> if (locked) R.drawable.network_wifi_3_bar_locked_24px else R.drawable.network_wifi_3_bar_24px
        4 -> if (locked) R.drawable.network_wifi_locked_24px else R.drawable.network_wifi_24px
        else -> R.drawable.network_wifi_24px
    }
}