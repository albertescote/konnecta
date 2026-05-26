package com.konnecta.app.data.remote

import com.konnecta.app.data.model.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ActivityService {
    private val client = SupabaseClient.client

    suspend fun getActivities(weekendDate: String, groupId: String): List<ActivityWithParticipants> {
        return client.postgrest["activities"]
            .select(columns = Columns.raw("*, activity_participants(user_id, additional_participants, profiles(*))")) {
                filter {
                    eq("weekend_date", weekendDate)
                    eq("group_id", groupId)
                }
            }
            .decodeList<ActivityWithParticipants>()
    }

    suspend fun getFutureActivities(today: String, groupId: String): List<ActivityWithParticipants> {
        return client.postgrest["activities"]
            .select(columns = Columns.raw("*, activity_participants(user_id, additional_participants, profiles(*))")) {
                filter {
                    gte("start_date", today)
                    eq("group_id", groupId)
                }
            }
            .decodeList<ActivityWithParticipants>()
    }

    suspend fun createActivity(activity: Activity): Activity {
        return client.postgrest["activities"]
            .insert(activity)
            .decodeSingle<Activity>()
    }
}
