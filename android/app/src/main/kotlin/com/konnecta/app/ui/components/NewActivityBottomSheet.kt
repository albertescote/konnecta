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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konnecta.app.data.model.Activity
import com.konnecta.app.ui.viewmodel.DashboardViewModel
import com.konnecta.app.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivityBottomSheet(
    weekendDate: String,
    groupId: String,
    freeDate: Boolean = false,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("dissabte") }
    var isFlexible by remember { mutableStateOf(freeDate) }

    val anchorDate = remember { DateUtils.parseDbDate(weekendDate) }
    var startDate by remember {
        mutableStateOf(
            if (freeDate) weekendDate
            else DateUtils.formatDbDate(DateUtils.addDays(anchorDate, 1))
        )
    }
    var startHour by remember { mutableStateOf("19") }
    var startMinute by remember { mutableStateOf("00") }
    
    var isMultiDay by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf(DateUtils.formatDbDate(DateUtils.addDays(anchorDate, 2))) }
    var selectedEndDay by remember { mutableStateOf("diumenge") }
    var endHour by remember { mutableStateOf("22") }
    var endMinute by remember { mutableStateOf("00") }
    val dayIndex = mapOf("divendres" to 0, "dissabte" to 1, "diumenge" to 2)
    
    var isPending by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val daysData = listOf(
        Triple("divendres", "DIV", anchorDate),
        Triple("dissabte", "DIS", DateUtils.addDays(anchorDate, 1)),
        Triple("diumenge", "DIU", DateUtils.addDays(anchorDate, 2))
    )

    fun showDatePicker(initialDate: String, onDateSelected: (String) -> Unit) {
        val date = DateUtils.parseDbDate(initialDate)
        val cal = Calendar.getInstance()
        cal.time = date
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, dayOfMonth)
                onDateSelected(DateUtils.formatDbDate(newCal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NOU PLA",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tancar")
                }
            }

            // Quick Day Selector — only in weekend-locked mode
            if (!freeDate) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    daysData.forEach { (id, label, date) ->
                        val isSelected = selectedDay == id
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedDay = id
                                    startDate = DateUtils.formatDbDate(date)
                                    if ((dayIndex[selectedEndDay] ?: 1) < (dayIndex[id] ?: 1)) {
                                        selectedEndDay = id
                                        endDate = DateUtils.formatDbDate(date)
                                    }
                                }
                                .padding(vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                            Text(
                                text = SimpleDateFormat("d", Locale.getDefault()).format(date),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }
                    }
                }
            }

            // Start Section
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF3B82F6)))
                    Text(
                        text = "INICI DEL PLA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }

                if (isFlexible) {
                    Button(
                        onClick = { showDatePicker(startDate) { startDate = it } },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = startDate, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeSelect(
                        label = "Hora",
                        value = startHour,
                        options = (0..23).map { it.toString().padStart(2, '0') },
                        onValueChange = { startHour = it },
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    TimeSelect(
                        label = "Min",
                        value = startMinute,
                        options = listOf("00", "15", "30", "45"),
                        onValueChange = { startMinute = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isMultiDay = !isMultiDay }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isMultiDay,
                        onCheckedChange = { isMultiDay = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                    )
                    Text(
                        text = "AFEGIR FINALITZACIÓ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // End Section
            AnimatedVisibility(visible = isMultiDay) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red))
                        Text(
                            text = "FINAL DEL PLA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    if (freeDate) {
                        Button(
                            onClick = { showDatePicker(endDate) { endDate = it } },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = endDate, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            daysData.forEach { (id, label, date) ->
                                val isSelected = selectedEndDay == id
                                val isEnabled = (dayIndex[id] ?: 1) >= (dayIndex[selectedDay] ?: 1)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                        .then(
                                            if (isEnabled) Modifier.clickable {
                                                selectedEndDay = id
                                                endDate = DateUtils.formatDbDate(date)
                                            } else Modifier
                                        )
                                        .padding(vertical = 7.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onSurface
                                            !isEnabled -> Color.Gray.copy(alpha = 0.3f)
                                            else -> Color.Gray
                                        }
                                    )
                                    Text(
                                        text = SimpleDateFormat("d", Locale.getDefault()).format(date),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onSurface
                                            !isEnabled -> Color.Gray.copy(alpha = 0.3f)
                                            else -> Color.Gray
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeSelect(
                            label = "Hora",
                            value = endHour,
                            options = (0..23).map { it.toString().padStart(2, '0') },
                            onValueChange = { endHour = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(":", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                        TimeSelect(
                            label = "Min",
                            value = endMinute,
                            options = listOf("00", "15", "30", "45"),
                            onValueChange = { endMinute = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Title & Description
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (it.length <= 50) title = it },
                        placeholder = { Text("Títol del pla") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = "${title.length}/50",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = if (title.length >= 45) Color.Red else Color.Gray,
                        modifier = Modifier.align(Alignment.End).padding(end = 4.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 200) description = it },
                        placeholder = { Text("Detalls (opcional)") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = "${description.length}/200",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = if (description.length >= 180) Color.Red else Color.Gray,
                        modifier = Modifier.align(Alignment.End).padding(end = 4.dp)
                    )
                }
            }

            // Submit
            Button(
                onClick = {
                    val userId = viewModel.getCurrentUserId() ?: return@Button
                    isPending = true
                    viewModel.createActivity(
                        Activity(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            group_id = groupId,
                            start_date = startDate,
                            end_date = if (isMultiDay) endDate else null,
                            start_time = "$startHour:$startMinute",
                            end_time = if (isMultiDay) "$endHour:$endMinute" else null,
                            creator_id = userId,
                            weekend_date = if (freeDate) startDate else weekendDate,
                            day_of_week = if (freeDate) dayOfWeekFromDate(startDate) else selectedDay
                        )
                    ) { success ->
                        isPending = false
                        if (success) {
                            Toast.makeText(context, "Pla creat correctament", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Error al crear el pla", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
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

private fun dayOfWeekFromDate(dateStr: String): String {
    return try {
        val cal = Calendar.getInstance().apply { time = DateUtils.parseDbDate(dateStr) }
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "dilluns"
            Calendar.TUESDAY -> "dimarts"
            Calendar.WEDNESDAY -> "dimecres"
            Calendar.THURSDAY -> "dijous"
            Calendar.FRIDAY -> "divendres"
            Calendar.SATURDAY -> "dissabte"
            Calendar.SUNDAY -> "diumenge"
            else -> "dissabte"
        }
    } catch (e: Exception) { "dissabte" }
}

@Composable
fun TimeSelect(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "$value${if (label == "Hora") "h" else "m"}", fontWeight = FontWeight.Bold)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("$option${if (label == "Hora") "h" else "m"}") },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
