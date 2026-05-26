package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konnecta.app.utils.DateUtils

@Composable
fun VotingSection(
    currentStatus: String?,
    weekendDate: String,
    onStatusChange: (String) -> Unit
) {
    val displayDate = DateUtils.formatDisplayDate(weekendDate)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Hi seràs?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "CAP DE SETMANA DEL $displayDate".uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color(0xFF3B82F6),
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            VoteButton(
                text = "Sí",
                icon = Icons.Default.Check,
                isSelected = currentStatus == "going",
                activeColor = Color(0xFF22C55E), // Green-500
                onClick = { onStatusChange("going") }
            )
            VoteButton(
                text = "No",
                icon = Icons.Default.Close,
                isSelected = currentStatus == "not_going",
                activeColor = Color(0xFFEF4444), // Red-500
                onClick = { onStatusChange("not_going") }
            )
            VoteButton(
                text = "Potser",
                icon = Icons.Default.Remove,
                isSelected = currentStatus == "pending",
                activeColor = Color(0xFFA1A1AA), // Zinc-400
                onClick = { onStatusChange("pending") }
            )
        }
    }
}

@Composable
fun VoteButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
    }
}
