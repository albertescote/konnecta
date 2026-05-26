package com.konnecta.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konnecta.app.data.model.*
import com.konnecta.app.data.remote.*
import com.konnecta.app.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val attendanceService = AttendanceService()
    private val activityService = ActivityService()
    private val weatherService = WeatherService()
    private val leaderboardService = LeaderboardService()
    private val groupService = GroupService()

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    private var currentUserId: String? = null
    private val dayOrder = mapOf("divendres" to 1, "dissabte" to 2, "diumenge" to 3)

    fun loadInitialData(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val groups = groupService.getUserGroups(userId)
            if (groups.isNotEmpty()) {
                val firstGroup = groups.first()
                _state.value = _state.value.copy(userGroups = groups, activeGroup = firstGroup)
                val initialDate = DateUtils.formatDbDate(DateUtils.getUpcomingFriday())
                loadDashboardData(initialDate, firstGroup.id)
            } else {
                _state.value = _state.value.copy(userGroups = emptyList(), isLoading = false)
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

    fun updateStatus(status: String, weekendDate: String) {
        val userId = currentUserId ?: return
        val groupId = _state.value.activeGroup?.id ?: return
        
        viewModelScope.launch {
            val success = attendanceService.updateAttendance(userId, groupId, weekendDate, status)
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
                // Fetch in parallel for better performance
                val plans = attendanceService.getAttendance(weekendDate, groupId)
                val allMembers = attendanceService.getGroupMembers(groupId)
                val activities = activityService.getActivities(weekendDate, groupId)
                val weatherForecast = weatherService.getWeekendWeather(weekendDate)
                val leaderboard = leaderboardService.getLeaderboard(groupId)
                
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
                    leaderboard = leaderboard,
                    currentUserStatus = myPlan?.status,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
