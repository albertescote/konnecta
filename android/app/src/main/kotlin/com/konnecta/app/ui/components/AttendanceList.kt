package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konnecta.app.data.model.Profile

@Composable
fun UserAttendanceCard(
    profile: Profile,
    comment: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.full_name?.take(1) ?: "?",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = profile.full_name ?: "Usuari",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            if (!comment.isNullOrBlank()) {
                Text(
                    text = comment,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AttendanceSection(
    title: String,
    users: List<Pair<Profile, String?>>,
    titleColor: Color
) {
    if (users.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$title (${users.size})",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = titleColor,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        users.forEach { (profile, comment) ->
            UserAttendanceCard(profile = profile, comment = comment)
        }
    }
}
