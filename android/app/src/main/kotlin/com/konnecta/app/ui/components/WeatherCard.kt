package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konnecta.app.data.model.WeatherDay

@Composable
fun WeatherCard(
    weather: WeatherDay?
) {
    if (weather == null) {
        val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .drawWithContent {
                    drawRoundRect(
                        color = Color.Gray.copy(alpha = 0.3f),
                        style = stroke,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                    )
                    drawContent()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MASSA AVIAT PER PREDIR EL TEMPS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.Gray
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            .clickable { /* Show Weather Modal */ }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getWeatherIcon(weather.code),
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "EL TEMPS DEL CAP DE SETMANA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3B82F6),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = getWeatherDescription(weather.code),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Thermostat,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${weather.maxTemp}°",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "MÍNIMA DE ${weather.minTemp}°",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = (-0.2).sp
                )
            }
        }
    }
}

fun getWeatherIcon(code: Int): String {
    return when (code) {
        0 -> "☀️"
        1, 2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        in 51..55 -> "🌦️"
        in 61..67 -> "🌧️"
        in 71..77 -> "❄️"
        in 80..82 -> "🌦️"
        else -> "⛈️"
    }
}

fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Cel clar"
        1, 2 -> "S'aclareix"
        3 -> "Nuvolat"
        45, 48 -> "Boira"
        in 51..55 -> "Plugim suau"
        in 61..67 -> "Pluja"
        in 71..77 -> "Neu"
        in 80..82 -> "Ruixats"
        else -> "Tempesta"
    }
}
