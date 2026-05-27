package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.konnecta.app.data.model.ActivityWithParticipants
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import com.konnecta.app.utils.CalendarUtils
import com.konnecta.app.utils.DateUtils
import com.konnecta.app.utils.ShareUtils

@Composable
fun ActivityCard(
    activity: ActivityWithParticipants,
    viewModel: DashboardViewModel
) {
    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }
    val currentUserId = viewModel.getCurrentUserId() ?: ""

    val isJoined = activity.activity_participants.any { it.user_id == currentUserId }
    val hasPlusOne =
        (activity.activity_participants.find { it.user_id == currentUserId }?.additional_participants
            ?: 0) > 0
    val totalAttendance = activity.activity_participants.sumOf { 1 + it.additional_participants }

    val eventDate = DateUtils.parseDbDate(activity.start_date ?: activity.weekend_date)
    val endDate = if (activity.end_date != null) DateUtils.parseDbDate(activity.end_date) else null
    val isMultiDay = endDate != null && activity.start_date != activity.end_date

    val dateDisplay = if (isMultiDay) {
        val startDay = DateUtils.getDayOfMonth(eventDate)
        val startMonth = DateUtils.getMonthOfMonth(eventDate)
        val endDay = DateUtils.getDayOfMonth(endDate!!)
        val endMonth = DateUtils.getMonthOfMonth(endDate)
        "$startDay/$startMonth AL $endDay/$endMonth"
    } else {
        val dayNameShort = DateUtils.formatDayOfWeek(eventDate).take(2).uppercase()
        val dayOfMonth = DateUtils.getDayOfMonth(eventDate)
        val monthNameShort =
            DateUtils.formatDayAndMonth(eventDate).split(" ").lastOrNull()?.uppercase() ?: ""
        "$dayNameShort. $dayOfMonth $monthNameShort"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(28.dp)
            )
            .clickable { showDetails = true }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Date Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dateDisplay,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3B82F6),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = activity.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    lineHeight = 22.sp
                )

                // Utility Buttons (Top Right)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape)
                            .clickable { CalendarUtils.addToCalendar(context, activity) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF3B82F6)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.1f), CircleShape)
                            .clickable { ShareUtils.shareActivity(context, activity) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF22C55E)
                        )
                    }
                }
            }

            if (!activity.description.isNullOrBlank()) {
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Main Actions (Join/Leave, Plus One)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isJoined) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface)
                    .clickable {
                        viewModel.updateParticipation(
                            activity.id,
                            !isJoined,
                            0,
                            activity.weekend_date
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isJoined) "SURT" else "APUNTA'T",
                    color = if (isJoined) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.surface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            if (isJoined) {
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (hasPlusOne) Color(0xFF3B82F6).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.8f
                            )
                        )
                        .border(
                            1.dp,
                            if (hasPlusOne) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            viewModel.updateParticipation(
                                activity.id,
                                true,
                                if (hasPlusOne) 0 else 1,
                                activity.weekend_date
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasPlusOne) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                        contentDescription = "Plus One",
                        tint = if (hasPlusOne) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Horizontal Separator - Better Contrast
        androidx.compose.material3.HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Bottom Footer (Participants & Time)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatars
                Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                    activity.activity_participants.take(4).forEach { participant ->
                        Box(
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                            ) {
                                if (participant.profiles.avatar_url != null) {
                                    AsyncImage(
                                        model = participant.profiles.avatar_url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (participant.profiles.full_name
                                                ?: participant.profiles.email).take(1).uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // +1 Badge on avatar - Moved outside the clipped box
                            if (participant.additional_participants > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 1.dp, y = (-1).dp)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B82F6))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.surface,
                                            CircleShape
                                        )
                                ) {
                                    Text(
                                        text = "+${participant.additional_participants}",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.Center),
                                        lineHeight = 7.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                if (totalAttendance > 0) {
                    Text(
                        text = "$totalAttendance ${if (totalAttendance == 1) "PERSONA" else "PERSONES"}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Group,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "NINGÚ ENCARA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            if (activity.start_time != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = activity.start_time,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (showDetails) {
        ActivityDetailsBottomSheet(
            activity = activity,
            onDismiss = { showDetails = false },
            viewModel = viewModel
        )
    }
}

