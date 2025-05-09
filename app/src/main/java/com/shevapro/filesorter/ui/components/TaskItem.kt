package com.shevapro.filesorter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.shevapro.filesorter.ui.components.ads.AdBanner
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.FolderPicker
import com.shevapro.filesorter.R
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.Utility.formatUriToUIString
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.ScheduleType
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TaskItem(
    modifier: Modifier = Modifier,
    task: UITaskRecord,
    onClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
    onToggleState: () -> Unit = {},
    onToggleScheduled: () -> Unit = {},
    onSchedule: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {

    val nmbrOfChrs = task.extension.length

    val textSize =
        if (nmbrOfChrs < 3) 16.sp else if (nmbrOfChrs < 5) 14.sp else if (nmbrOfChrs < 8) 10.sp else 9.sp



    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable {
                onClick()
            }
            .background(
                colorResource(id = R.color.fiery_rose).copy(alpha = 0.1F),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(8.dp)
        ,
//        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: Extension circle and active state
        Row(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(horizontal = 8.dp)
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Extension circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        shape = CircleShape,
                        color = Color(Utility.generateGoldenRatioColor(task.extension))
                    )
            ) {
                if (!task.errorMessage.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            modifier = Modifier
                                .background(shape = CircleShape, color = Color.Yellow.copy(0.8f))
                                .size(20.dp)
                                .border((1).dp, Color.Red, CircleShape)
                                .padding(2.dp),
                            imageVector = Icons.Outlined.AddAlert,
                            tint = Color.Red,
                            contentDescription = "alert for error message",
                        )
                    }
                }
                Text(
                    task.extension,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize
                )
            }
            // Source & destination folder paths
            Column(
                modifier = Modifier
                    .weight(1f)
                .padding(horizontal = 4.dp)
                ,verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FolderPath(stringResource(R.string.from), task.source)
                FolderPath(stringResource(R.string.to), task.destination)
            }
//            Spacer(modifier = Modifier.weight(1f))

            // Active state
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isActive,
                    onCheckedChange = { onToggleState() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorResource(id = R.color.fiery_rose)
                    )
                )

                Text(
                    text = if (task.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isActive) colorResource(id = R.color.fiery_rose) else Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }



        // Schedule info
        val sectionAlpha by animateFloatAsState(targetValue = if (task.isActive) 1.0f else 0.7f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(alpha = sectionAlpha)
                .padding(horizontal = 8.dp)
            , verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Auto-Schedule: ",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colors.secondaryVariant
            )
            Switch(
                checked = task.isScheduled,
                onCheckedChange = { if (task.isActive) onToggleScheduled() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorResource(id = R.color.fiery_rose),
                    checkedTrackColor = colorResource(id = R.color.fiery_rose).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                ),
                enabled = task.isActive
            )
            AnimatedVisibility(!task.isActive) {
                Text(
                    text = "Enable task above to use scheduling",
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colors.secondaryVariant.copy(alpha = 0.9f),
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
        // Bottom row: Schedule button, Edit button and Delete button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Display current schedule status (always visible)
            val scheduleStatusText = when {
                task.scheduleTime != null -> {
                    val pattern = when (task.scheduleType) {
                        ScheduleType.DAILY -> "HH:mm"
                        ScheduleType.WEEKLY -> "EEE 'at' HH:mm" // Shorter for summary
                        else -> "MMM d, HH:mm" // Shorter for summary
                    }
                    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
                    val typePrefix = when (task.scheduleType) {
                        ScheduleType.DAILY -> "Daily"
                        ScheduleType.WEEKLY -> "Weekly"
                        else -> "Once"
                    }
                    "$typePrefix: ${dateFormat.format(task.scheduleTime)}"
                }
                task.scheduleType == ScheduleType.NEVER  && !task.isScheduled -> "Scheduling disabled"
                task.isScheduled -> "Schedule pending"
                else -> "Not scheduled"
            }
            val scheduleStatusColor by animateColorAsState(
                when {
                    !task.isActive -> Color.Gray
                    task.scheduleType == ScheduleType.NEVER -> Color.Gray
                    task.isScheduled && task.scheduleTime != null -> MaterialTheme.colors.primaryVariant
                    task.isScheduled -> MaterialTheme.colors.secondaryVariant.copy(alpha = 0.7f)
                    else -> Color.Gray
                }
            )
            Text(
                text = scheduleStatusText,
                style = MaterialTheme.typography.body2,
                color = scheduleStatusColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))

            // Schedule button
            val scheduleIconTintColor by animateColorAsState(
                if (task.isActive) {
                    when {
                        task.scheduleType == ScheduleType.NEVER -> Color.Gray
                        task.isScheduled && task.scheduleTime != null -> MaterialTheme.colors.primaryVariant
                        task.isScheduled -> MaterialTheme.colors.secondaryVariant
                        else -> MaterialTheme.colors.secondaryVariant.copy(alpha = 0.7f)
                    }
                } else Color.Gray
            )
            val scheduleTooltip = if (task.scheduleTime != null) {
                stringResource(R.string.edit_schedule)
            } else if (task.scheduleType == ScheduleType.NEVER) {
                stringResource(R.string.enable_scheduling)
            } else if (task.isScheduled) {
                stringResource(R.string.complete_schedule_setup)
            } else {
                stringResource(R.string.set_schedule)
            }
            IconButton(
                onClick = { if (task.isActive) onSchedule() },
                enabled = task.isActive,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = scheduleTooltip,
                    tint = scheduleIconTintColor
                )
            }

            // Edit button
            IconButton(
                onClick = { onEditClick() },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = MaterialTheme.colors.secondaryVariant
                )
            }

            // Delete button
            IconButton(onClick = {
                onRemoveClick()
            },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = stringResource(
                        id = R.string.delete_item_with_id_and_extension,
                        task.id,
                        task.extension
                    ),
                    tint = MaterialTheme.colors.error // Consistent error color
                )
            }
        }
    }

}

@Composable
private fun FolderPath(source: String, path: String) {

//    val formattedDestination = if (destination.length > 16) {
//
//        val first = destination.substringBefore("/")
//        val last =
//            if (destination.contains(":") && destination.substringAfterLast(":").length > 2) {
//                destination.substringAfterLast(":").substringAfterLast("/")
//            } else destination.substringBeforeLast("/")
//        first + last
//    } else destination.replaceFirst("/tree","Root")


    val formattedPath = formatUriToUIString(path)

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            source, color = Color.Gray, modifier = Modifier
                .width(30.dp), maxLines = 2, fontSize = 12.sp
        )

        Text(
            " : ", color = colorResource(R.color.jet),
            modifier = Modifier.padding(horizontal = 1.dp)
        )
        Text(
            formattedPath,
            color = colorResource(R.color.jet),
            maxLines = 1,
            fontSize = 14.sp,
//            overflow = TextOverflow.Ellipsis
        )
    }


}

@Composable
fun TaskListContent(
    tasksList: List<UITaskRecord>,
    onItemClick: (UITaskRecord) -> Unit,
    onRemoveItem: (UITaskRecord) -> Unit,
    onToggleState: (UITaskRecord) -> Unit,
    onToggleScheduled: (UITaskRecord) -> Unit = {},
    onSchedule: (UITaskRecord) -> Unit = {},
    onEditClick: (UITaskRecord) -> Unit = {}
) {


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
//            .background(colorResource(R.color.raisin_black).copy(0.1f))
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {

        itemsIndexed(tasksList) { index, task ->
            // Display the task item
            // Insert first ad at position 3, then every 5 items after that
            if ((index == 2) || (index > 3 && (index - 3) % 5 == 0 && index < tasksList.size - 1)) {
                AdBanner(Modifier.padding( vertical = 8.dp))
            } else TaskItem(
                modifier = Modifier.padding(8.dp, vertical = 4.dp),
                task = task,
                onClick = { onItemClick(task) },
                onRemoveClick = { onRemoveItem(task) },
                onToggleState = { onToggleState(task) },
                onToggleScheduled = { onToggleScheduled(task) },
                onSchedule = { onSchedule(task) },
                onEditClick = { onEditClick(task) }
            )


        }


    }
}
