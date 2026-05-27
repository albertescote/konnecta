package com.konnecta.app.data.remote

import com.konnecta.app.BuildConfig
import com.konnecta.app.data.model.WeatherDay
import com.konnecta.app.data.model.WeatherForecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    private val lat = BuildConfig.WEATHER_LAT
    private val lng = BuildConfig.WEATHER_LNG
    private val json = Json { ignoreUnknownKeys = true }
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getWeekendWeather(fridayDateStr: String): WeatherForecast? = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
        
        try {
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
            
            if (weekendForecast.all { it == null }) return@withContext null
            
            // For the summary, use Saturday's weather if available, or the first available day
            val summary = weekendForecast[1] ?: weekendForecast.find { it != null } ?: return@withContext null
            
            WeatherForecast(summary, weekendForecast)
        } catch (e: Exception) {
            println("Weather: Error fetching data: ${e.message}")
            null
        }
    }

    private fun addDays(dateStr: String, days: Int): String {
        return try {
            val date = sdf.parse(dateStr) ?: return dateStr
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_YEAR, days)
            sdf.format(calendar.time)
        } catch (e: Exception) {
            dateStr
        }
    }
}
