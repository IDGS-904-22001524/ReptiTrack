package com.waldoz_x.reptitrack.ui.components.sensorUtils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.waldoz_x.reptitrack.R

@Composable
fun getSensorIcon(sensorType: String): Painter {
    return when (sensorType) {
        "temperature" -> painterResource(id = R.drawable.ic_baseline_thermostat_24)
        "humidity" -> painterResource(id = R.drawable.ic_outline_humidity_high_24)
        "distance" -> painterResource(id = R.drawable.ic_outline_distance_24)
        "power" -> painterResource(id = R.drawable.ic_outline_settings_power_24)
        else -> painterResource(id = R.drawable.ic_baseline_cloud_24)
    }
}
