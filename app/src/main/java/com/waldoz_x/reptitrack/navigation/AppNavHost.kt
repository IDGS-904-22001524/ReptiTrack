package com.waldoz_x.reptitrack.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.ui.Modifier
import com.waldoz_x.reptitrack.ui.screens.home.HomeRoute
import com.waldoz_x.reptitrack.ui.screens.settings.SettingsScreen
import com.waldoz_x.reptitrack.ui.screens.timezone.TimeZoneSelectionScreen
import com.waldoz_x.reptitrack.ui.screens.countryregion.CountryRegionSelectionScreen
import com.waldoz_x.reptitrack.ui.screens.mqtt.MqttSettingsScreen
import com.waldoz_x.reptitrack.ui.screens.terrariumdetail.TerrariumDetailScreen

// Define las rutas de navegación como constantes para evitar errores de tipeo
object Destinations {
    const val HOME_ROUTE = "home_route"
    const val SETTINGS_ROUTE = "settings_route"
    const val TIME_ZONE_SELECTION_ROUTE = "time_zone_selection_route"
    const val COUNTRY_REGION_SELECTION_ROUTE = "country_region_selection_route"
    const val MQTT_SETTINGS_ROUTE = "mqtt_settings_route"
    // ¡CORREGIDO! TERRARIUM_ID_ARG debe ser inicializado antes de ser usado en TERRARIUM_DETAIL_ROUTE
    const val TERRARIUM_ID_ARG = "terrariumId" // Nombre del argumento para el ID del terrario
    // ¡NUEVO! Ruta para la pantalla de detalle del terrario con un argumento
    const val TERRARIUM_DETAIL_ROUTE = "terrarium_detail_route/{${TERRARIUM_ID_ARG}}"

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
                    // Navega al detalle del terrario con el ID real
                    // ¡CORREGIDO! Eliminar la plantilla de cadena redundante
                    navController.navigate(Destinations.TERRARIUM_DETAIL_ROUTE.replace("{${Destinations.TERRARIUM_ID_ARG}}", terrariumId))
                },
                navigateToSettings = { navController.navigate(Destinations.SETTINGS_ROUTE) }
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
                        Destinations.TERRARIUM_DETAIL_ROUTE.replace("{${Destinations.TERRARIUM_ID_ARG}}", "placeholder_terrarium_id")
                    )
                }
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

        composable(Destinations.MQTT_SETTINGS_ROUTE) {
            MqttSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ¡NUEVO! Ruta para la pantalla de detalle del terrario
        composable(
            route = Destinations.TERRARIUM_DETAIL_ROUTE,
            arguments = listOf(navArgument(Destinations.TERRARIUM_ID_ARG) { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val terrariumId = backStackEntry.arguments?.getString(Destinations.TERRARIUM_ID_ARG)
            if (terrariumId != null) {
                TerrariumDetailScreen(
                    terrariumId = terrariumId,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                // Manejar error o navegar de vuelta si el ID es nulo
                Log.e("AppNavHost", "Terrarium ID is null for detail route, popping back stack.")
                navController.popBackStack()
            }
        }
    }
}
