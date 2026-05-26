package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.konnecta.app.data.model.Profile
import com.konnecta.app.data.model.UserStats
import com.konnecta.app.data.remote.ProfileService
import com.konnecta.app.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun UserSummaryModal(
    profile: Profile,
    groupId: String,
    onDismiss: () -> Unit
) {
    val profileService = remember { ProfileService() }
    var stats by remember { mutableStateOf<UserStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val nextWeekends = remember { DateUtils.getNextWeekends(5) }

    LaunchedEffect(profile.id, groupId) {
        isLoading = true
        stats = profileService.getUserStats(profile.id, groupId)
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            ) {
                // Close button header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tancar",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image - Removed border/circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.avatar_url != null) {
                            AsyncImage(
                                model = profile.avatar_url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = (profile.full_name ?: profile.email).take(1).uppercase(),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = profile.full_name ?: profile.email.split("@")[0],
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Stats Grid - Forced same height with IntrinsicSize.Max
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "VISITES TOTALS",
                            value = if (isLoading) "..." else (stats?.totalVisits ?: 0).toString(),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        StatCard(
                            title = "PRÒXIMA VISITA",
                            value = if (isLoading) "..." else {
                                val next = stats?.upcomingPlans?.find { it.status == "going" }
                                if (next != null) {
                                    val date = DateUtils.parseDbDate(next.weekend_date)
                                    DateUtils.formatDayAndMonth(date)
                                } else "N/D"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            valueSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Availability Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "DISPONIBILITAT PROPERS FINDES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            nextWeekends.forEach { weekendDateStr ->
                                val plan = stats?.upcomingPlans?.find { it.weekend_date == weekendDateStr }
                                val status = plan?.status ?: "none"
                                val date = DateUtils.parseDbDate(weekendDateStr)
                                val dayMonth = "${DateUtils.getDayOfMonth(date)}/${date.month + 1}"

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(getStatusColor(status)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (status == "going") {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                    Text(
                                        text = dayMonth,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueSize: androidx.compose.ui.unit.TextUnit = 24.sp
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centered content for same height
    ) {
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = valueSize,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "going" -> Color(0xFF22C55E)
        "not_going" -> Color(0xFFEF4444)
        "pending" -> Color(0xFFA1A1AA)
        else -> Color.Gray.copy(alpha = 0.1f)
    }
}
