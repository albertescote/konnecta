package com.konnecta.app.utils

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.konnecta.app.data.model.*
import java.text.SimpleDateFormat
import java.util.Locale

object CalendarUtils {
    fun addToCalendar(context: Context, activity: ActivityWithParticipants) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val startTimeStr = "${activity.weekend_date} ${activity.start_time ?: "10:00"}"
        val startDate = sdf.parse(startTimeStr)?.time ?: System.currentTimeMillis()
        val endDate = startDate + 2 * 60 * 60 * 1000 // 2 hours duration

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, "${activity.title} [Konnecta]")
            .putExtra(CalendarContract.Events.DESCRIPTION, activity.description ?: "")
            .putExtra(CalendarContract.Events.EVENT_LOCATION, "Valls")
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate)

        context.startActivity(intent)
    }
}
