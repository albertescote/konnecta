package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konnecta.app.utils.DateUtils

@Composable
fun VotingSection(
    currentStatus: String?,
    currentComment: String?,
    weekendDate: String,
    onStatusChange: (String) -> Unit,
    onCommentSave: (String) -> Unit
) {
    val displayDate = DateUtils.formatDisplayDate(weekendDate)
    var commentText by remember(currentComment) { mutableStateOf(currentComment ?: "") }
    var isEditingComment by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val hasCommentChanged = commentText != (currentComment ?: "")

    // Reset editing mode when currentComment is updated from outside
    LaunchedEffect(currentComment) {
        isEditingComment = false
        focusManager.clearFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { 
                if (isEditingComment) {
                    isEditingComment = false
                    focusManager.clearFocus()
                }
            }
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

        // Comment Section
        Box(modifier = Modifier.fillMaxWidth()) {
            if (!isEditingComment) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable { isEditingComment = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        tint = if (!currentComment.isNullOrBlank()) Color(0xFF3B82F6) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (!currentComment.isNullOrBlank()) "EDITAR COMENTARI" else "AFEGIR COMENTARI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (!currentComment.isNullOrBlank()) Color(0xFF3B82F6) else Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { if (it.length <= 140) commentText = it },
                            placeholder = { Text("Ex: Tinc un sopar dissabte...", fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp)
                                .focusRequester(focusRequester),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6).copy(alpha = 0.3f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                cursorColor = Color(0xFF3B82F6)
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )
                        
                        // Close button for the text box - explicitly top right
                        IconButton(
                            onClick = { 
                                isEditingComment = false
                                focusManager.clearFocus()
                                commentText = currentComment ?: ""
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Tancar",
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        // Character counter - moved to BottomStart to avoid ALL overlaps
                        Text(
                            text = "${commentText.length}/140",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (commentText.length >= 130) Color.Red else Color.Gray,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 12.dp, bottom = 12.dp)
                        )

                        // Submit button - kept at BottomEnd
                        if (hasCommentChanged) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = { onCommentSave(commentText) },
                                    modifier = Modifier.size(36.dp),
                                    containerColor = Color(0xFF3B82F6),
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Guardar", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    if (hasCommentChanged) {
                        Text(
                            text = "COMENTARI PENDENT DE GUARDAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                LaunchedEffect(isEditingComment) {
                    if (isEditingComment) focusRequester.requestFocus()
                }
            }
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
