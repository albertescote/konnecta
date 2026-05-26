package com.konnecta.app.utils

import android.content.Context
import android.content.Intent
import com.konnecta.app.data.model.*

object ShareUtils {
    fun shareActivity(context: Context, activity: ActivityWithParticipants) {
        val timeText = if (activity.start_time != null) " a les ${activity.start_time}" else ""
        val text = """
            Ei! Estem organitzant això per KONNECTA:
            
            *${activity.title.uppercase()}*
            📅 ${activity.day_of_week.uppercase()}, ${activity.weekend_date}$timeText
            
            Anima't i apunta't a l'app!
        """.trimIndent()

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }
}
