package com.konnecta.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM", Locale("ca"))

    fun getUpcomingFriday(): Date {
        return getFridayForDate(Date())
    }

    fun getFridayForDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        when (val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            Calendar.SUNDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -2)
            }

            Calendar.FRIDAY -> {
                // Already Friday
            }

            else -> {
                // For Mon-Thu, move to the next Friday
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

    fun isWeekend(date: Date): Boolean {
        val cal = Calendar.getInstance()
        cal.time = date
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    fun formatDbDate(date: Date): String {
        return dbDateFormat.format(date)
    }

    fun parseDbDate(dateStr: String): Date {
        return try {
            dbDateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    fun formatMonth(date: Date): String {
        val fullMonth = monthFormat.format(date).lowercase()
        // Remove "de " or "d'" prefix if present in the localized string
        return fullMonth.replace("de ", "").replace("d'", "").uppercase()
    }

    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
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

    fun formatDisplayDate(fridayStr: String): String {
        val friday = parseDbDate(fridayStr)
        val sat = addDays(friday, 1)
        val sun = addDays(friday, 2)

        val dayMonthFormat = SimpleDateFormat("d 'de' MMM", Locale("ca"))
        return "${dayMonthFormat.format(sat)} - ${dayMonthFormat.format(sun)}"
    }

    fun formatDayOfWeek(date: Date): String {
        return SimpleDateFormat("EEEE", Locale("ca")).format(date)
    }

    fun formatDayAndMonth(date: Date): String {
        return SimpleDateFormat("d MMM", Locale("ca")).format(date)
    }

    fun getDayOfMonth(date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    fun getMonthOfMonth(date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.MONTH) + 1
    }
}
