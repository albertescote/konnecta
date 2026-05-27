package com.konnecta.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.konnecta.app.data.model.ActivityWithParticipants
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import com.konnecta.app.utils.CalendarUtils
import com.konnecta.app.utils.DateUtils
import com.konnecta.app.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailsBottomSheet(
    activity: ActivityWithParticipants,
    onDismiss: () -> Unit,
    viewModel: DashboardViewModel
) {
    val currentUserId = viewModel.getCurrentUserId() ?: ""
    val isCreator = activity.creator_id == currentUserId
    val myParticipation = activity.activity_participants.find { it.user_id == currentUserId }
    val isJoined = myParticipation != null
    val hasPlusOne = (myParticipation?.additional_participants ?: 0) > 0
    val totalAttendance = activity.activity_participants.sumOf { 1 + it.additional_participants }

    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(activity.title) }
    var editDescription by remember { mutableStateOf(activity.description ?: "") }
    var editStartDate by remember { mutableStateOf(activity.start_date ?: activity.weekend_date) }
    var editEndDate by remember { mutableStateOf(activity.end_date ?: "") }
    var editStartHour by remember {
        mutableStateOf(
            activity.start_time?.split(":")?.getOrNull(0) ?: "19"
        )
    }
    var editStartMinute by remember {
        mutableStateOf(
            activity.start_time?.split(":")?.getOrNull(1) ?: "00"
        )
    }
    var editEndHour by remember {
        mutableStateOf(
            activity.end_time?.split(":")?.getOrNull(0) ?: "22"
        )
    }
    var editEndMinute by remember {
        mutableStateOf(
            activity.end_time?.split(":")?.getOrNull(1) ?: "00"
        )
    }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val headerBg = MaterialTheme.colorScheme.surfaceVariant
    val bodyBg = MaterialTheme.colorScheme.surface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = headerBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(top = 14.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isCreator) {
                        if (!isEditing) {
                            HeaderIconButton(
                                onClick = { isEditing = true },
                                bg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            HeaderIconButton(
                                onClick = {
                                    viewModel.deleteActivity(activity.id, activity.weekend_date)
                                    onDismiss()
                                },
                                bg = Color.Red.copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Esborrar",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            HeaderIconButton(
                                onClick = {
                                    viewModel.updateActivity(
                                        activity.id,
                                        mapOf(
                                            "title" to editTitle,
                                            "description" to editDescription,
                                            "start_date" to editStartDate,
                                            "end_date" to editEndDate.ifBlank { null },
                                            "start_time" to "$editStartHour:$editStartMinute",
                                            "end_time" to "$editEndHour:$editEndMinute"
                                        ),
                                        activity.weekend_date
                                    )
                                    isEditing = false
                                },
                                bg = Color(0xFF22C55E).copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Guardar",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            HeaderIconButton(
                                onClick = { isEditing = false },
                                bg = Color.Red.copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Undo,
                                    contentDescription = "Cancel·lar",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                HeaderIconButton(
                    onClick = onDismiss,
                    bg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Tancar",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF3B82F6)
                            ),
                            placeholder = {
                                Text(
                                    "Títol del pla",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    } else {
                        val eventDate =
                            DateUtils.parseDbDate(activity.start_date ?: activity.weekend_date)
                        val endDate =
                            if (activity.end_date != null) DateUtils.parseDbDate(activity.end_date) else null
                        val isMultiDay = endDate != null && activity.start_date != activity.end_date

                        val dateText = if (isMultiDay) {
                            "${DateUtils.formatDayAndMonth(eventDate)} al ${
                                DateUtils.formatDayAndMonth(
                                    endDate!!
                                )
                            }"
                        } else {
                            "${DateUtils.formatDayOfWeek(eventDate)} ${
                                DateUtils.formatDayAndMonth(
                                    eventDate
                                )
                            }"
                        }

                        Text(
                            text = dateText.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                        Text(
                            text = activity.title,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            lineHeight = 26.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            // ── Body ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bodyBg)
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Description & time info / edit fields
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp),
                            placeholder = { Text("Descripció (opcional)") },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = headerBg.copy(alpha = 0.5f),
                                focusedContainerColor = headerBg.copy(alpha = 0.5f),
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "INICI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TimeSelect(
                                        label = "Hora",
                                        value = editStartHour,
                                        options = (0..23).map { it.toString().padStart(2, '0') },
                                        onValueChange = { editStartHour = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    TimeSelect(
                                        label = "Min",
                                        value = editStartMinute,
                                        options = listOf("00", "15", "30", "45"),
                                        onValueChange = { editStartMinute = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "FINAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TimeSelect(
                                        label = "Hora",
                                        value = editEndHour,
                                        options = (0..23).map { it.toString().padStart(2, '0') },
                                        onValueChange = { editEndHour = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    TimeSelect(
                                        label = "Min",
                                        value = editEndMinute,
                                        options = listOf("00", "15", "30", "45"),
                                        onValueChange = { editEndMinute = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else {
                        val hasDescription = !activity.description.isNullOrBlank()
                        Text(
                            text = activity.description?.takeIf { it.isNotBlank() }
                                ?: "Sense descripció addicional.",
                            fontSize = 14.sp,
                            color = if (hasDescription)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = if (hasDescription) FontStyle.Normal else FontStyle.Italic,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            InfoChip(
                                icon = Icons.Outlined.Schedule,
                                text = "${activity.start_time ?: "19:00"}h" + (if (activity.end_time != null) " - ${activity.end_time}h" else "")
                            )
                            InfoChip(
                                icon = Icons.Outlined.Groups,
                                text = "$totalAttendance ${if (totalAttendance == 1) "PERSONA" else "PERSONES"}"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            OutlinedButton(
                                onClick = { CalendarUtils.addToCalendar(context, activity) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.5f
                                    ),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CALENDARI", fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = { ShareUtils.shareActivity(context, activity) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF22C55E).copy(alpha = 0.15f),
                                    contentColor = Color(0xFF22C55E)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
                            ) {
                                Icon(
                                    Icons.Outlined.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("COMPARTIR", fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Participants list
                if (!isEditing) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "QUI S'HA APUNTAT?",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        if (activity.activity_participants.isEmpty()) {
                            val dashedBorderColor =
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .drawBehind {
                                        drawRoundRect(
                                            color = dashedBorderColor,
                                            cornerRadius = CornerRadius(16.dp.toPx()),
                                            style = Stroke(
                                                width = 1.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(
                                                    floatArrayOf(
                                                        8f,
                                                        5f
                                                    ), 0f
                                                )
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Encara ningú",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                activity.activity_participants.forEach { ParticipantRow(it) }
                            }
                        }
                    }
                }

                // Bottom actions
                if (!isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isJoined) {
                            Button(
                                onClick = {
                                    viewModel.updateParticipation(
                                        activity.id, true,
                                        if (hasPlusOne) 0 else 1,
                                        activity.weekend_date
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasPlusOne)
                                        Color(0xFF3B82F6).copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (hasPlusOne)
                                        Color(0xFF3B82F6)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = if (hasPlusOne) BorderStroke(
                                    1.dp,
                                    Color(0xFF3B82F6).copy(alpha = 0.3f)
                                ) else null
                            ) {
                                Icon(
                                    if (hasPlusOne) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (hasPlusOne) "TREURE +1" else "AFEGIR +1",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.updateParticipation(
                                    activity.id,
                                    !isJoined,
                                    0,
                                    activity.weekend_date
                                )
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isJoined)
                                    Color.Red.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                contentColor = if (isJoined) Color.Red else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isJoined) BorderStroke(
                                1.dp,
                                Color.Red.copy(alpha = 0.3f)
                            ) else null
                        ) {
                            Text(
                                if (isJoined) "SORTIR DEL PLA" else "APUNTA'T AL PLA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    bg: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ParticipantRow(participant: com.konnecta.app.data.model.ParticipantWithProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (participant.profiles.avatar_url != null) {
                    AsyncImage(
                        model = participant.profiles.avatar_url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = (participant.profiles.full_name
                            ?: participant.profiles.email).take(1).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (participant.additional_participants > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                        .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Text(
                        text = "+${participant.additional_participants}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center),
                        lineHeight = 8.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = participant.profiles.full_name ?: participant.profiles.email.split("@")[0],
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (participant.additional_participants > 0) {
                Text(
                    text = "+ ${participant.additional_participants} ${if (participant.additional_participants == 1) "acompanyant" else "acompanyants"}",
                    fontSize = 10.sp,
                    color = Color(0xFF3B82F6),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
