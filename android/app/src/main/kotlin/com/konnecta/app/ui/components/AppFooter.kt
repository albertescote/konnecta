package com.konnecta.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    Text(
        text = "KONNECTA v1.0",
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = Color.Gray.copy(alpha = 0.6f),
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(vertical = 48.dp)
    )
}
