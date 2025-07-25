// com.waldoz_x.reptitrack.navigation/AppNavHost.kt
package com.waldoz_x.reptitrack.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.waldoz_x.reptitrack.presentation.wifi.WiFiScanScreen
import com.waldoz_x.reptitrack.ui.screens.home.HomeRoute
import com.waldoz_x.reptitrack.ui.screens.settings.SettingsScreen
import com.waldoz_x.reptitrack.ui.screens.timezone.TimeZoneSelectionScreen
import com.waldoz_x.reptitrack.ui.screens.countryregion.CountryRegionSelectionScreen
import com.waldoz_x.reptitrack.ui.screens.mqtt.MqttSettingsScreen // ¡NUEVO! Importa la pantalla de configuración MQTT
import com.waldoz_x.reptitrack.ui.screens.provisioning.BluetoothScanScreen
import com.waldoz_x.reptitrack.ui.screens.provisioning.CheckpointScreen
import com.waldoz_x.reptitrack.ui.screens.provisioning.CredentialsScreen
import com.waldoz_x.reptitrack.ui.screens.provisioning.ProvisioningProcessScreen
import com.waldoz_x.reptitrack.ui.screens.provisioning.SetupCompletedScreen

// Define las rutas de navegación como constantes para evitar errores de tipeo
object Destinations {
    const val HOME_ROUTE = "home_route"
    const val SETTINGS_ROUTE = "settings_route"
    const val TIME_ZONE_SELECTION_ROUTE = "time_zone_selection_route"
    const val COUNTRY_REGION_SELECTION_ROUTE = "country_region_selection_route"
    const val MQTT_SETTINGS_ROUTE = "mqtt_settings_route" // ¡NUEVO! Ruta para configuración MQTT
    const val TERRARIUM_DETAIL_ROUTE = "terrarium_detail_route/{terrariumId}"
    const val TERRARIUM_ID_ARG = "terrariumId"


    const val TERRARIUM_SETUP_CREDENTIALS = "terrarium_setup_credentials"
    const val BLUETOOTH_SCAN_ROUTE = "bluetooth_scan_route"
    const val TERRARIUM_SETUP_WIFI = "terrarium_setup_wifi"
    const val TERRARIUM_SETUP_SENDING = "terrarium_setup_sending"
    const val TERRARIUM_SETUP_SUCCESS = "terrarium_setup_success"
    const val TERRARIUM_SETUP_CHECKPOINT = "terrarium_setup_checkpoint"
    const val TERRARIUM_SETUP_COMPLETED = "terrarium_setup_completed"

}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Destinations.HOME_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Destinations.HOME_ROUTE) {
            HomeRoute(
                navigateToTerrariumDetail = { terrariumId ->
                    Log.d("AppNavHost", "Navegación a detalle de terrario $terrariumId (no implementada aún)")
                },
                navigateToSettings = { navController.navigate(Destinations.SETTINGS_ROUTE) },
                onAddTerrarium = { navController.navigate(Destinations.TERRARIUM_SETUP_CREDENTIALS) }            )
        }


        // Define una pantalla en el NavHost con la ruta "bluetooth_scan_route"
        composable(Destinations.BLUETOOTH_SCAN_ROUTE) {

            // Llama al Composable que dibuja la pantalla de escaneo Bluetooth
            BluetoothScanScreen(
                // Pasa el parámetro nombrado "onBackClick" que es una función lambda
                // BluetoothScanScreen está definida para recibir este parámetro como función:
                //    fun BluetoothScanScreen(onBackClick: () -> Unit) { ... }
                // Por eso debemos proporcionarle esta función para manejar el evento "volver"
                onBackClick = {
                    // Dentro de la lambda, se llama a navController.popBackStack()
                    // Esto hace que la navegación regrese a la pantalla anterior en la pila de navegación
                    navController.popBackStack()
                },
                onNavigateToWiFi = {
                    navController.navigate(Destinations.TERRARIUM_SETUP_WIFI)
                }
            )
        }
        composable(Destinations.TERRARIUM_SETUP_CHECKPOINT) {
            CheckpointScreen(
                onContinueClick = {
                    navController.navigate(Destinations.TERRARIUM_SETUP_CREDENTIALS) {
                        popUpTo(Destinations.TERRARIUM_SETUP_CHECKPOINT) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(Destinations.TERRARIUM_SETUP_COMPLETED) {
            SetupCompletedScreen(
                onContinueClick = {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        // Limpia toda la pila hasta HOME para evitar volver atrás a configuraciones
                        popUpTo(Destinations.HOME_ROUTE) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Destinations.TERRARIUM_SETUP_CREDENTIALS) {
            CredentialsScreen(

                onNextClick = { navController.navigate(Destinations.BLUETOOTH_SCAN_ROUTE) },
                onBackClick = {
                    // Dentro de la lambda, se llama a navController.popBackStack()
                    // Esto hace que la navegación regrese a la pantalla anterior en la pila de navegación
                    navController.popBackStack()
                },
            )
        }

        composable(Destinations.TERRARIUM_SETUP_SENDING) {
            ProvisioningProcessScreen(
                onProvisioningSuccess = {
                    navController.navigate(Destinations.TERRARIUM_SETUP_CHECKPOINT)
                },
                onNavigateToHome = {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = false }
                    }
                },
                onNavigateToCompleted = {
                    navController.navigate(Destinations.TERRARIUM_SETUP_COMPLETED) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                }
            )
        }



        composable(Destinations.TERRARIUM_SETUP_WIFI) {
            WiFiScanScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = false }
                    }
                },
                onNavigateToSendingScreen = {
                    navController.navigate(Destinations.TERRARIUM_SETUP_SENDING)
                }
            )
        }





        composable(Destinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                navigateToTimeZoneSelection = { navController.navigate(Destinations.TIME_ZONE_SELECTION_ROUTE) },
                navigateToCountryRegionSelection = { navController.navigate(Destinations.COUNTRY_REGION_SELECTION_ROUTE) },
                navigateToMqttSettings = { navController.navigate(Destinations.MQTT_SETTINGS_ROUTE) } // ¡NUEVO! Pasa el callback
            )
        }

        composable(Destinations.TIME_ZONE_SELECTION_ROUTE) {
            TimeZoneSelectionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Destinations.COUNTRY_REGION_SELECTION_ROUTE) {
            CountryRegionSelectionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ¡NUEVO! Ruta para la pantalla de configuración de MQTT
        composable(Destinations.MQTT_SETTINGS_ROUTE) {
            MqttSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }




        // Si tienes una ruta de detalle de terrario, asegúrate de que esté aquí
        // composable(Destinations.TERRARIUM_DETAIL_ROUTE) { backStackEntry ->
        //     val terrariumId = backStackEntry.arguments?.getString(Destinations.TERRARIUM_ID_ARG)
        //     if (terrariumId != null) {
        //         // Aquí podrías tener una TerrariumDetailRoute si la creas,
        //         // por ahora usamos SensorDetailRoute como placeholder
        //         SensorDetailRoute(
        //             sensorId = terrariumId, // Usamos terrariumId como sensorId por ahora
        //             onBackClick = { navController.popBackStack() }
        //         )
        //     } else {
        //         // Manejar error o navegar de vuelta si el ID es nulo
        //         Log.e("Navigation", "Terrarium ID is null for detail route")
        //         navController.popBackStack()
        //     }
        // }
    }
}


