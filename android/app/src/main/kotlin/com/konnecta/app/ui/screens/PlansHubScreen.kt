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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konnecta.app.data.model.*
import com.konnecta.app.ui.components.ActivityCard
import com.konnecta.app.ui.components.AppFooter
import com.konnecta.app.ui.components.NewActivityBottomSheet
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import com.konnecta.app.ui.viewmodel.PlansHubViewModel
import com.konnecta.app.utils.DateUtils
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansHubScreen(
    groupId: String,
    viewModel: PlansHubViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel
) {
    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.loadFutureActivities(groupId)
            pullToRefreshState.endRefresh()
        }
    }

    LaunchedEffect(groupId) {
        viewModel.loadFutureActivities(groupId)
    }

    Box(modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "PROPERS ESDEVENIMENTS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    if (activities.isEmpty() && !isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .height(120.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hi ha cap pla futur encara...",
                                fontSize = 14.sp,
                                color = Color.Gray.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            activities.forEach { activity ->
                                ActivityCard(activity = activity, viewModel = dashboardViewModel)
                            }
                        }
                    }
                    
                    var showNewActivitySheet by remember { mutableStateOf(false) }
                    val todayStr = remember { DateUtils.formatDbDate(Date()) }

                    OutlinedButton(
                        onClick = { showNewActivitySheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline))
                    ) {
                        Text(
                            text = "+ PROPOSA UN PLA",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    if (showNewActivitySheet) {
                        NewActivityBottomSheet(
                            groupId = groupId,
                            weekendDate = todayStr,
                            freeDate = true,
                            onSuccess = {
                                showNewActivitySheet = false
                                viewModel.loadFutureActivities(groupId)
                            },
                            onDismiss = { showNewActivitySheet = false },
                            viewModel = dashboardViewModel
                        )
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AppFooter()
                }
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
