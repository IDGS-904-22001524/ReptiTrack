// com.waldoz_x.reptitrack/ReptilTrackApplication.kt
package com.waldoz_x.reptitrack

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// La clase Application de tu app, anotada con @HiltAndroidApp
// Esto le dice a Hilt que genere el componente principal de la aplicación.
@HiltAndroidApp
class ReptilTrackApplication : Application() {
    // No se necesita código adicional aquí a menos que tengas inicializaciones específicas de la app.
}
