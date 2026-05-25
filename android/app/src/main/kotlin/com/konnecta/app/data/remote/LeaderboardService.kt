package com.konnecta.app.data.remote

import com.konnecta.app.data.model.Profile
import io.github.jan_tennert.supabase.postgrest.postgrest
import io.github.jan_tennert.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class LeaderboardEntry(
    val full_name: String?,
    val avatar_url: String?,
    val email: String,
    val visit_count: Int
)

@Serializable
private data class PlanWithProfileOnly(
    val user_id: String,
    val profiles: Profile
)

class LeaderboardService {
    private val client = SupabaseClient.client

    suspend fun getLeaderboard(groupId: String): List<LeaderboardEntry> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        return try {
            val plans = client.postgrest["weekend_plans"]
                .select(columns = Columns.raw("user_id, profiles(id, full_name, avatar_url, email, updated_at)")) {
                    filter {
                        eq("status", "going")
                        eq("group_id", groupId)
                        lt("weekend_date", today)
                    }
                }
                .decodeList<PlanWithProfileOnly>()

            val userCounts = plans.groupBy { it.user_id }
                .map { (userId, userPlans) ->
                    val profile = userPlans.first().profiles
                    LeaderboardEntry(
                        full_name = profile.full_name,
                        avatar_url = profile.avatar_url,
                        email = profile.email,
                        visit_count = userPlans.size
                    )
                }
                .sortedByDescending { it.visit_count }
                .take(5)

            userCounts
        } catch (e: Exception) {
            emptyList()
        }
    }
}
