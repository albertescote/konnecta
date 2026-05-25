package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun VotingSection(
    currentStatus: String?,
    onStatusChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "VINS AQUEST CAP DE SETMANA?",
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VoteButton(
                text = "SÍ",
                isSelected = currentStatus == "going",
                activeColor = Color(0xFF4ADE80), // Green-400
                modifier = Modifier.weight(1f),
                onClick = { onStatusChange("going") }
            )
            VoteButton(
                text = "NO",
                isSelected = currentStatus == "not_going",
                activeColor = Color(0xFFF87171), // Red-400
                modifier = Modifier.weight(1f),
                onClick = { onStatusChange("not_going") }
            )
            VoteButton(
                text = "POTSER",
                isSelected = currentStatus == "pending",
                activeColor = Color(0xFFFBBF24), // Amber-400
                modifier = Modifier.weight(1f),
                onClick = { onStatusChange("pending") }
            )
        }
    }
}

@Composable
fun VoteButton(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) activeColor else Color.White.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.4f)
        )
    }
}
