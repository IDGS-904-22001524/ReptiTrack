// com.waldoz_x.reptitrack.navigation/AppNavHost.kt
package com.waldoz_x.reptitrack.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.waldoz_x.reptitrack.ui.screens.home.HomeRoutes // Importa HomeRoutes (nombre actualizado)

// Define las rutas de navegación como constantes para evitar errores de tipeo
object Destinations {
    const val HOME_ROUTE = "home_route"
    // Comentado: No necesitamos la ruta de detalle por ahora
    // const val TERRARIUM_DETAIL_ROUTE = "terrarium_detail_route/{terrariumId}"
    // const val TERRARIUM_ID_ARG = "terrariumId"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Destinations.HOME_ROUTE // La pantalla de inicio de tu aplicación
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Define la ruta para la pantalla de inicio (Home)
        composable(Destinations.HOME_ROUTE) {
            HomeRoutes( // Llama a HomeRoutes
                // Cuando se hace clic en un terrario, por ahora solo logeamos.
                // La navegación a detalle se implementará más tarde.
                navigateToTerrariumDetail = { terrariumId ->
                    Log.d("AppNavHost", "Navegación a detalle de terrario $terrariumId (no implementada aún)")
                    // navController.navigate("${Destinations.TERRARIUM_DETAIL_ROUTE.split("/").first()}/$terrariumId")
                }
            )
        }

    }
}
