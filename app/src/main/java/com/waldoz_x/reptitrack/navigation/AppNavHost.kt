// com.waldoz_x.reptitrack.navigation/AppNavHost.kt
package com.waldoz_x.reptitrack.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.waldoz_x.reptitrack.ui.screens.home.HomeRoute
import com.waldoz_x.reptitrack.ui.screens.settings.SettingsScreen
import com.waldoz_x.reptitrack.ui.screens.timezone.TimeZoneSelectionScreen
import com.waldoz_x.reptitrack.ui.screens.countryregion.CountryRegionSelectionScreen
import com.waldoz_x.reptitrack.ui.screens.mqtt.MqttSettingsScreen // ¡NUEVO! Importa la pantalla de configuración MQTT

// Define las rutas de navegación como constantes para evitar errores de tipeo
object Destinations {
    const val HOME_ROUTE = "home_route"
    const val SETTINGS_ROUTE = "settings_route"
    const val TIME_ZONE_SELECTION_ROUTE = "time_zone_selection_route"
    const val COUNTRY_REGION_SELECTION_ROUTE = "country_region_selection_route"
    const val MQTT_SETTINGS_ROUTE = "mqtt_settings_route" // ¡NUEVO! Ruta para configuración MQTT
    const val TERRARIUM_DETAIL_ROUTE = "terrarium_detail_route/{terrariumId}"
    const val TERRARIUM_ID_ARG = "terrariumId"
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
                navigateToSettings = { navController.navigate(Destinations.SETTINGS_ROUTE) }
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
