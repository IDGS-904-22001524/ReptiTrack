package com.waldoz_x.reptitrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.waldoz_x.reptitrack.R
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FanActuatorsCard(
    fan1Active: Boolean,
    fan2Active: Boolean,
    onToggleFan1: (Boolean) -> Unit,
    onToggleFan2: (Boolean) -> Unit
) {
    val cardColor = Color(0xFF90CAF9) // Light blue for fans
    val shadowElevation by animateDpAsState(targetValue = 8.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(shadowElevation, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_mode_fan_24),
                    contentDescription = "Ventiladores",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Control de Ventiladores",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fan 1 Control
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_mode_fan_24),
                        contentDescription = "Ventilador 1",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp).graphicsLayer(rotationZ = if (fan1Active) 360f else 0f)
                    )
                    Text(
                        text = "Ventilador 1",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Switch(
                        checked = fan1Active,
                        onCheckedChange = onToggleFan1,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = cardColor,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF616161)
                        )
                    )
                }
                // Fan 2 Control
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_mode_fan_24),
                        contentDescription = "Ventilador 2",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp).graphicsLayer(rotationZ = if (fan2Active) 360f else 0f)
                    )
                    Text(
                        text = "Ventilador 2",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Switch(
                        checked = fan2Active,
                        onCheckedChange = onToggleFan2,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = cardColor,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF616161)
                        )
                    )
                }
            }
        }
    }
}
