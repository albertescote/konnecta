package com.konnecta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.konnecta.app.data.model.*
import com.konnecta.app.data.remote.SupabaseClient
import com.konnecta.app.ui.components.*
import com.konnecta.app.ui.screens.DashboardScreen
import com.konnecta.app.ui.screens.LoginScreen
import com.konnecta.app.ui.screens.NoGroupScreen
import com.konnecta.app.ui.screens.PlansHubScreen
import com.konnecta.app.ui.theme.KonnectaTheme
import com.konnecta.app.ui.viewmodel.AuthViewModel
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import com.konnecta.app.utils.DateUtils
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle OAuth and Magic Link redirects
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

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainContainer(
    viewModel: DashboardViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.state.collectAsState()
    val dashboardState by viewModel.state.collectAsState()
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    
    var showGroupSelector by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }
    var showInviteFriends by remember { mutableStateOf<Group?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    // Use the profile from dashboardState if available, fallback to auth metadata
    val userProfile = dashboardState.currentUserProfile ?: authState.user?.let { user ->
        val metadata = user.userMetadata
        Profile(
            id = user.id,
            full_name = metadata?.get("full_name")?.toString() ?: metadata?.get("name")?.toString(),
            avatar_url = metadata?.get("avatar_url")?.toString() ?: metadata?.get("picture")?.toString(),
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

    when (val status = authState.sessionStatus) {
        is SessionStatus.Authenticated -> {
            if (dashboardState.isLoading && dashboardState.activeGroup == null && dashboardState.userGroups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (dashboardState.userGroups.isEmpty() && !dashboardState.isLoading) {
                Scaffold(
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = 32.dp)
                        ) {
                            DashboardHeader(
                                profile = userProfile,
                                dashboardState = dashboardState,
                                onProfileClick = { showProfile = true },
                                onGroupClick = { }, // No group to click
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NoGroupScreen(
                            onNavigateToCreate = { showCreateGroup = true }
                        )
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
                        onSignOut = { 
                            authViewModel.signOut()
                            showProfile = false 
                        },
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
            } else {
                Scaffold(
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = 32.dp)
                        ) {
                            DashboardHeader(
                                profile = userProfile,
                                dashboardState = dashboardState,
                                onProfileClick = { showProfile = true },
                                onGroupClick = { showGroupSelector = true },
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ViewToggle(
                                activePage = pagerState.currentPage,
                                onPageSelected = { targetPage ->
                                    scope.launch { pagerState.animateScrollToPage(targetPage) }
                                },
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        }
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

                    if (showGroupSelector) {
                        GroupSelectorBottomSheet(
                            groups = dashboardState.userGroups,
                            activeGroupId = dashboardState.activeGroup?.id ?: "",
                            currentUserId = userProfile?.id ?: "",
                            onGroupSelected = { viewModel.switchGroup(it, DateUtils.formatDbDate(DateUtils.getUpcomingFriday())) },
                            onGroupCreated = { newGroup ->
                                showGroupSelector = false
                                showInviteFriends = newGroup
                            },
                            onInviteClick = { group ->
                                showGroupSelector = false
                                showInviteFriends = group
                            },
                            onDismiss = { showGroupSelector = false }
                        )
                    }

                    if (showProfile) {
                        ProfileBottomSheet(
                            profile = userProfile,
                            groups = dashboardState.userGroups,
                            activeGroupId = dashboardState.activeGroup?.id ?: "",
                            onSignOut = { 
                                authViewModel.signOut()
                                showProfile = false 
                            },
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
            }
        }
        is SessionStatus.NotAuthenticated -> {
            LoginScreen(
                onLoginSuccess = { /* Handle if needed */ }
            )
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun DashboardHeader(
    profile: Profile?,
    dashboardState: DashboardState,
    onProfileClick: () -> Unit,
    onGroupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "KONNECTA",
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                letterSpacing = (-2).sp,
                lineHeight = 28.sp
            )
            
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onGroupClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dashboardState.activeGroup?.name?.uppercase() ?: "BENVINGUT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (dashboardState.userGroups.size > 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            ThemeToggle()
            Spacer(modifier = Modifier.width(8.dp))
            // Profile Button with actual avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                if (profile?.avatar_url != null && profile.avatar_url.isNotBlank()) {
                    AsyncImage(
                        model = profile.avatar_url,
                        contentDescription = "El meu perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = profile?.full_name?.take(1) ?: profile?.email?.take(1) ?: "👤", 
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ViewToggle(
    activePage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onPageSelected(0) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CAP DE SETMANA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = if (activePage == 0) MaterialTheme.colorScheme.onBackground else Color.Gray
            )
            if (activePage == 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(40.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
            }
        }
        
        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.LightGray.copy(alpha = 0.3f)))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onPageSelected(1) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TOTS ELS PLANS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = if (activePage == 1) MaterialTheme.colorScheme.onBackground else Color.Gray
            )
            if (activePage == 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(40.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
            }
        }
    }
}
