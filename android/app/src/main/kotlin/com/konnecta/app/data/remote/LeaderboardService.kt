package com.konnecta.app.data.remote

import com.konnecta.app.data.model.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardService @Inject constructor() {
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
                    limit(500)
                }
                .decodeList<PlanWithProfileOnly>()

            plans.groupBy { it.user_id }
                .map { (_, userPlans) ->
                    val profile = userPlans.first().profiles
                    LeaderboardEntry(
                        full_name = profile.full_name,
                        avatar_url = profile.avatar_url,
                        email = profile.email,
                        visit_count = userPlans.size
                    )
                }
                .sortedByDescending { it.visit_count }
        } catch (e: Exception) {
            Timber.e(e, "LeaderboardService: Error fetching leaderboard")
            emptyList()
        }
    }
}
