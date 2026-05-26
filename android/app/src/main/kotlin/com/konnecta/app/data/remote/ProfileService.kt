package com.konnecta.app.data.remote

import com.konnecta.app.data.model.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileService {
    private val client = SupabaseClient.client

    suspend fun getProfile(userId: String): Profile? {
        return try {
            client.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Profile>()
        } catch (e: Exception) {
            println("ProfileService: Error fetching profile: ${e.message}")
            null
        }
    }

    suspend fun getUserStats(userId: String, groupId: String): UserStats? {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Fetch passed visits to count them
            val pastPlans = client.postgrest["weekend_plans"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("group_id", groupId)
                        eq("status", "going")
                        lt("weekend_date", today)
                    }
                }
                .decodeList<UserPlanSummary>()
            
            val totalVisits = pastPlans.size

            // Get next 5 weekends
            val upcomingPlans = client.postgrest["weekend_plans"]
                .select(columns = Columns.raw("weekend_date, status")) {
                    filter {
                        eq("user_id", userId)
                        eq("group_id", groupId)
                        gte("weekend_date", today)
                    }
                    order("weekend_date", Order.ASCENDING)
                    limit(5)
                }
                .decodeList<UserPlanSummary>()

            UserStats(
                totalVisits = totalVisits,
                upcomingPlans = upcomingPlans
            )
        } catch (e: Exception) {
            println("ProfileService: Error fetching user stats: ${e.message}")
            null
        }
    }
}
