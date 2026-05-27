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
            println("ActivityService: Creating activity: ${activity.title}")
            client.postgrest["activities"].insert(activity)
            println("ActivityService: Activity created successfully")
            true
        } catch (e: Exception) {
            println("ActivityService: ERROR creating activity: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun updateActivity(activityId: String, update: ActivityUpdate): Boolean {
        return try {
            println("ActivityService: Updating activity $activityId")
            client.postgrest["activities"].update(update) {
                filter { eq("id", activityId) }
            }
            println("ActivityService: Activity updated successfully")
            true
        } catch (e: Exception) {
            println("ActivityService: ERROR updating activity: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteActivity(activityId: String): Boolean {
        return try {
            println("ActivityService: Deleting activity $activityId")
            client.postgrest["activities"].delete {
                filter { eq("id", activityId) }
            }
            println("ActivityService: Activity deleted successfully")
            true
        } catch (e: Exception) {
            println("ActivityService: ERROR deleting activity: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun updateParticipation(activityId: String, userId: String, isJoining: Boolean, additionalParticipants: Int = 0): Boolean {
        return try {
            if (isJoining) {
                println("ActivityService: Upserting participation for activity $activityId, user $userId, plusOne $additionalParticipants")
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
                println("ActivityService: Deleting participation for activity $activityId, user $userId")
                client.postgrest["activity_participants"].delete {
                    filter {
                        eq("activity_id", activityId)
                        eq("user_id", userId)
                    }
                }
            }
            println("ActivityService: Participation updated successfully")
            true
        } catch (e: Exception) {
            println("ActivityService: ERROR updating participation: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
