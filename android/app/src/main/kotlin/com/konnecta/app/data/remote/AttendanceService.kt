package com.konnecta.app.data.remote

import com.konnecta.app.data.model.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class AttendanceService {
    private val client = SupabaseClient.client

    suspend fun getAttendance(weekendDate: String, groupId: String): List<PlanWithProfile> {
        return client.postgrest["weekend_plans"]
            .select(columns = Columns.raw("user_id, status, comment, profiles(id, full_name, avatar_url, email, updated_at)")) {
                filter {
                    eq("weekend_date", weekendDate)
                    eq("group_id", groupId)
                }
            }
            .decodeList<PlanWithProfile>()
    }

    suspend fun getGroupMembers(groupId: String): List<Profile> {
        val memberships = client.postgrest["group_memberships"]
            .select(columns = Columns.raw("profiles(id, full_name, avatar_url, email, updated_at)")) {
                filter {
                    eq("group_id", groupId)
                }
            }
            .decodeList<MembershipWithProfile>()
        return memberships.map { it.profiles }
    }

    suspend fun updateAttendance(userId: String, groupId: String, weekendDate: String, status: String, comment: String? = null): Boolean {
        return try {
            val plan = mutableMapOf<String, Any?>(
                "user_id" to userId,
                "group_id" to groupId,
                "weekend_date" to weekendDate,
                "status" to status
            )
            if (comment != null) {
                plan["comment"] = comment
            }
            client.postgrest["weekend_plans"].upsert(plan) {
                onConflict = "user_id,group_id,weekend_date"
            }
            true
        } catch (e: Exception) {
            println("Dashboard: Error updating attendance: ${e.message}")
            false
        }
    }

    suspend fun updateComment(userId: String, groupId: String, weekendDate: String, comment: String): Boolean {
        return try {
            val plan = mapOf(
                "user_id" to userId,
                "group_id" to groupId,
                "weekend_date" to weekendDate,
                "comment" to comment
            )
            client.postgrest["weekend_plans"].upsert(plan) {
                onConflict = "user_id,group_id,weekend_date"
            }
            true
        } catch (e: Exception) {
            println("Dashboard: Error updating comment: ${e.message}")
            false
        }
    }
}
