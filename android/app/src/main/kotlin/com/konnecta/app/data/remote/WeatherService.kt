package com.konnecta.app.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

@Serializable
data class WeatherDay(
    val date: String,
    val maxTemp: Int,
    val minTemp: Int,
    val code: Int
)

@Serializable
data class WeatherForecast(
    val summary: WeatherDay,
    val details: List<WeatherDay?>
)

@Serializable
private data class OpenMeteoResponse(
    val daily: OpenMeteoDaily
)

@Serializable
private data class OpenMeteoDaily(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)

class WeatherService {
    private val lat = 41.2856
    private val lng = 1.2504
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getWeekendWeather(fridayDateStr: String): WeatherForecast? {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
        
        return try {
            val responseText = URL(url).readText()
            val data = json.decodeFromString<OpenMeteoResponse>(responseText)
            
            val friday = fridayDateStr
            val saturday = addDays(friday, 1)
            val sunday = addDays(friday, 2)
            val dates = listOf(friday, saturday, sunday)
            
            val weekendForecast = dates.map { date ->
                val dayIndex = data.daily.time.indexOf(date)
                if (dayIndex == -1) null
                else WeatherDay(
                    date = date,
                    maxTemp = data.daily.temperature_2m_max[dayIndex].toInt(),
                    minTemp = data.daily.temperature_2m_min[dayIndex].toInt(),
                    code = data.daily.weather_code[dayIndex]
                )
            }
            
            if (weekendForecast.all { it == null }) return null
            
            val summary = weekendForecast[1] ?: weekendForecast.find { it != null } ?: return null
            
            WeatherForecast(summary, weekendForecast)
        } catch (e: Exception) {
            null
        }
    }

    private fun addDays(dateStr: String, days: Int): String {
        // Simple mock for scaffolding, in real app use java.time
        return dateStr // This would be properly calculated
    }
}
