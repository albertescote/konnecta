package com.konnecta.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konnecta.app.data.model.*
import com.konnecta.app.data.remote.*
import com.konnecta.app.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel : ViewModel() {
    private val attendanceService = AttendanceService()
    private val activityService = ActivityService()
    private val weatherService = WeatherService()
    private val leaderboardService = LeaderboardService()
    private val groupService = GroupService()
    private val profileService = ProfileService()

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    private var currentUserId: String? = null
    private val dayOrder = mapOf("divendres" to 1, "dissabte" to 2, "diumenge" to 3)

    fun loadInitialData(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Load user profile from DB to ensure it's up to date
                val profile = profileService.getProfile(userId)
                
                val groups = groupService.getUserGroups(userId)
                if (groups.isNotEmpty()) {
                    val activeGroup = _state.value.activeGroup ?: groups.first()
                    // Ensure the active group is in the current list
                    val validatedActiveGroup = groups.find { it.id == activeGroup.id } ?: groups.first()
                    
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
                println("DashboardViewModel: Error loading initial data: ${e.message}")
                _state.value = _state.value.copy(isLoading = false)
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
                // Refresh groups
                val groups = groupService.getUserGroups(userId)
                val oldGroups = _state.value.userGroups
                _state.value = _state.value.copy(userGroups = groups)
                // Switch to the newly joined group
                val newGroup = groups.find { g -> oldGroups.none { it.id == g.id } } ?: groups.first()
                switchGroup(newGroup.id, DateUtils.formatDbDate(DateUtils.getUpcomingFriday()))
            }
        }
    }

    fun createGroup(name: String, onResult: (Group?) -> Unit) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            val slug = name.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
            val group = groupService.createGroup(userId, name, slug)
            if (group != null) {
                // Refresh and set the new group as active before calling initial data
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
            val success = attendanceService.updateAttendance(userId, groupId, weekendDate, status, comment)
            if (success) {
                loadDashboardData(weekendDate, groupId)
            }
        }
    }

    fun updateComment(comment: String, weekendDate: String) {
        val userId = currentUserId ?: return
        val groupId = _state.value.activeGroup?.id ?: return
        
        viewModelScope.launch {
            val success = attendanceService.updateComment(userId, groupId, weekendDate, comment)
            if (success) {
                loadDashboardData(weekendDate, groupId)
            }
        }
    }

    fun loadDashboardData(weekendDate: String, groupId: String) {
        val userId = currentUserId
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Refresh profile too just in case
                val profile = if (userId != null) {
                    try { profileService.getProfile(userId) } catch (e: Exception) { null }
                } else null
                
                // Fetch each piece of data with its own try-catch to prevent one failure from breaking everything
                val plans = try { attendanceService.getAttendance(weekendDate, groupId) } catch (e: Exception) { 
                    println("Error loading plans: ${e.message}")
                    emptyList() 
                }
                val allMembers = try { attendanceService.getGroupMembers(groupId) } catch (e: Exception) { 
                    println("Error loading members: ${e.message}")
                    emptyList() 
                }
                val activities = try { activityService.getActivities(weekendDate, groupId) } catch (e: Exception) { 
                    println("Error loading activities: ${e.message}")
                    emptyList() 
                }
                val weatherForecast = try { weatherService.getWeekendWeather(weekendDate) } catch (e: Exception) { 
                    println("Error loading weather: ${e.message}")
                    null 
                }
                val leaderboard = try { leaderboardService.getLeaderboard(groupId) } catch (e: Exception) { 
                    println("Error loading leaderboard: ${e.message}")
                    emptyList() 
                }
                
                val myPlan = if (userId != null) plans.find { it.user_id == userId } else null
                val answeredIds = plans.map { it.user_id }.toSet()
                
                val going = plans.filter { it.status == "going" }.map { it.profiles to it.comment }
                val notGoing = plans.filter { it.status == "not_going" }.map { it.profiles to it.comment }
                val pending = plans.filter { it.status == "pending" }.map { it.profiles to it.comment }
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
                    isLoading = false
                )
            } catch (e: Exception) {
                println("DashboardViewModel: Global error loading dashboard data: ${e.message}")
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    // Group Management Methods
    suspend fun getGroupMembers(groupId: String): List<MembershipWithProfile> {
        return try {
            groupService.getGroupMembers(groupId)
        } catch (e: Exception) {
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
            if (success) {
                // If it was the current user, we should probably reload everything
                if (userId == currentUserId) {
                    loadInitialData(userId)
                }
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
                val fetched = activityService.getFutureActivities(today, groupId)
                _state.value = _state.value.copy(
                    futureActivities = fetched.sortedBy { it.start_date ?: it.weekend_date },
                    isFutureActivitiesLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isFutureActivitiesLoading = false)
            }
        }
    }

    fun updateParticipation(activityId: String, isJoining: Boolean, additionalParticipants: Int = 0, weekendDate: String) {
        val userId = currentUserId ?: return
        val groupId = _state.value.activeGroup?.id ?: return
        val userProfile = _state.value.currentUserProfile

        fun applyToList(list: List<ActivityWithParticipants>): List<ActivityWithParticipants> = list.map { activity ->
            if (activity.id != activityId) return@map activity
            val participants = activity.activity_participants
            val updated = when {
                isJoining -> {
                    val existing = participants.find { it.user_id == userId }
                    when {
                        existing != null -> participants.map {
                            if (it.user_id == userId) it.copy(additional_participants = additionalParticipants) else it
                        }
                        userProfile != null -> participants + ParticipantWithProfile(userId, additionalParticipants, userProfile)
                        else -> participants
                    }
                }
                else -> participants.filter { it.user_id != userId }
            }
            activity.copy(activity_participants = updated)
        }

        // Synchronous optimistic update on the main thread before any DB call
        _state.value = _state.value.copy(
            activities = applyToList(_state.value.activities),
            futureActivities = applyToList(_state.value.futureActivities)
        )

        viewModelScope.launch {
            val success = activityService.updateParticipation(activityId, userId, isJoining, additionalParticipants)
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
            _state.value = _state.value.copy(activeGroup = result.copy(role = _state.value.activeGroup?.role))
        }
        return result
    }
}
