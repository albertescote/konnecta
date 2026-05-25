package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konnecta.app.data.model.Activity
import com.konnecta.app.data.remote.ActivityService
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivityBottomSheet(
    weekendDate: String,
    groupId: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("dissabte") }
    var isPending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val activityService = ActivityService()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "NOU PLA",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp
            )

            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 50) title = it },
                label = { Text("Títol del pla") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                supportingText = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text("${title.length}/50")
                    }
                }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 200) description = it },
                label = { Text("Detalls (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(20.dp),
                supportingText = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text("${description.length}/200")
                    }
                }
            )

            // Day Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("divendres", "dissabte", "diumenge").forEach { day ->
                    val isSelected = selectedDay == day
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { selectedDay = day }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.take(3).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            isPending = true
                            try {
                                // Simplified for now, real app would handle all fields
                                activityService.createActivity(
                                    Activity(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        description = description,
                                        group_id = groupId,
                                        weekendDate = weekendDate,
                                        day_of_week = selectedDay,
                                        creator_id = "your-user-id", // Should come from auth
                                        created_at = "",
                                        start_time = "19:00"
                                    )
                                )
                                onSuccess()
                            } catch (e: Exception) {
                                // Handle error
                            } finally {
                                isPending = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = !isPending && title.isNotBlank()
            ) {
                Text(
                    text = if (isPending) "CREANT..." else "AFEGIR PLA",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
