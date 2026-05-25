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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KonnectaTheme {
                MainContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(viewModel: DashboardViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    var showGroupSelector by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadInitialData("your-user-id")
        
        val intent = (context as? android.app.Activity)?.intent
        val data: android.net.Uri? = intent?.data
        if (data != null && data.scheme == "konnecta" && data.host == "join") {
            val token = data.lastPathSegment
            if (token != null) {
                viewModel.joinGroup(token, "your-user-id")
            }
        }
    }

    Scaffold(
        topBar = {
            if (selectedTab == 0 && state.activeGroup != null) {
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
                                text = state.activeGroup?.name ?: "",
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
            if (state.isLoading && state.activeGroup == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        groupId = state.activeGroup?.id ?: "",
                        viewModel = viewModel
                    )
                    1 -> PlansHubScreen(groupId = state.activeGroup?.id ?: "")
                }
            }
        }

        if (showGroupSelector) {
            GroupSelectorBottomSheet(
                groups = state.userGroups,
                activeGroupId = state.activeGroup?.id ?: "",
                onGroupSelected = { viewModel.switchGroup(it, "2024-05-24") },
                onDismiss = { showGroupSelector = false }
            )
        }

        if (showProfile) {
            ProfileBottomSheet(
                profile = null,
                onSignOut = { showProfile = false },
                onDeleteAccount = { showProfile = false },
                onDismiss = { showProfile = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(groupId: String, viewModel: DashboardViewModel = viewModel()) {
    var selectedDate by remember { mutableStateOf("2024-05-24") }
    var currentStatus by remember { mutableStateOf<String?>(null) }

    val state by viewModel.state.collectAsState()
    val dates = listOf("2024-05-24", "2024-05-31", "2024-06-07", "2024-06-14")

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
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            WeekendSelector(
                dates = dates,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                WeatherCard(weather = state.weather)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            VotingSection(
                currentStatus = currentStatus,
                onStatusChange = { currentStatus = it }
            )

            // Attendance Section
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
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

            Divider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp), color = Color.Gray.copy(alpha = 0.1f))

            // Activity Board Section
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
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
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    state.activities.forEach { activity ->
                        ActivityCard(activity = activity)
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

            Divider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp), color = Color.Gray.copy(alpha = 0.1f))

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
