package com.konnecta.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konnecta.app.data.model.Activity
import com.konnecta.app.data.model.ActivityUpdate
import com.konnecta.app.data.model.ActivityWithParticipants
import com.konnecta.app.data.model.AttendanceState
import com.konnecta.app.data.model.DashboardState
import com.konnecta.app.data.model.Group
import com.konnecta.app.data.model.MembershipWithProfile
import com.konnecta.app.data.model.ParticipantWithProfile
import com.konnecta.app.data.remote.ActivityService
import com.konnecta.app.data.remote.AttendanceService
import com.konnecta.app.data.remote.GroupService
import com.konnecta.app.data.remote.LeaderboardService
import com.konnecta.app.data.remote.ProfileService
import com.konnecta.app.data.remote.WeatherService
import com.konnecta.app.utils.DateUtils
import com.konnecta.app.utils.withRetry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val attendanceService: AttendanceService,
    private val activityService: ActivityService,
    private val weatherService: WeatherService,
    private val leaderboardService: LeaderboardService,
    private val groupService: GroupService,
    private val profileService: ProfileService
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    private var currentUserId: String? = null
    private val dayOrder = mapOf("divendres" to 1, "dissabte" to 2, "diumenge" to 3)

    fun loadInitialData(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val profile = profileService.getProfile(userId)

                val groups = groupService.getUserGroups(userId)
                if (groups.isNotEmpty()) {
                    val activeGroup = _state.value.activeGroup ?: groups.first()
                    val validatedActiveGroup =
                        groups.find { it.id == activeGroup.id } ?: groups.first()

                    _state.value = _state.value.copy(
                        userGroups = groups,
                        activeGroup = validatedActiveGroup,
                        currentUserProfile = profile
                    )
                    val initialDate = DateUtils.formatDbDate(DateUtils.getUpcomingFriday())
                    loadDashboardData(initialDate, validatedActiveGroup.id)
                } else {
                    _state.value = _state.value.copy(
                        userGroups = emptyList(),
                        activeGroup = null,
                        currentUserProfile = profile,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "DashboardViewModel: Error loading initial data")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error de connexió. Torna-ho a intentar."
                )
            }
        }
    }

    fun switchGroup(groupId: String, weekendDate: String) {
        val selectedGroup = _state.value.userGroups.find { it.id == groupId }
        _state.value = _state.value.copy(activeGroup = selectedGroup)
        loadDashboardData(weekendDate, groupId)
    }

    fun joinGroup(token: String, userId: String) {
        viewModelScope.launch {
            val success = groupService.joinGroupByToken(token, userId)
            if (success) {
                val groups = groupService.getUserGroups(userId)
                val oldGroups = _state.value.userGroups
                _state.value = _state.value.copy(userGroups = groups)
                val newGroup =
                    groups.find { g -> oldGroups.none { it.id == g.id } } ?: groups.first()
                switchGroup(newGroup.id, DateUtils.formatDbDate(DateUtils.getUpcomingFriday()))
            }
        }
    }

    fun createGroup(name: String, onResult: (Group?) -> Unit) {
        val userId = currentUserId ?: return
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || trimmedName.length > 50) {
            onResult(null); return
        }
        viewModelScope.launch {
            val slug = trimmedName.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
            val group = groupService.createGroup(userId, trimmedName, slug)
            if (group != null) {
                _state.value = _state.value.copy(activeGroup = group.copy(role = "admin"))
                loadInitialData(userId)
                onResult(group.copy(role = "admin"))
            } else {
                onResult(null)
            }
        }
    }

    fun updateStatus(status: String, weekendDate: String, comment: String? = null) {
        val userId = currentUserId ?: return
        val groupId = _state.value.activeGroup?.id ?: return

        viewModelScope.launch {
            val success =
                attendanceService.updateAttendance(userId, groupId, weekendDate, status, comment)
            if (success) {
                loadDashboardData(weekendDate, groupId)
            }
        }
    }

    fun updateComment(comment: String, weekendDate: String) {
        val trimmedComment = comment.trim().take(280)
        val userId = currentUserId ?: return
        val groupId = _state.value.activeGroup?.id ?: return

        viewModelScope.launch {
            val success =
                attendanceService.updateComment(userId, groupId, weekendDate, trimmedComment)
            if (success) {
                loadDashboardData(weekendDate, groupId)
            }
        }
    }

    fun loadDashboardData(weekendDate: String, groupId: String) {
        val userId = currentUserId
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                val profile = if (userId != null) {
                    try {
                        profileService.getProfile(userId)
                    } catch (e: Exception) {
                        null
                    }
                } else null

                var hasErrors = false

                // These three methods throw on failure — wrap them with retry.
                val plans = try {
                    withRetry { attendanceService.getAttendance(weekendDate, groupId) }
                } catch (e: Exception) {
                    Timber.e(e, "Error loading attendance")
                    hasErrors = true
                    emptyList()
                }
                val allMembers = try {
                    withRetry { attendanceService.getGroupMembers(groupId) }
                } catch (e: Exception) {
                    Timber.e(e, "Error loading group members")
                    hasErrors = true
                    emptyList()
                }
                val activities = try {
                    withRetry { activityService.getActivities(weekendDate, groupId) }
                } catch (e: Exception) {
                    Timber.e(e, "Error loading activities")
                    hasErrors = true
                    emptyList()
                }
                // Weather and leaderboard handle their own errors internally.
                val weatherForecast = try {
                    weatherService.getWeekendWeather(weekendDate)
                } catch (e: Exception) {
                    Timber.e(e, "Error loading weather")
                    null
                }
                val leaderboard = try {
                    leaderboardService.getLeaderboard(groupId)
                } catch (e: Exception) {
                    Timber.e(e, "Error loading leaderboard")
                    emptyList()
                }

                val myPlan = if (userId != null) plans.find { it.user_id == userId } else null
                val answeredIds = plans.map { it.user_id }.toSet()

                val going = plans.filter { it.status == "going" }.map { it.profiles to it.comment }
                val notGoing =
                    plans.filter { it.status == "not_going" }.map { it.profiles to it.comment }
                val pending =
                    plans.filter { it.status == "pending" }.map { it.profiles to it.comment }
                val unanswered = allMembers.filter { it.id !in answeredIds }.map { it to null }

                val sortedActivities = activities.sortedWith(compareBy<ActivityWithParticipants> {
                    dayOrder[it.day_of_week.lowercase()] ?: 99
                }.thenBy { it.start_time ?: "" })

                _state.value = _state.value.copy(
                    attendance = AttendanceState(going, notGoing, pending, unanswered),
                    activities = sortedActivities,
                    weather = weatherForecast?.summary,
                    fullForecast = weatherForecast?.details ?: emptyList(),
                    leaderboard = leaderboard,
                    currentUserProfile = profile ?: _state.value.currentUserProfile,
                    currentUserStatus = myPlan?.status,
                    currentUserComment = myPlan?.comment,
                    isLoading = false,
                    error = if (hasErrors) "No s'han pogut carregar totes les dades" else null
                )
            } catch (e: Exception) {
                Timber.e(e, "DashboardViewModel: Unexpected error loading dashboard data")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error inesperat. Torna-ho a intentar."
                )
            }
        }
    }

    suspend fun getGroupMembers(groupId: String): List<MembershipWithProfile> {
        return try {
            groupService.getGroupMembers(groupId)
        } catch (e: Exception) {
            Timber.e(e, "DashboardViewModel: Error fetching group members")
            emptyList()
        }
    }

    fun updateMemberRole(userId: String, role: String, onResult: (Boolean) -> Unit) {
        val groupId = _state.value.activeGroup?.id ?: return
        viewModelScope.launch {
            val success = groupService.updateMemberRole(groupId, userId, role)
            onResult(success)
        }
    }

    fun removeMember(userId: String, onResult: (Boolean) -> Unit) {
        val groupId = _state.value.activeGroup?.id ?: return
        viewModelScope.launch {
            val success = groupService.removeMember(groupId, userId)
            if (success && userId == currentUserId) {
                loadInitialData(userId)
            }
            onResult(success)
        }
    }

    fun deleteGroup(onResult: (Boolean) -> Unit) {
        val groupId = _state.value.activeGroup?.id ?: return
        viewModelScope.launch {
            val success = groupService.deleteGroup(groupId)
            if (success) {
                currentUserId?.let {
                    _state.value = _state.value.copy(activeGroup = null)
                    loadInitialData(it)
                }
            }
            onResult(success)
        }
    }

    fun leaveGroup(onResult: (Boolean) -> Unit) {
        val userId = currentUserId ?: return
        val groupId = _state.value.activeGroup?.id ?: return
        viewModelScope.launch {
            val success = groupService.leaveGroup(groupId, userId)
            if (success) {
                _state.value = _state.value.copy(activeGroup = null)
                loadInitialData(userId)
            }
            onResult(success)
        }
    }

    fun createActivity(activity: Activity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = activityService.createActivity(activity)
            onResult(success)
        }
    }

    fun loadFutureActivities(groupId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isFutureActivitiesLoading = true)
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val fetched = withRetry { activityService.getFutureActivities(today, groupId) }
                _state.value = _state.value.copy(
                    futureActivities = fetched.sortedBy { it.start_date ?: it.weekend_date },
                    isFutureActivitiesLoading = false
                )
            } catch (e: Exception) {
                Timber.e(e, "DashboardViewModel: Error loading future activities")
                _state.value = _state.value.copy(isFutureActivitiesLoading = false)
            }
        }
    }

    fun updateParticipation(
        activityId: String,
        isJoining: Boolean,
        additionalParticipants: Int = 0,
        weekendDate: String
    ) {
        val userId = currentUserId ?: return
        val groupId = _state.value.activeGroup?.id ?: return
        val userProfile = _state.value.currentUserProfile

        fun applyToList(list: List<ActivityWithParticipants>): List<ActivityWithParticipants> =
            list.map { activity ->
                if (activity.id != activityId) return@map activity
                val participants = activity.activity_participants
                val updated = when {
                    isJoining -> {
                        val existing = participants.find { it.user_id == userId }
                        when {
                            existing != null -> participants.map {
                                if (it.user_id == userId) it.copy(additional_participants = additionalParticipants) else it
                            }

                            userProfile != null -> participants + ParticipantWithProfile(
                                userId,
                                additionalParticipants,
                                userProfile
                            )

                            else -> participants
                        }
                    }

                    else -> participants.filter { it.user_id != userId }
                }
                activity.copy(activity_participants = updated)
            }

        // Optimistic update before the DB call
        _state.value = _state.value.copy(
            activities = applyToList(_state.value.activities),
            futureActivities = applyToList(_state.value.futureActivities)
        )

        viewModelScope.launch {
            val success = activityService.updateParticipation(
                activityId,
                userId,
                isJoining,
                additionalParticipants
            )
            if (!success) {
                loadDashboardData(weekendDate, groupId)
                loadFutureActivities(groupId)
            }
        }
    }

    fun updateActivity(activityId: String, update: ActivityUpdate, weekendDate: String) {
        val groupId = _state.value.activeGroup?.id ?: return
        viewModelScope.launch {
            val success = activityService.updateActivity(activityId, update)
            if (success) {
                loadDashboardData(weekendDate, groupId)
            }
        }
    }

    fun deleteActivity(activityId: String, weekendDate: String) {
        val groupId = _state.value.activeGroup?.id ?: return
        viewModelScope.launch {
            val success = activityService.deleteActivity(activityId)
            if (success) {
                loadDashboardData(weekendDate, groupId)
            }
        }
    }

    fun getCurrentUserId(): String? = currentUserId

    suspend fun refreshInviteToken(groupId: String): Group? {
        val result = groupService.refreshInviteToken(groupId)
        if (result != null && _state.value.activeGroup?.id == groupId) {
            _state.value =
                _state.value.copy(activeGroup = result.copy(role = _state.value.activeGroup?.role))
        }
        return result
    }
}
