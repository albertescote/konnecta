package com.konnecta.app.data.remote

import com.konnecta.app.data.model.Activity
import com.konnecta.app.data.model.Profile
import io.github.jan_tennert.supabase.postgrest.postgrest
import io.github.jan_tennert.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantWithProfile(
    val user_id: String,
    val additional_participants: Int,
    val profiles: Profile
)

@Serializable
data class ActivityWithParticipants(
    val id: String,
    val title: String,
    val description: String?,
    val group_id: String,
    val start_date: String?,
    val end_date: String?,
    val start_time: String?,
    val end_time: String?,
    val creator_id: String,
    val weekend_date: String,
    val day_of_week: String,
    val activity_participants: List<ParticipantWithProfile> = emptyList()
)

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
