package com.konnecta.app.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.konnecta.app.data.model.ActivityWithParticipants
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import com.konnecta.app.utils.DateUtils
import com.konnecta.app.utils.ShareUtils
import java.text.SimpleDateFormat
import java.util.*

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
    
    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(activity.title) }
    var editDescription by remember { mutableStateOf(activity.description ?: "") }
    var editStartDate by remember { mutableStateOf(activity.start_date ?: activity.weekend_date) }
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Actions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isCreator) {
                        IconButton(
                            onClick = { 
                                if (isEditing) {
                                    // SAVE ACTION
                                    viewModel.updateActivity(
                                        activity.id,
                                        mapOf("title" to editTitle, "description" to editDescription, "start_date" to editStartDate),
                                        activity.weekend_date
                                    )
                                    isEditing = false
                                } else {
                                    isEditing = true
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isEditing) Color(0xFF22C55E) else Color.Black.copy(alpha = 0.05f))
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Guardar" else "Editar",
                                tint = if (isEditing) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (!isEditing) {
                            IconButton(
                                onClick = { 
                                    viewModel.deleteActivity(activity.id, activity.weekend_date)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Esborrar",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            // Cancel button when editing
                            IconButton(
                                onClick = { 
                                    isEditing = false
                                    editTitle = activity.title
                                    editDescription = activity.description ?: ""
                                    editStartDate = activity.start_date ?: activity.weekend_date
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel·lar",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tancar", modifier = Modifier.size(18.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 20.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF3B82F6)
                            )
                        )
                    } else {
                        val date = DateUtils.parseDbDate(activity.start_date ?: activity.weekend_date)
                        Text(
                            text = DateUtils.formatDayOfWeek(date).uppercase() + " " + DateUtils.formatDayAndMonth(date).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                        Text(
                            text = activity.title.uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            lineHeight = 28.sp,
                            letterSpacing = (-1).sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Description
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            placeholder = { Text("Descripció del pla...") },
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else {
                        Text(
                            text = activity.description ?: "Sense descripció addicional.",
                            fontSize = 15.sp,
                            color = if (activity.description.isNullOrBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                            fontStyle = if (activity.description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (!isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                        ) {
                            InfoChip(icon = Icons.Outlined.Schedule, text = "${activity.start_time ?: "19:00"}h")
                            InfoChip(icon = Icons.Outlined.Groups, text = "${activity.activity_participants.sumOf { 1 + it.additional_participants }} PERSONES")
                        }

                        // WhatsApp Share
                        Button(
                            onClick = { ShareUtils.shareActivity(context, activity) },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E).copy(alpha = 0.1f),
                                contentColor = Color(0xFF22C55E)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Outlined.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("COMPARTIR", fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }

                // Participants List
                if (!isEditing) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "QUI S'HA APUNTAT?",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray.copy(alpha = 0.6f),
                            letterSpacing = 2.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        if (activity.activity_participants.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .height(80.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Encara ningú",
                                    fontSize = 12.sp,
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                activity.activity_participants.forEach { participant ->
                                    ParticipantRow(participant)
                                }
                            }
                        }
                    }
                }

                // Bottom Actions
                if (!isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isJoined) {
                            Button(
                                onClick = { 
                                    viewModel.updateParticipation(activity.id, true, if (hasPlusOne) 0 else 1, activity.weekend_date)
                                },
                                modifier = Modifier.weight(1f).height(60.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasPlusOne) Color(0xFF3B82F6).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (hasPlusOne) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(if (hasPlusOne) Icons.Default.PersonRemove else Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (hasPlusOne) "TREURE +1" else "AFEGIR +1", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Button(
                            onClick = { 
                                viewModel.updateParticipation(activity.id, !isJoined, 0, activity.weekend_date)
                            },
                            modifier = Modifier.weight(1.5f).height(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isJoined) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface,
                                contentColor = if (isJoined) Color.Red else MaterialTheme.colorScheme.surface
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text(if (isJoined) "SORTIR DEL PLA" else "APUNTA'T AL PLA", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 0.5.sp)
    }
}

@Composable
fun ParticipantRow(participant: com.konnecta.app.data.model.ParticipantWithProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
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
                    text = (participant.profiles.full_name ?: participant.profiles.email).take(1).uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = participant.profiles.full_name ?: participant.profiles.email.split("@")[0],
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
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
