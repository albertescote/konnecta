package com.konnecta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konnecta.app.data.model.Group
import com.konnecta.app.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelectorBottomSheet(
    groups: List<Group>,
    activeGroupId: String,
    currentUserId: String,
    onGroupSelected: (String) -> Unit,
    onGroupCreated: (Group) -> Unit,
    onInviteClick: (Group) -> Unit,
    onDismiss: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    var showGroupManagement by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }
    
    val activeGroup = groups.find { it.id == activeGroupId }
    val isAdmin = activeGroup?.role == "admin"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ELS MEUS GRUPS",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tancar")
                }
            }

            // Group List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groups.forEach { group ->
                    val isSelected = group.id == activeGroupId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { 
                                onGroupSelected(group.id)
                                onDismiss()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = group.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
                            )
                            Text(
                                text = group.role?.uppercase() ?: "MEMBER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray
                            )
                        }
                        
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seleccionat",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Group Management (Active Group)
            if (activeGroup != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(
                            text = "GESTIÓ DE GRUP: ${activeGroup.name.uppercase()}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isAdmin) {
                            OutlinedButton(
                                onClick = { onInviteClick(activeGroup) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline))
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CONVIDAR", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        OutlinedButton(
                            onClick = { showGroupManagement = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline))
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GESTIONAR", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Create Group Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(
                        text = "VOLS CREAR UN GRUP?",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
                OutlinedButton(
                    onClick = { showCreateGroup = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline))
                ) {
                    Text("CREAR NOU GRUP", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    if (showGroupManagement && activeGroup != null) {
        GroupManagementBottomSheet(
            group = activeGroup,
            currentUserId = currentUserId,
            onDismiss = { showGroupManagement = false },
            viewModel = dashboardViewModel
        )
    }

    if (showCreateGroup) {
        CreateGroupBottomSheet(
            onDismiss = { showCreateGroup = false },
            onGroupCreated = onGroupCreated,
            viewModel = dashboardViewModel
        )
    }
}
