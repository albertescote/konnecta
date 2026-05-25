package com.konnecta.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konnecta.app.data.remote.ActivityService
import com.konnecta.app.data.remote.ActivityWithParticipants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlansHubViewModel : ViewModel() {
    private val activityService = ActivityService()

    private val _activities = MutableStateFlow<List<ActivityWithParticipants>>(emptyList())
    val activities: StateFlow<List<ActivityWithParticipants>> = _activities

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadFutureActivities(groupId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val fetched = activityService.getFutureActivities(today, groupId)
                _activities.value = fetched.sortedBy { it.start_date ?: it.weekend_date }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
