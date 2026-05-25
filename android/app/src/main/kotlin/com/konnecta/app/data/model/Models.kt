package com.konnecta.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val full_name: String?,
    val avatar_url: String?,
    val email: String,
    val updated_at: String
)

@Serializable
data class Group(
    val id: String,
    val name: String,
    val slug: String,
    val created_at: String,
    val created_by: String,
    val description: String?,
    val role: String? = null // local UI state
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
