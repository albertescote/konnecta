package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.konnecta.app.data.model.DashboardState
import com.konnecta.app.data.model.Profile

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
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabItem(
            label = "CAP DE SETMANA",
            isActive = activePage == 0,
            onClick = { onPageSelected(0) },
            modifier = Modifier.weight(1f)
        )

        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.LightGray.copy(alpha = 0.3f)))

        TabItem(
            label = "TOTS ELS PLANS",
            isActive = activePage == 1,
            onClick = { onPageSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = if (isActive) MaterialTheme.colorScheme.onBackground else Color.Gray
        )
        if (isActive) {
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
