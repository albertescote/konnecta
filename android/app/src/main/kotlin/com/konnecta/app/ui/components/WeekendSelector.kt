package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
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
import com.konnecta.app.utils.DateUtils

@Composable
fun WeekendSelector(
    dates: List<String>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onMoreDatesClick: () -> Unit
) {
    // Only take first 5 as requested
    val limitedDates = dates.take(5)

    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // ... (rest of items)
        items(limitedDates) { dateStr ->
            val isSelected = dateStr == selectedDate
            val date = DateUtils.parseDbDate(dateStr)
            val month = DateUtils.formatMonth(date)
            val satDay = DateUtils.getDayOfMonth(DateUtils.addDays(date, 1))
            val sunDay = DateUtils.getDayOfMonth(DateUtils.addDays(date, 2))

            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onBackground 
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp, 
                        if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), 
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { onDateSelected(dateStr) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = month,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.background.copy(alpha = 0.6f) else Color.Gray
                    )
                    Text(
                        text = "$satDay-$sunDay",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 0.dp)
                    )
                    Text(
                        text = "DS-DG", // Capitalized
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(88.dp) // Match
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { onMoreDatesClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Més dates",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "MÉS", // Capitalized
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
