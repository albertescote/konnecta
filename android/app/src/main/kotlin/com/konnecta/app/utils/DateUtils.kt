package com.konnecta.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getUpcomingFriday(): Date {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        when (dayOfWeek) {
            Calendar.FRIDAY -> {
                // If it's Friday, return today at midnight
            }
            Calendar.SATURDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            Calendar.SUNDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -2)
            }
            else -> {
                // Move to next Friday
                val daysUntilFriday = (Calendar.FRIDAY - dayOfWeek + 7) % 7
                calendar.add(Calendar.DAY_OF_YEAR, daysUntilFriday)
            }
        }
        
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return calendar.time
    }

    fun formatDbDate(date: Date): String {
        return dbDateFormat.format(date)
    }

    fun getNextWeekends(count: Int = 10): List<String> {
        val weekends = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.time = getUpcomingFriday()
        
        for (i in 0 until count) {
            weekends.add(formatDbDate(calendar.time))
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        
        return weekends
    }
}
