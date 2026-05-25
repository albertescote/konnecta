package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konnecta.app.data.remote.WeatherDay

@Composable
fun WeatherCard(
    weather: WeatherDay?
) {
    if (weather == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MASSA AVIAT PER PREDIR EL TEMPS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getWeatherIcon(weather.code),
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "VALLS",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = getWeatherDescription(weather.code),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${weather.maxTemp}°",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = "${weather.minTemp}°",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun getWeatherIcon(code: Int): String {
    return when {
        code <= 1 -> "☀️"
        code == 2 -> "⛅"
        code == 3 -> "☁️"
        code <= 48 -> "🌫️"
        code <= 67 -> "🌧️"
        code <= 77 -> "❄️"
        code <= 82 -> "🌦️"
        else -> "⛈️"
    }
}

fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Sol radiant"
        1, 2 -> "Cel clar"
        3 -> "Nuvolat"
        45, 48 -> "Boira"
        in 51..55 -> "Plugim suau"
        in 61..67 -> "Pluja"
        in 71..77 -> "Neu"
        in 80..82 -> "Ruixats"
        else -> "Tempesta"
    }
}
