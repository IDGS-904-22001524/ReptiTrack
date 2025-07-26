package com.waldoz_x.reptitrack.ui.screens.provisioning
import android.Manifest
import android.R.attr.contentDescription
import android.R.attr.tint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Importa el icono de volver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.waldoz_x.reptitrack.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.waldoz_x.reptitrack.data.model.ConnectionState


@OptIn(ExperimentalMaterial3Api::class) // Anotación para usar APIs experimentales de Material3 (como TopAppBar)
/*
 * Define una función Composable llamada BluetoothScanScreen.
 * Una función Composable es una función especial en Jetpack Compose que representa
 * un componente visual de la interfaz de usuario. Estas funciones permiten construir
 * el componente describiendo su estructura y apariencia, en lugar de indicar paso a paso
 * cómo debe mostrase cada elemento de dicho componente.
 */
@Composable
fun BluetoothScanScreen(
    viewModel: BluetoothScanViewModel = hiltViewModel(),
    onBackClick: () -> Unit, // Parámetro: función que no recibe argumentos y no devuelve nada (Unit),
                            // llamada cuando se presiona "volver"
    onNavigateToWiFi: () -> Unit
    ) {

    val dispositivos by viewModel.dispositivos.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val haEscaneado by viewModel.haEscaneado.collectAsState()
    val connectionStates by viewModel.connectionStates.collectAsState()

    val navegarAWifi = viewModel.navegarAWifi.collectAsState(initial = null)

    // Observa el evento para navegar solo cuando no sea null
    LaunchedEffect(navegarAWifi.value) {
        if (navegarAWifi.value != null) {
            onNavigateToWiFi()
        }
    }

    // Esta línea carga la animación desde el archivo radar.json en res/raw
    // y la guarda en la variable 'composition'.
    // Usamos 'rememberLottieComposition' para que se recuerde entre recomposiciones.
    // El 'by' permite acceder directamente al valor sin tener que usar .value
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.radar))

    // Crea un objeto LottieAnimatable que se usará para controlar la animación (reproducir, pausar, etc.)
    // Se recuerda con 'remember' para que no se cree de nuevo cada vez que el Composable se recomponga
    val lottieAnimatable = rememberLottieAnimatable()

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isScanning // Controla la reproducción con isScanning
    )

    val context = LocalContext.current
    val activity = LocalActivity.current


    LaunchedEffect(Unit) {
        viewModel.mensaje.collect { mensaje ->
            Log.d("BluetoothScanVM", mensaje)
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
        }
    }


    // Definir permisos requeridos segun la version de android
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Verifica si el dispositivo está usando **Android 12 (API 31)** o una versión superior.
        // Build.VERSION.SDK_INT devuelve el nivel de API actual del sistema operativo.
        // Build.VERSION_CODES.S representa Android 12.
        //
        // Si es Android 12 o superior, se requieren permisos específicos:
        // - BLUETOOTH_SCAN: para poder escanear dispositivos Bluetooth cercanos.
        // - BLUETOOTH_CONNECT: para conectarse a dispositivos.
        // Aunque ACCESS_FINE_LOCATION *ya no es requerido oficialmente*, algunos dispositivos (como Huawei)
        // aún lo exigen de forma implícita para detectar dispositivos BLE. Por eso se incluye también.
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION // agregarlo para compatibilidad con dispositivos como Huawei
        )
    } else {
        // Android 11 (API 30) o versiones anteriores:
        // En estas versiones, NO existen los permisos BLUETOOTH_SCAN ni CONNECT
        // Para escanear dispositivos BLE, todavía se requiere ACCESS_FINE_LOCATION
        //  Por eso, en este caso solo agregamos ese permiso
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    // NOTA: NO se combinan los permisos en un solo arrayOf(...) es uno u otro dependiendo de la version


// Lanzador para solicitar permisos faltantes
// Creamos un lanzador (launcher) que se usará para solicitar múltiples permisos al usuario.
// Esto se hace con rememberLauncherForActivityResult, que es una función de Jetpack Compose.
// "remember" hace que el lanzador se mantenga en memoria durante recomposiciones del Composable.
    val permissionLauncher = rememberLauncherForActivityResult(

        // Aquí se pasa el contrato que define qué tipo de resultado esperamos:
        // en este caso, solicitar múltiples permisos.
        ActivityResultContracts.RequestMultiplePermissions()

// Esta parte define una función lambda que se ejecutará automáticamente cuando el usuario
// responda a la solicitud de permisos. Esta lambda recibe como parámetro un Map<String, Boolean>,
// donde cada clave es un nombre de permiso, y el valor es true (si fue concedido) o false (si fue denegado).
    ) { grantedMap ->

        // Verificamos si TODOS los permisos fueron concedidos.
        // `grantedMap.values` nos da una colección de valores Boolean (true o false)
        // `.all { it }` comprueba que todos los valores sean `true` (es decir, todos fueron aceptados)
        if (grantedMap.values.all { it }) {

            viewModel.limpiarEstados() // <- Limpia connectionStates manualmente antes de escanear

            // Si TODOS los permisos solicitados fueron concedidos,
            // ahora sí podemos iniciar el escaneo Bluetooth.
            // Esta llamada ocurre *después* de que el usuario aceptó los permisos
            // Osea para que se haga automaticamente
            viewModel.iniciarEscaneo(context)



        } else {
            // Si uno o más permisos fueron denegados, los filtramos.

            // `grantedMap.filter { !it.value }` devuelve un Map con solo los permisos que tienen valor `false`
            // `.keys` extrae solo los nombres de los permisos denegados
            val deniedPermissions = grantedMap.filter { !it.value }.keys

            // Convertimos esa lista de permisos denegados en un string separado por comas,
            // usando nombres más amigables si son conocidos.
            val deniedListString = deniedPermissions.joinToString(", ") {
                // Usamos un when para identificar el permiso y devolver un nombre más claro
                when (it) {
                    Manifest.permission.BLUETOOTH_SCAN -> "BLUETOOTH_SCAN"
                    Manifest.permission.BLUETOOTH_CONNECT -> "BLUETOOTH_CONNECT"
                    Manifest.permission.ACCESS_FINE_LOCATION -> "ACCESS_FINE_LOCATION"
                    else -> it // Si no es uno de los anteriores, se devuelve tal cual
                }
            }

            // Escribimos en el Log (útil para depuración) qué permisos fueron denegados
            Log.d("Permisos", "Permisos denegados: $deniedListString")

            // Mostramos un mensaje visual al usuario para informarle qué permisos faltaron
            Toast.makeText(context, "Permisos denegados", Toast.LENGTH_LONG).show()
        }
    }



    LaunchedEffect(Unit) {
        viewModel.solicitarActivarBluetooth.collect {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity?.startActivity(enableBtIntent)
        }
    }

    // Scaffold es un layout básico que maneja estructura común: top bar, floating button, contenido, etc.
    Scaffold(
        topBar = {
            // TopAppBar es el componente Material para la barra de título y acciones
            TopAppBar(
                title = { Text("Buscar Terrario") },   // Título de la barra superior
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
    ) { paddingValues -> // El cuerpo de Scaffold recibe un parámetro paddingValues con los paddings para evitar solapamiento
        // El componente Scaffold lo calcula automáticamente en función de los elementos que tenga (barra superior, inferior, etc.).

            // Column es un layout que organiza hijos verticalmente
            Column(
                modifier = Modifier  // Modifier define cómo se modifica o dimensiona este layout
                    .fillMaxSize() // Ocupa todo el espacio disponible (ancho y alto)
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                LottieAnimation(
                    // Le pasamos la animación que cargamos previamente (radar.json) para que se muestre en pantalla.
                    composition = composition,

                    // Indicamos el progreso de la animación usando una lambda: { progress }
                    // Esto le dice a Lottie que use el progreso animado que controla automáticamente
                    // la reproducción y pausa según el estado (isScanning)
                    // Usamos una lambda en lugar de pasar solo el valor para que Compose
                    // pueda volver a componer y actualizar la animación en tiempo real
                    progress = { progress  },

                    // Modificador que define el tamaño del componente (ancho y alto de 200 dp).
                    // 'Modifier.size(...)' es una forma rápida de definir ambas dimensiones a la vez.
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        dispositivos.isNotEmpty() && !isScanning -> "Dispositivos encontrados:"
                        dispositivos.isEmpty() && !isScanning && haEscaneado -> "No se encontraron dispositivos"
                        isScanning -> "Buscando dispositivos..."
                        else -> "Da click en el botón para iniciar el escaneo"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                // Bloque condicional que verifica si se está realizando un escaneo
                if (isScanning) {

                    Text(
                        text = "Asegúrate de que el dispositivo esté encendido y cerca del teléfono",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // Espaciador flexible que empuja el contenido hacia arriba (ocupa todo el espacio disponible abajo)
                    Spacer(modifier = Modifier.weight(1f))

                } else if (!isScanning && dispositivos.isEmpty() && haEscaneado) {

                    // Columna centrada para el mensaje de búsqueda finalizada
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Búsqueda finalizada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth() //
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = "Durante la conexión, asegúrate de lo siguiente:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp),
                            )

                        // Lista numerada con posibles recomendaciones
                        val recomendaciones = listOf(
                            "El dispositivo esté encendido.",
                            "No haya interferencias de otros dispositivos",
                            "El dispositivo esté en modo de emparejamiento",
                            "Acércate al dispositivo si estás a más de 5 metros"
                        )

                        recomendaciones.forEachIndexed { index, item ->
                            Text(
                                text = "${index + 1}. $item",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                                )
                        }
                    }
                    // Espaciador flexible que empuja el contenido hacia arriba (ocupa todo el espacio disponible abajo)
                    Spacer(modifier = Modifier.weight(1f))
                }
                else if (dispositivos.isNotEmpty()) {  // Si NO se está escaneando Y hay dispositivos encontrados

                    // Espaciador fijo de 20dp para separar el texto superior de la lista
                    Spacer(modifier = Modifier.height(20.dp))

                    // Lista perezosa (solo renderiza lo visible en pantalla)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()  // Ocupa todo el ancho disponible
                            .weight(1f),    // Ocupa todo el espacio vertical restante
                            // Espaciado vertical de 8dp entre cada elemento de la lista
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Itera sobre cada dispositivo en la lista
                        items(dispositivos) { dispositivo ->
                            // Tarjeta contenedora para cada dispositivo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()  // Ocupa todo el ancho
                                    .padding(horizontal = 8.dp) // Solo márgenes laterales            viewModel.conectarDispositivo(context, dispositivo)
                                    .then(
                                        if (connectionStates[dispositivo.address] != ConnectionState.CONNECTING) {
                                            Modifier.clickable {
                                                viewModel.conectarDispositivo(context, dispositivo)
                                            }
                                        } else {
                                            Modifier // Modificador vacío, sin clickable
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween // Separa contenido del indicador a la derecha
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Ícono Bluetooth (igual que antes)
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.bluetooth_24px),
                                                contentDescription = "Bluetooth",
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .fillMaxSize(),
                                                tint = Color(0xFF0082FC)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(dispositivo.name, style = MaterialTheme.typography.bodyLarge)
                                            Text(dispositivo.address, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    // Aquí mostramos el spinner o texto de error en rojo según estado
                                    when (connectionStates[dispositivo.address] ?: ConnectionState.IDLE) {
                                        ConnectionState.CONNECTING -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp) // espacio entre texto y spinner
                                            ) {
                                                Text(
                                                    text = "Conectando",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp,
                                                )
                                            }
                                        }
                                        ConnectionState.ERROR -> {
                                            Text(
                                                text = "Error: No se pudo conectar al dispositivo",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        else -> {
                                            // No mostrar nada si IDLE
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {  // Si NO se está escaneando Y NO hay dispositivos

                    // Espaciador flexible que centra el contenido (para cuando no hay resultados)
                    Spacer(modifier = Modifier.weight(1f))
                }
                Button(
                    onClick = {
                        // Verificar si la ubicación del sistema está activada (GPS o Red)
                        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                        if (!isLocationEnabled) {
                            Toast.makeText(context, "Activa la ubicación para encontrar dispositivos BLE", Toast.LENGTH_LONG).show()
                            return@Button // No continuar si la ubicación está desactivada
                        }

                        // Pedir solo permisos faltantes
                        val missingPermissions = permissions.toList().filter { p ->
                            androidx.core.content.ContextCompat.checkSelfPermission(context, p) !=
                                    android.content.pm.PackageManager.PERMISSION_GRANTED
                        }
                        if (missingPermissions.isEmpty()) {
                            viewModel.limpiarEstados() // <- Limpia connectionStates manualmente antes de escanear
                            viewModel.iniciarEscaneo(context)
                        } else {
                            permissionLauncher.launch(missingPermissions.toTypedArray())
                        }
                    },
                    enabled = !isScanning,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(if (haEscaneado) "Escanear otra vez" else "Escanear")
                }

            }
            }
    }
