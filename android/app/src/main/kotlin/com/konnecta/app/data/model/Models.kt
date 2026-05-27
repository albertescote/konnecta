package com.konnecta.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val full_name: String?,
    val avatar_url: String?,
    val email: String,
    val updated_at: String?
)

@Serializable
data class Group(
    val id: String,
    val name: String,
    val slug: String,
    val created_by: String,
    val invite_token: String? = null,
    val invite_token_expires_at: String? = null,
    val role: String? = null // local UI state
)

@Serializable
data class MembershipWithGroup(
    val role: String,
    val groups: Group
)

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

@Serializable
data class Activity(
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
    val day_of_week: String
)

@Serializable
data class WeekendPlan(
    val id: String,
    val user_id: String,
    val group_id: String,
    val weekend_date: String,
    val status: String,
    val comment: String?,
    val updated_at: String
)

@Serializable
data class ParticipationUpdate(
    val activity_id: String,
    val user_id: String,
    val additional_participants: Int
)

@Serializable
data class ActivityUpdate(
    val title: String,
    val description: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val start_time: String? = null,
    val end_time: String? = null
)


@Serializable
data class PlanWithProfile(
    val user_id: String,
    val status: String,
    val comment: String?,
    val profiles: Profile
)

@Serializable
data class MembershipWithProfile(
    val user_id: String,
    val role: String,
    val profiles: Profile
)

@Serializable
data class LeaderboardEntry(
    val full_name: String?,
    val avatar_url: String?,
    val email: String,
    val visit_count: Int
)

@Serializable
data class PlanWithProfileOnly(
    val user_id: String,
    val profiles: Profile
)

data class AttendanceState(
    val going: List<Pair<Profile, String?>> = emptyList(),
    val notGoing: List<Pair<Profile, String?>> = emptyList(),
    val pending: List<Pair<Profile, String?>> = emptyList(),
    val unanswered: List<Pair<Profile, String?>> = emptyList()
)

data class DashboardState(
    val attendance: AttendanceState = AttendanceState(),
    val activities: List<ActivityWithParticipants> = emptyList(),
    val futureActivities: List<ActivityWithParticipants> = emptyList(),
    val weather: WeatherDay? = null,
    val fullForecast: List<WeatherDay?> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val userGroups: List<Group> = emptyList(),
    val activeGroup: Group? = null,
    val currentUserProfile: Profile? = null,
    val currentUserStatus: String? = null,
    val currentUserComment: String? = null,
    val isLoading: Boolean = false,
    val isFutureActivitiesLoading: Boolean = false,
    val error: String? = null
)

@Serializable
data class UserPlanSummary(
    val weekend_date: String,
    val status: String
)

@Serializable
data class UserStats(
    val totalVisits: Int,
    val upcomingPlans: List<UserPlanSummary>
)

@Serializable
data class WeatherDay(
    val date: String,
    val maxTemp: Int,
    val minTemp: Int,
    val code: Int
)

@Serializable
data class WeatherForecast(
    val summary: WeatherDay,
    val details: List<WeatherDay?>
)
