package com.waldoz_x.reptitrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview // Importar Preview
import androidx.compose.ui.unit.dp
import com.waldoz_x.reptitrack.R

@Composable
fun FoodDispenserCard(
    isActive: Boolean,
    loads: Int,
    onDispense: (Boolean) -> Unit,
    onRecharge: () -> Unit // Nueva función de callback para recargar
) {
    val activeColor = Color(0xFFFFC107) // Amarillo para activo
    val inactiveColor = Color(0xFF607D8B) // Gris azulado para inactivo

    val dispenserColor by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 300)
    )
    val shadowElevation by animateDpAsState(targetValue = if (isActive) 12.dp else 4.dp)

    // Ajuste del color del icono principal
    val mainIconTint by animateColorAsState(
        targetValue = if (isActive) Color.White else activeColor, // Blanco cuando activo, amarillo cuando inactivo
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // Ajustada la altura para acomodar el nuevo botón
            .padding(8.dp)
            .shadow(shadowElevation, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = dispenserColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono grande y destacado
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        // Color de fondo del Box del icono: Amarillo cuando activo, Blanco cuando inactivo
                        color = if (isActive) activeColor else Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    // Cambiado a un icono más representativo para comida (ej. ic_food_bowl_24 o ic_pet_food_24)
                    // Asumiendo que tienes un drawable como R.drawable.ic_food_bowl_24
                    // Si no tienes uno, puedes usar un placeholder o crear uno SVG
                    painter = painterResource(id = R.drawable.ic_outline_distance_24), // <--- CAMBIAR ESTE ICONO
                    contentDescription = "Dispensador de comida",
                    tint = mainIconTint, // Usar el color animado para el tinte del icono
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dispensador de Comida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Indicador visual de cargas restantes con animación y colores vivos
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                repeat(5) { index ->
                    val filled = index < loads
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = if (filled) activeColor else Color(0xFFEEEEEE), // Color de relleno: amarillo si lleno, gris claro si vacío
                                shape = RoundedCornerShape(50)
                            )
                            .border(
                                2.dp,
                                // Borde: Blanco si lleno y activo, amarillo si lleno e inactivo, amarillo si vacío
                                if (filled && isActive) Color.White else if (filled && !isActive) activeColor else activeColor,
                                RoundedCornerShape(50)
                            )
                            .shadow(if (filled) 4.dp else 0.dp, RoundedCornerShape(50))
                    )
                    if (index < 4) Spacer(modifier = Modifier.width(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Botón de Dispensar
            Button(
                onClick = { onDispense(!isActive) },
                enabled = loads > 0 && !isActive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    // Color del contenido del botón (texto e icono)
                    contentColor = if (loads > 0) dispenserColor else Color.Gray // Más oscuro si no hay cargas
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(44.dp)
                    .fillMaxWidth(0.7f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_heat_24), // Icono de restaurante/comida (Mantengo este por ahora)
                    contentDescription = null,
                    tint = if (loads > 0) dispenserColor else Color.Gray, // Más oscuro si no hay cargas
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isActive) "Dispensando..." else "Dispensar",
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) activeColor else if (loads > 0) dispenserColor else Color.Gray // Texto del botón: amarillo si activo, color del dispensador si inactivo y con cargas, gris si sin cargas
                )
            }

            // Nuevo: Botón de Recargar, visible solo si no hay cargas
            if (loads == 0) {
                Spacer(modifier = Modifier.height(8.dp)) // Espacio entre botones
                Button(
                    onClick = onRecharge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeColor, // Color amarillo para recargar
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .fillMaxWidth(0.7f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outline_water_pump_24), // Icono de recargar
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recargar",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFoodDispenserCard() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Dispensador de Comida - Activo, 3 cargas", style = MaterialTheme.typography.titleMedium)
            FoodDispenserCard(isActive = true, loads = 3, onDispense = { /* Do nothing for preview */ }, onRecharge = {})

            Text("Dispensador de Comida - Inactivo, 5 cargas", style = MaterialTheme.typography.titleMedium)
            FoodDispenserCard(isActive = false, loads = 5, onDispense = { /* Do nothing for preview */ }, onRecharge = {})

            Text("Dispensador de Comida - Inactivo, 0 cargas", style = MaterialTheme.typography.titleMedium)
            // Para esta preview, simulamos la recarga estableciendo loads a 5
            FoodDispenserCard(isActive = false, loads = 0, onDispense = { /* Do nothing for preview */ }, onRecharge = { /* Lógica de recarga en preview */ })
        }
    }
}
