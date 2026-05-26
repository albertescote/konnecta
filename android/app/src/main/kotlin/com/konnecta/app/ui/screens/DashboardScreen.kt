package com.konnecta.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konnecta.app.data.model.*
import com.konnecta.app.ui.components.*
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import com.konnecta.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(groupId: String, viewModel: DashboardViewModel = viewModel()) {
    val initialDate = remember { DateUtils.formatDbDate(DateUtils.getUpcomingFriday()) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    val state by viewModel.state.collectAsState()
    val dates = remember { DateUtils.getNextWeekends(10) }

    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.loadDashboardData(selectedDate, groupId)
            pullToRefreshState.endRefresh()
        }
    }

    LaunchedEffect(selectedDate, groupId) {
        viewModel.loadDashboardData(selectedDate, groupId)
    }

    Box(modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
        ) {
            item {
                WeekendSelector(
                    dates = dates,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    WeatherCard(weather = state.weather)
                    
                    VotingSection(
                        currentStatus = state.currentUserStatus,
                        weekendDate = selectedDate,
                        onStatusChange = { viewModel.updateStatus(it, selectedDate) }
                    )
                }
            }

            // Attendance Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "QUI VE?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    AttendanceSection(title = "SÍ", users = state.attendance.going, titleColor = Color(0xFF4ADE80))
                    AttendanceSection(title = "NO", users = state.attendance.notGoing, titleColor = Color(0xFFF87171))
                    AttendanceSection(title = "POTSER", users = state.attendance.pending, titleColor = Color(0xFFFBBF24))
                    AttendanceSection(title = "PENDENT", users = state.attendance.unanswered, titleColor = Color.Gray, isUnanswered = true)
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 8.dp))
            }

            // Activity Board Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "PLANS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    if (state.activities.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            state.activities.forEach { activity ->
                                ActivityCard(activity = activity)
                            }
                        }
                    }

                    var showNewActivitySheet by remember { mutableStateOf(false) }
                    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    val dashColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .drawWithContent {
                                if (state.activities.isEmpty()) {
                                    drawRoundRect(
                                        color = dashColor,
                                        style = stroke,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                                    )
                                }
                                drawContent()
                            }
                    ) {
                        OutlinedButton(
                            onClick = { showNewActivitySheet = true },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = if (state.activities.isEmpty()) null else ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline))
                        ) {
                            Text(
                                text = "+ PROPOSA UN PLA",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    if (showNewActivitySheet) {
                        NewActivityBottomSheet(
                            groupId = groupId,
                            weekendDate = selectedDate,
                            onSuccess = {
                                showNewActivitySheet = false
                                viewModel.loadDashboardData(selectedDate, groupId)
                            },
                            onDismiss = { showNewActivitySheet = false }
                        )
                    }
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 8.dp))
            }

            // Hall of Fame Section
            item {
                HallOfFame(winners = state.leaderboard)
            }
        }
        
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}
