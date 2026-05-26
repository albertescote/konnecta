package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.konnecta.app.data.model.Profile

@Composable
fun UserAttendanceCard(
    profile: Profile,
    comment: String? = null,
    opacity: Float = 1.0f
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable { /* Show User Summary */ }
            .alpha(opacity)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (profile.avatar_url != null) {
                AsyncImage(
                    model = profile.avatar_url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    text = (profile.full_name ?: profile.email).take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.full_name ?: profile.email.split("@")[0],
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!comment.isNullOrBlank()) {
                Text(
                    text = "\"$comment\"",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AttendanceSection(
    title: String,
    users: List<Pair<Profile, String?>>,
    titleColor: Color,
    isUnanswered: Boolean = false
) {
    if (users.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$title (${users.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isUnanswered) titleColor.copy(alpha = 0.6f) else titleColor,
            modifier = Modifier.padding(horizontal = 4.dp).alpha(if (isUnanswered) 0.6f else 1.0f),
            letterSpacing = 0.5.sp
        )
        users.forEach { (profile, comment) ->
            UserAttendanceCard(
                profile = profile, 
                comment = comment, 
                opacity = if (isUnanswered) 0.5f else 1.0f
            )
        }
    }
}
