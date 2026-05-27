package com.konnecta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.konnecta.app.data.model.DashboardState
import com.konnecta.app.data.model.Group
import com.konnecta.app.data.model.Profile
import com.konnecta.app.data.remote.SupabaseClient
import com.konnecta.app.ui.components.CreateGroupBottomSheet
import com.konnecta.app.ui.components.DashboardHeader
import com.konnecta.app.ui.components.GroupSelectorBottomSheet
import com.konnecta.app.ui.components.InviteFriendsBottomSheet
import com.konnecta.app.ui.components.ProfileBottomSheet
import com.konnecta.app.ui.components.ViewToggle
import com.konnecta.app.ui.screens.DashboardScreen
import com.konnecta.app.ui.screens.LoginScreen
import com.konnecta.app.ui.screens.NoGroupScreen
import com.konnecta.app.ui.screens.PlansHubScreen
import com.konnecta.app.ui.theme.KonnectaTheme
import com.konnecta.app.ui.viewmodel.AuthViewModel
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import com.konnecta.app.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseClient.client.handleDeeplinks(intent)
        setContent {
            KonnectaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContainer()
                }
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun MainContainer(
    viewModel: DashboardViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()
    val dashboardState by viewModel.state.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    val userProfile = dashboardState.currentUserProfile ?: authState.user?.let { user ->
        val metadata = user.userMetadata
        Profile(
            id = user.id,
            full_name = metadata?.get("full_name")?.toString() ?: metadata?.get("name")?.toString(),
            avatar_url = metadata?.get("avatar_url")?.toString() ?: metadata?.get("picture")
                ?.toString(),
            email = user.email ?: "",
            updated_at = null
        )
    }

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

    when (authState.sessionStatus) {
        is SessionStatus.Authenticated -> {
            when {
                dashboardState.isLoading && dashboardState.activeGroup == null && dashboardState.userGroups.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                dashboardState.userGroups.isEmpty() && !dashboardState.isLoading -> {
                    NoGroupScaffold(
                        userProfile = userProfile,
                        dashboardState = dashboardState,
                        authViewModel = authViewModel,
                        viewModel = viewModel
                    )
                }

                else -> {
                    MainScaffold(
                        userProfile = userProfile,
                        dashboardState = dashboardState,
                        authViewModel = authViewModel,
                        viewModel = viewModel
                    )
                }
            }
        }

        is SessionStatus.NotAuthenticated -> LoginScreen(onLoginSuccess = {})
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun NoGroupScaffold(
    userProfile: Profile?,
    dashboardState: DashboardState,
    authViewModel: AuthViewModel,
    viewModel: DashboardViewModel
) {
    var showCreateGroup by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showInviteFriends by remember { mutableStateOf<Group?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                userProfile = userProfile,
                dashboardState = dashboardState,
                onProfileClick = { showProfile = true },
                onGroupClick = {}
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NoGroupScreen(onNavigateToCreate = { showCreateGroup = true })
        }
    }

    if (showCreateGroup) {
        CreateGroupBottomSheet(
            onDismiss = { showCreateGroup = false },
            onGroupCreated = { newGroup ->
                showCreateGroup = false
                showInviteFriends = newGroup
            },
            viewModel = viewModel
        )
    }
    if (showProfile) {
        ProfileBottomSheet(
            profile = userProfile,
            groups = dashboardState.userGroups,
            activeGroupId = "",
            onSignOut = { authViewModel.signOut(); showProfile = false },
            onDismiss = { showProfile = false }
        )
    }
    showInviteFriends?.let { group ->
        InviteFriendsBottomSheet(
            group = group,
            onDismiss = { showInviteFriends = null },
            viewModel = viewModel
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MainScaffold(
    userProfile: Profile?,
    dashboardState: DashboardState,
    authViewModel: AuthViewModel,
    viewModel: DashboardViewModel
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var showGroupSelector by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showInviteFriends by remember { mutableStateOf<Group?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                userProfile = userProfile,
                dashboardState = dashboardState,
                onProfileClick = { showProfile = true },
                onGroupClick = { showGroupSelector = true },
                bottomSlot = {
                    ViewToggle(
                        activePage = pagerState.currentPage,
                        onPageSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> DashboardScreen(
                        groupId = dashboardState.activeGroup?.id ?: "",
                        viewModel = viewModel
                    )

                    1 -> PlansHubScreen(
                        groupId = dashboardState.activeGroup?.id ?: "",
                        dashboardViewModel = viewModel
                    )
                }
            }
        }
    }

    if (showGroupSelector) {
        GroupSelectorBottomSheet(
            groups = dashboardState.userGroups,
            activeGroupId = dashboardState.activeGroup?.id ?: "",
            currentUserId = userProfile?.id ?: "",
            onGroupSelected = {
                viewModel.switchGroup(
                    it,
                    DateUtils.formatDbDate(DateUtils.getUpcomingFriday())
                )
            },
            onGroupCreated = { newGroup ->
                showGroupSelector = false
                showInviteFriends = newGroup
            },
            onDismiss = { showGroupSelector = false }
        )
    }
    if (showProfile) {
        ProfileBottomSheet(
            profile = userProfile,
            groups = dashboardState.userGroups,
            activeGroupId = dashboardState.activeGroup?.id ?: "",
            onSignOut = { authViewModel.signOut(); showProfile = false },
            onDismiss = { showProfile = false }
        )
    }
    showInviteFriends?.let { group ->
        InviteFriendsBottomSheet(
            group = group,
            onDismiss = { showInviteFriends = null },
            viewModel = viewModel
        )
    }
}

@Composable
private fun AppTopBar(
    userProfile: Profile?,
    dashboardState: DashboardState,
    onProfileClick: () -> Unit,
    onGroupClick: () -> Unit,
    bottomSlot: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 32.dp)
    ) {
        DashboardHeader(
            profile = userProfile,
            dashboardState = dashboardState,
            onProfileClick = onProfileClick,
            onGroupClick = onGroupClick,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        bottomSlot()
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    }
}
