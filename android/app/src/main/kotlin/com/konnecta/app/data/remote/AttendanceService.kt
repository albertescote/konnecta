package com.konnecta.app.data.remote

import com.konnecta.app.data.model.Profile
import com.konnecta.app.data.model.WeekendPlan
import io.github.jan_tennert.supabase.postgrest.postgrest
import io.github.jan_tennert.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

@Serializable
data class PlanWithProfile(
    val user_id: String,
    val status: String,
    val comment: String?,
    val profiles: Profile
)

@Serializable
data class MembershipWithProfile(
    val profiles: Profile
)

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
}
