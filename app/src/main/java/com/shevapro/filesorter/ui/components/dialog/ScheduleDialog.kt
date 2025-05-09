package com.shevapro.filesorter.ui.components.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shevapro.filesorter.R
import com.shevapro.filesorter.model.ScheduleType
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.ui.components.common.NumberPickerColumn
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.format

/**
 * A dialog for scheduling task execution
 *
 * @param task The task to schedule
 * @param onScheduleSet Callback when the schedule is set
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun ScheduleDialog(
    task: UITaskRecord,
    onScheduleSet: (ScheduleType, Date?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedScheduleType by remember { mutableStateOf(task.scheduleType) }
    var selectedDate by remember { mutableStateOf(task.scheduleTime ?: Date()) }
    val calendar = remember { Calendar.getInstance() }

    // Parse the initial date
    calendar.time = selectedDate

    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    // Update the selected date whenever any component changes
    LaunchedEffect(year, month, day, hour, minute) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        selectedDate = calendar.time
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Schedule Rule",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Select when to run this rule:",
                    modifier = Modifier.padding(bottom = 16.dp).animateContentSize(),
                    style = MaterialTheme.typography.subtitle1
                )

                // Schedule type selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Once option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedScheduleType = ScheduleType.ONCE }
                    ) {
                        RadioButton(
                            selected = selectedScheduleType == ScheduleType.ONCE,
                            onClick = { selectedScheduleType = ScheduleType.ONCE },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colors.primaryVariant,
                                unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = "Once",
                            color = MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Daily option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedScheduleType = ScheduleType.DAILY }
                    ) {
                        RadioButton(
                            selected = selectedScheduleType == ScheduleType.DAILY,
                            onClick = { selectedScheduleType = ScheduleType.DAILY },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colors.primaryVariant,
                                unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = "Daily",
                            color = MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Weekly option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedScheduleType = ScheduleType.WEEKLY }
                    ) {
                        RadioButton(
                            selected = selectedScheduleType == ScheduleType.WEEKLY,
                            onClick = { selectedScheduleType = ScheduleType.WEEKLY },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colors.primaryVariant,
                                unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = "Weekly",
                            color = MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Never option
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.95f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { selectedScheduleType = ScheduleType.NEVER }
                            .padding(start = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedScheduleType == ScheduleType.NEVER,
                            onClick = { selectedScheduleType = ScheduleType.NEVER },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colors.primaryVariant,
                                unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = "Never (disable scheduling)",
                            color = MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Weekly day selection - Show only for WEEKLY
                AnimatedVisibility(visible = selectedScheduleType == ScheduleType.WEEKLY) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .animateContentSize()
                    ) {
                        Text(
                            text = "Which day of the week?",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Show the days of week
                        val daysOfWeek = remember{
                            java.text.DateFormatSymbols().weekdays
                                .filterIndexed { i, _ -> i in 1..7 }
                                .map { it.take(3) }
                        }
                        val selectedDayOfWeek = remember {
                            mutableStateOf(calendar.get(Calendar.DAY_OF_WEEK) - 1) // Calendar.SUNDAY is 1
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val newCalendar = remember { Calendar.getInstance() }
                            daysOfWeek.forEachIndexed { index, dayName ->
                                val isSelected = selectedDayOfWeek.value == index

                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colors.primaryVariant.copy(alpha = 0.2f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            selectedDayOfWeek.value = index

                                            // Update the calendar to this day of the week
                                            // Set to next occurrence of this day
                                            val currentDayOfWeek = newCalendar.get(Calendar.DAY_OF_WEEK) - 1
                                            val daysToAdd = if (index >= currentDayOfWeek) {
                                                index - currentDayOfWeek
                                            } else {
                                                7 - (currentDayOfWeek - index)
                                            }

                                            newCalendar.add(Calendar.DAY_OF_MONTH, daysToAdd)

                                            // Keep the time from the existing calendar
                                            newCalendar.set(Calendar.HOUR_OF_DAY, hour)
                                            newCalendar.set(Calendar.MINUTE, minute)

                                            // Update the state variables
                                            year = newCalendar.get(Calendar.YEAR)
                                            month = newCalendar.get(Calendar.MONTH)
                                            day = newCalendar.get(Calendar.DAY_OF_MONTH)
                                        }
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = dayName,
                                        color = if (isSelected) MaterialTheme.colors.primaryVariant else LocalContentColor.current,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Show the selected date for reference
                        val dateFormat = remember{ SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                        Text(
                            text = "Selected date: ${dateFormat.format(selectedDate)}",
                            style = MaterialTheme.typography.caption.copy(
                                color = MaterialTheme.colors.onSurface.copy(
                                    alpha = 0.7f
                                )
                            ),
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                }

                // Date selection - Show only for ONCE
                AnimatedVisibility(visible = selectedScheduleType == ScheduleType.ONCE) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .animateContentSize()
                    ) {
                        Text(
                            text = "Date:",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Month picker
                            NumberPickerColumn(
                                label = "Month",
                                value = month + 1, // Month is 0-based in Calendar
                                range = 1..12,
                                onValueChange = { month = it - 1 },
                                modifier = Modifier.weight(1f)
                            )

                            // Day picker
                            NumberPickerColumn(
                                label = "Day",
                                value = day,
                                range = 1..31, // Simplified, should ideally account for month
                                onValueChange = { day = it },
                                modifier = Modifier.weight(1f)
                            )

                            // Year picker
                            NumberPickerColumn(
                                label = "Year",
                                value = year,
                                range = year..(year + 5), // Allow scheduling up to 5 years ahead
                                onValueChange = { year = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Time selection - Show only when not NEVER
                AnimatedVisibility(visible = selectedScheduleType != ScheduleType.NEVER) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Text(
                            text = "Time:",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Hour picker
                            NumberPickerColumn(
                                label = "Hour",
                                value = hour,
                                range = 0..23,
                                onValueChange = { hour = it },
                                displayTransform = { "%02d".format(it) },
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = ":",
                                style = MaterialTheme.typography.h5,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .align(Alignment.CenterVertically)
                            )

                            // Minute picker
                            NumberPickerColumn(
                                label = "Min",
                                value = minute,
                                range = 0..59,
                                onValueChange = { minute = it },
                                displayTransform = { "%02d".format(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Show selected date and time
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    val pattern =
                        if (selectedScheduleType == ScheduleType.DAILY) "HH:mm" else if (selectedScheduleType == ScheduleType.WEEKLY) "EEEE 'at' HH:mm" else "EEE, MMM dd, yyyy HH:mm"
                    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())

                    AnimatedVisibility(visible = selectedScheduleType != ScheduleType.NEVER) {
                        val typeString =
                            if (selectedScheduleType == ScheduleType.DAILY) "Daily at"
                            else if (selectedScheduleType == ScheduleType.WEEKLY) "Weekly on"
                            else "Once on"
                        Text(
                            text = "Scheduled: $typeString ${dateFormat.format(selectedDate)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.secondary,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    AnimatedVisibility(visible = selectedScheduleType == ScheduleType.NEVER) {
                        Text(
                            text = "Scheduling disabled",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedScheduleType == ScheduleType.NEVER) {
                        onScheduleSet(selectedScheduleType, null)
                    } else {
                        val finalCalendar = Calendar.getInstance()
                        finalCalendar.time = selectedDate // Start with picker's date/time
                        if (selectedScheduleType == ScheduleType.DAILY) {
                            // For daily, we only care about H:M, date part will be handled by service
                        }
                        onScheduleSet(selectedScheduleType, finalCalendar.time)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.fiery_rose),
                    contentColor = Color.White
                )
            ) {
                Text("Set Schedule", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.fiery_rose)
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}
