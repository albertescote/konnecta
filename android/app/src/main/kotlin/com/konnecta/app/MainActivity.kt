package com.konnecta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konnecta.app.ui.components.*
import com.konnecta.app.ui.screens.PlansHubScreen
import com.konnecta.app.ui.theme.KonnectaTheme
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.konnecta.app.ui.viewmodel.AuthViewModel
import com.konnecta.app.ui.screens.LoginScreen
import com.konnecta.app.ui.screens.NoGroupScreen
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.handleDeeplinks
import com.konnecta.app.data.remote.SupabaseClient
import com.konnecta.app.data.model.*
import com.konnecta.app.utils.DateUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle OAuth and Magic Link redirects
        SupabaseClient.client.handleDeeplinks(intent)

        setContent {
            KonnectaTheme {
                MainContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    viewModel: DashboardViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.state.collectAsState()
    val dashboardState by viewModel.state.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showGroupSelector by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(authState.sessionStatus) {
        val status = authState.sessionStatus
        if (status is SessionStatus.Authenticated) {
            viewModel.loadInitialData(status.session.user?.id ?: "")
        }
    }

    LaunchedEffect(Unit) {
        val intent = (context as? android.app.Activity)?.intent
        val data: android.net.Uri? = intent?.data
        if (data != null && data.scheme == "konnecta" && data.host == "join") {
            val token = data.lastPathSegment
            val user = (authState.sessionStatus as? SessionStatus.Authenticated)?.session?.user
            if (token != null && user != null) {
                viewModel.joinGroup(token, user.id)
            }
        }
    }

    when (val status = authState.sessionStatus) {
        is SessionStatus.NotAuthenticated -> {
            LoginScreen(
                onLoginSuccess = { /* Handle if needed, though LaunchedEffect handles it */ }
            )
        }
        is SessionStatus.Authenticated -> {
            if (dashboardState.isLoading && dashboardState.activeGroup == null && dashboardState.userGroups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (dashboardState.userGroups.isEmpty() && !dashboardState.isLoading) {
                NoGroupScreen(
                    userId = status.session.user?.id ?: "",
                    onGroupCreated = { viewModel.loadInitialData(status.session.user?.id ?: "") }
                )
            } else {
                Scaffold(
                    topBar = {
                        if (selectedTab == 0 && dashboardState.activeGroup != null) {
                            CenterAlignedTopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { showProfile = true }) {
                                        Text("👤", fontSize = 20.sp)
                                    }
                                },
                                title = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable { showGroupSelector = true }
                                    ) {
                                        Text(
                                            text = dashboardState.activeGroup?.name ?: "",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "CANVIA DE GRUP ▾",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.background,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Text("🏠") },
                                label = { Text("Inici") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Text("📅") },
                                label = { Text("Plans") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                groupId = dashboardState.activeGroup?.id ?: "",
                                viewModel = viewModel
                            )
                            1 -> PlansHubScreen(groupId = dashboardState.activeGroup?.id ?: "")
                        }
                    }

                    if (showGroupSelector) {
                        GroupSelectorBottomSheet(
                            groups = dashboardState.userGroups,
                            activeGroupId = dashboardState.activeGroup?.id ?: "",
                            onGroupSelected = { viewModel.switchGroup(it, "2024-05-24") },
                            onDismiss = { showGroupSelector = false }
                        )
                    }

                    if (showProfile) {
                        ProfileBottomSheet(
                            profile = null,
                            onSignOut = { 
                                authViewModel.signOut()
                                showProfile = false 
                            },
                            onDeleteAccount = { showProfile = false },
                            onDismiss = { showProfile = false }
                        )
                    }
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

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
        }
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) pullToRefreshState.endRefresh()
    }

    LaunchedEffect(selectedDate, groupId) {
        viewModel.loadDashboardData(selectedDate, groupId)
    }

    Box(modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "KONNECTA",
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                letterSpacing = (-1.5).sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            WeekendSelector(
                dates = dates,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                WeatherCard(weather = state.weather)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            VotingSection(
                currentStatus = state.currentUserStatus,
                onStatusChange = { viewModel.updateStatus(it, selectedDate) }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Attendance Section
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "QUI VE?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.Gray.copy(alpha = 0.6f)
                )

                AttendanceSection(title = "SÍ", users = state.attendance.going, titleColor = Color(0xFF4ADE80))
                AttendanceSection(title = "NO", users = state.attendance.notGoing, titleColor = Color(0xFFF87171))
                AttendanceSection(title = "POTSER", users = state.attendance.pending, titleColor = Color.Gray)
                AttendanceSection(title = "PENDENT", users = state.attendance.unanswered, titleColor = Color.Gray.copy(alpha = 0.5f))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp), color = Color.Gray.copy(alpha = 0.1f))

            // Activity Board Section
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PLANS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.Gray.copy(alpha = 0.6f)
                )

                if (state.activities.isEmpty()) {
                    Text(
                        text = "No hi ha cap pla encara...",
                        fontSize = 14.sp,
                        color = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.activities.forEach { activity ->
                            ActivityCard(activity = activity)
                        }
                    }
                }

                var showNewActivitySheet by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { showNewActivitySheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.LightGray.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "+ PROPOSA UN PLA",
                        fontWeight = FontWeight.Black,
                        color = Color.Gray
                    )
                }

                if (showNewActivitySheet) {
                    NewActivityBottomSheet(
                        weekendDate = selectedDate,
                        groupId = groupId,
                        onDismiss = { showNewActivitySheet = false },
                        onSuccess = {
                            showNewActivitySheet = false
                            viewModel.loadDashboardData(selectedDate, groupId)
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp), color = Color.Gray.copy(alpha = 0.1f))

            // Hall of Fame Section
            HallOfFame(winners = state.leaderboard)

            Spacer(modifier = Modifier.height(32.dp))
        }
        
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
        )
    }
}
