package com.konnecta.app.data.remote

import com.konnecta.app.data.model.Activity
import com.konnecta.app.data.model.ActivityUpdate
import com.konnecta.app.data.model.ActivityWithParticipants
import com.konnecta.app.data.model.ParticipationUpdate
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityService @Inject constructor() {
    private val client = SupabaseClient.client

    suspend fun getActivities(
        weekendDate: String,
        groupId: String
    ): List<ActivityWithParticipants> {
        return client.postgrest["activities"]
            .select(columns = Columns.raw("*, activity_participants(user_id, additional_participants, profiles(*))")) {
                filter {
                    eq("weekend_date", weekendDate)
                    eq("group_id", groupId)
                }
            }
            .decodeList<ActivityWithParticipants>()
    }

    suspend fun getFutureActivities(
        today: String,
        groupId: String
    ): List<ActivityWithParticipants> {
        return client.postgrest["activities"]
            .select(columns = Columns.raw("*, activity_participants(user_id, additional_participants, profiles(*))")) {
                filter {
                    gte("start_date", today)
                    eq("group_id", groupId)
                }
                limit(50)
            }
            .decodeList<ActivityWithParticipants>()
    }

    suspend fun createActivity(activity: Activity): Boolean {
        return try {
            client.postgrest["activities"].insert(activity)
            true
        } catch (e: Exception) {
            Timber.e(e, "ActivityService: Error creating activity")
            false
        }
    }

    suspend fun updateActivity(activityId: String, update: ActivityUpdate): Boolean {
        return try {
            client.postgrest["activities"].update(update) {
                filter { eq("id", activityId) }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "ActivityService: Error updating activity %s", activityId)
            false
        }
    }

    suspend fun deleteActivity(activityId: String): Boolean {
        return try {
            client.postgrest["activities"].delete {
                filter { eq("id", activityId) }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "ActivityService: Error deleting activity %s", activityId)
            false
        }
    }

    suspend fun updateParticipation(
        activityId: String,
        userId: String,
        isJoining: Boolean,
        additionalParticipants: Int = 0
    ): Boolean {
        return try {
            if (isJoining) {
                client.postgrest["activity_participants"].upsert(
                    ParticipationUpdate(
                        activity_id = activityId,
                        user_id = userId,
                        additional_participants = additionalParticipants
                    )
                ) {
                    onConflict = "activity_id,user_id"
                }
            } else {
                client.postgrest["activity_participants"].delete {
                    filter {
                        eq("activity_id", activityId)
                        eq("user_id", userId)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "ActivityService: Error updating participation for activity %s", activityId)
            false
        }
    }
}
