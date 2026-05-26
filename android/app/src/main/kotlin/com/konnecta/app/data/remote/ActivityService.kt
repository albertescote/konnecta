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

    suspend fun createActivity(activity: Activity): Boolean {
        return try {
            client.postgrest["activities"].insert(activity)
            true
        } catch (e: Exception) {
            println("ActivityService: Error creating activity: ${e.message}")
            false
        }
    }

    suspend fun updateActivity(activityId: String, updates: Map<String, Any?>): Boolean {
        return try {
            client.postgrest["activities"].update(updates) {
                filter { eq("id", activityId) }
            }
            true
        } catch (e: Exception) {
            println("ActivityService: Error updating activity: ${e.message}")
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
            println("ActivityService: Error deleting activity: ${e.message}")
            false
        }
    }

    suspend fun updateParticipation(activityId: String, userId: String, isJoining: Boolean, additionalParticipants: Int = 0): Boolean {
        return try {
            if (isJoining) {
                client.postgrest["activity_participants"].upsert(
                    mapOf(
                        "activity_id" to activityId,
                        "user_id" to userId,
                        "additional_participants" to additionalParticipants
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
            println("ActivityService: Error updating participation: ${e.message}")
            false
        }
    }
}
