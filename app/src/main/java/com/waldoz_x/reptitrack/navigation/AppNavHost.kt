package com.waldoz_x.reptitrack.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.waldoz_x.reptitrack.presentation.wifi.WiFiScanScreen
import com.waldoz_x.reptitrack.ui.screens.home.HomeRoute
import com.waldoz_x.reptitrack.ui.screens.settings.SettingsScreen
import com.waldoz_x.reptitrack.ui.screens.timezone.TimeZoneSelectionScreen
import com.waldoz_x.reptitrack.ui.screens.countryregion.CountryRegionSelectionScreen
import com.waldoz_x.reptitrack.ui.screens.mqtt.MqttSettingsScreen
import com.waldoz_x.reptitrack.ui.screens.terrariumdetail.TerrariumDetailScreen
import com.waldoz_x.reptitrack.ui.screens.provisioning.*


// Define las rutas de navegación como constantes para evitar errores de tipeo
object Destinations {
    const val HOME_ROUTE = "home_route"
    const val SETTINGS_ROUTE = "settings_route"
    const val TIME_ZONE_SELECTION_ROUTE = "time_zone_selection_route"
    const val COUNTRY_REGION_SELECTION_ROUTE = "country_region_selection_route"
    const val MQTT_SETTINGS_ROUTE = "mqtt_settings_route"

    const val TERRARIUM_ID_ARG = "terrariumId"
    const val TERRARIUM_DETAIL_ROUTE = "terrarium_detail_route/{$TERRARIUM_ID_ARG}"

    const val TERRARIUM_SETUP_CREDENTIALS = "terrarium_setup_credentials"
    const val BLUETOOTH_SCAN_ROUTE = "bluetooth_scan_route"
    const val TERRARIUM_SETUP_WIFI = "terrarium_setup_wifi"
    const val TERRARIUM_SETUP_SENDING = "terrarium_setup_sending"
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
                    navController.navigate(
                        Destinations.TERRARIUM_DETAIL_ROUTE.replace(
                            "{${Destinations.TERRARIUM_ID_ARG}}", terrariumId
                        )
                    )
                },
                navigateToSettings = { navController.navigate(Destinations.SETTINGS_ROUTE) },
                onAddTerrarium = { navController.navigate(Destinations.TERRARIUM_SETUP_CREDENTIALS) }
            )
        }

        composable(Destinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                navigateToTimeZoneSelection = { navController.navigate(Destinations.TIME_ZONE_SELECTION_ROUTE) },
                navigateToCountryRegionSelection = { navController.navigate(Destinations.COUNTRY_REGION_SELECTION_ROUTE) },
                navigateToMqttSettings = { navController.navigate(Destinations.MQTT_SETTINGS_ROUTE) },
                navigateToTerrariumDetailPlaceholder = {
                    navController.navigate(
                        Destinations.TERRARIUM_DETAIL_ROUTE.replace(
                            "{${Destinations.TERRARIUM_ID_ARG}}", "placeholder_terrarium_id"
                        )
                    )
                }
            )
        }

        composable(Destinations.TIME_ZONE_SELECTION_ROUTE) {
            TimeZoneSelectionScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Destinations.COUNTRY_REGION_SELECTION_ROUTE) {
            CountryRegionSelectionScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Destinations.MQTT_SETTINGS_ROUTE) {
            MqttSettingsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Destinations.TERRARIUM_DETAIL_ROUTE,
            arguments = listOf(navArgument(Destinations.TERRARIUM_ID_ARG) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val terrariumId = backStackEntry.arguments?.getString(Destinations.TERRARIUM_ID_ARG)
            if (terrariumId != null) {
                TerrariumDetailScreen(
                    terrariumId = terrariumId,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                Log.e("AppNavHost", "Terrarium ID is null for detail route")
                navController.popBackStack()
            }
        }

        composable(Destinations.TERRARIUM_SETUP_CREDENTIALS) {
            CredentialsScreen(
                onNextClick = { navController.navigate(Destinations.BLUETOOTH_SCAN_ROUTE) },
                onBackClick = { navController.popBackStack() }
            )
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

        composable(Destinations.TERRARIUM_SETUP_CHECKPOINT) {
            CheckpointScreen(
                onContinueClick = {
                    navController.navigate(Destinations.TERRARIUM_SETUP_CREDENTIALS) {
                        popUpTo(Destinations.TERRARIUM_SETUP_CHECKPOINT) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Destinations.TERRARIUM_SETUP_COMPLETED) {
            SetupCompletedScreen(
                onContinueClick = {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

