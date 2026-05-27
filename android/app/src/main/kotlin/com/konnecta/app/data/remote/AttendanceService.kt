package com.konnecta.app.data.remote

import com.konnecta.app.data.model.MembershipWithProfile
import com.konnecta.app.data.model.PlanWithProfile
import com.konnecta.app.data.model.Profile
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceService @Inject constructor() {
    private val client = SupabaseClient.client

    @Serializable
    private data class AttendanceUpsert(
        val user_id: String,
        val group_id: String,
        val weekend_date: String,
        val status: String,
        val comment: String? = null
    )

    @Serializable
    private data class CommentUpsert(
        val user_id: String,
        val group_id: String,
        val weekend_date: String,
        val comment: String
    )

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
            .select(columns = Columns.raw("user_id, role, profiles(id, full_name, avatar_url, email, updated_at)")) {
                filter {
                    eq("group_id", groupId)
                }
            }
            .decodeList<MembershipWithProfile>()
        return memberships.map { it.profiles }
    }

    suspend fun updateAttendance(
        userId: String,
        groupId: String,
        weekendDate: String,
        status: String,
        comment: String? = null
    ): Boolean {
        return try {
            client.postgrest["weekend_plans"].upsert(
                AttendanceUpsert(userId, groupId, weekendDate, status, comment)
            ) {
                onConflict = "user_id,group_id,weekend_date"
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "AttendanceService: Error updating attendance")
            false
        }
    }

    suspend fun updateComment(
        userId: String,
        groupId: String,
        weekendDate: String,
        comment: String
    ): Boolean {
        return try {
            client.postgrest["weekend_plans"].upsert(
                CommentUpsert(userId, groupId, weekendDate, comment)
            ) {
                onConflict = "user_id,group_id,weekend_date"
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "AttendanceService: Error updating comment")
            false
        }
    }
}
