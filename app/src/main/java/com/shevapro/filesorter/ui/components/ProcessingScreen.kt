package com.shevapro.filesorter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.R
import com.shevapro.filesorter.model.TaskStats
import com.shevapro.filesorter.ui.theme.AppThemeLight
import kotlin.time.Duration.Companion.milliseconds
import kotlin.math.log10
import kotlin.math.pow
import com.shevapro.filesorter.model.AggregatedTaskStats
import kotlinx.coroutines.delay

/**
 * Formats bytes into a human-readable string (KB, MB, GB, etc.)
 */
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    return String.format("%.1f %s", bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups.coerceAtMost(units.size - 1)])
}

@Composable
fun ProgressScreen(
    currentMoveStats: TaskStats,
    isComplete: Boolean = false,
    onDone: () -> Unit = {}
) {

    // Check if there are no files to move
    if (currentMoveStats.totalFiles == 0) {
        EmptyTaskScreen(
            fileExtension = currentMoveStats.fileExtension,
            sourcePath = currentMoveStats.sourceFolder.substringAfterLast("/"),
            destinationPath = currentMoveStats.destinationFolder.substringAfterLast("/"),
            onDone = onDone
        )
        return
    }
    var timeoutReached by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        delay(5000) // 5 seconds
        timeoutReached = true
        println("ProgressScreen - Timeout reached")
    }

    println("ProgressScreen - isComplete: $isComplete")

    // Calculate progress as a float between 0 and 1
    val progressValue = when {
        // Force to 100% if we have any sign that processing is complete
        isComplete -> 1f
        // Normal calculation if we have files
        currentMoveStats.totalFiles > 0 && currentMoveStats.numberOfFilesMoved >= currentMoveStats.totalFiles -> 1f
        currentMoveStats.totalFiles > 0 -> (currentMoveStats.numberOfFilesMoved.toFloat() / currentMoveStats.totalFiles.toFloat())
        // Default fallback
        else -> 0f
    }

    // Consider it complete if progress is >= 99% or total files moved equals total files
    val effectivelyComplete = isComplete || progressValue >= 0.99f ||
            (currentMoveStats.totalFiles > 0 && currentMoveStats.numberOfFilesMoved >= currentMoveStats.totalFiles) || timeoutReached

    // Calculate the display values for file counts - show total/total if complete
    val displayedFileMoved =
        if (isComplete || effectivelyComplete) currentMoveStats.totalFiles else currentMoveStats.numberOfFilesMoved

    // Log file count info
    println(
        "ProgressScreen - Files: ${currentMoveStats.numberOfFilesMoved}/${currentMoveStats.totalFiles}, " +
                "Display: $displayedFileMoved/${currentMoveStats.totalFiles}, " +
                "isComplete: $isComplete, effectivelyComplete: $effectivelyComplete"
    )

    println("ProgressScreen - Progress: $progressValue, effectivelyComplete: $effectivelyComplete")

    // Animate the progress value
    val progress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
    )

    // Create pulsing animation for the app logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Create rotation animation for file icon
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Calculate elapsed time
    var elapsedTimeText by remember { mutableStateOf("Just started") }
    val startTime = remember { currentMoveStats.startTime }

    LaunchedEffect(currentMoveStats.numberOfFilesMoved) {
        val elapsedMs = System.currentTimeMillis() - startTime
        elapsedTimeText = when {
            elapsedMs < 1000 -> if (isComplete) "Less than 1 second" else "Just started"
            elapsedMs < 60000 -> "${elapsedMs / 1000} seconds"
            else -> "${elapsedMs / 60000} minutes ${(elapsedMs % 60000) / 1000} seconds"
        }
    }

    // Extract file extension for display
    val fileExtension = if (currentMoveStats.fileExtension.isNotEmpty()) {
        ".${currentMoveStats.fileExtension}"
    } else ""

    // Format source and destination paths for display
    val sourcePath = currentMoveStats.sourceFolder.substringAfterLast("/")
    val destPath = currentMoveStats.destinationFolder.substringAfterLast("/")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
//                bottom = 80.dp
            ), // Added 80dp bottom padding to account for ad banner
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator with app logo
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (isComplete) 80.dp else 120.dp),
                progress = progress,
                color = AppThemeLight.primary,
                strokeWidth = 8.dp
            )

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App logo",
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Progress percentage text
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = AppThemeLight.primary
        )

        Spacer(Modifier.height(8.dp))

        // Elapsed time
        Text(
            text = "${if (isComplete) "Total time: " else "Time elapsed: "}$elapsedTimeText",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // File details card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(2.dp, AppThemeLight.primary, RoundedCornerShape(12.dp)),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Current operation animation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Source folder",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Destination folder",
                        tint = AppThemeLight.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Divider()
                Spacer(Modifier.height(8.dp))

                // Current file being processed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = "File",
                        tint = AppThemeLight.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation)
                    )

                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "${if (isComplete) " Last" else " Current"} File:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = currentMoveStats.currentFileName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Progress counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Files Processed:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "$displayedFileMoved/${currentMoveStats.totalFiles}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Current file progress
                if (currentMoveStats.currentFileSize > 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current File Progress:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            // Format bytes in human-readable form
                            val currentBytes = formatBytes(currentMoveStats.currentBytesTransferred)
                            val totalBytes = formatBytes(currentMoveStats.currentFileSize)

                            Text(
                                text = "$currentBytes / $totalBytes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Progress bar for current file
                        val fileProgress = if (currentMoveStats.currentFileSize > 0) {
                            (currentMoveStats.currentBytesTransferred.toFloat() / currentMoveStats.currentFileSize.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        val animatedFileProgress by animateFloatAsState(
                            targetValue = fileProgress,
                            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                        )

                        LinearProgressIndicator(
                            progress = animatedFileProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF4CAF50) // Green color for file progress
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // Total bytes progress
                if (currentMoveStats.totalBytes > 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Progress:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            // Format bytes in human-readable form
                            val transferredBytes = formatBytes(currentMoveStats.totalBytesTransferred)
                            val totalBytes = formatBytes(currentMoveStats.totalBytes)

                            Text(
                                text = "$transferredBytes / $totalBytes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Progress bar for total bytes
                        val bytesProgress = if (currentMoveStats.totalBytes > 0) {
                            (currentMoveStats.totalBytesTransferred.toFloat() / currentMoveStats.totalBytes.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        val animatedBytesProgress by animateFloatAsState(
                            targetValue = bytesProgress,
                            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                        )

                        LinearProgressIndicator(
                            progress = animatedBytesProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF2196F3) // Blue color for total progress
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // File type being processed
                if (fileExtension.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "File Type:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = fileExtension,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // Source and destination folders
                if (sourcePath.isNotEmpty() && destPath.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "From:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = sourcePath,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "To:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = destPath,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Show Done button only when processing is complete
        if (effectivelyComplete) {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth(0.5f).padding(top=16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.button,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyTaskScreen(
    fileExtension: String,
    sourcePath: String,
    destinationPath: String,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            modifier = Modifier.size(80.dp),
            tint = AppThemeLight.primary
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "No Files to Move",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = AppThemeLight.primary
        )

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(2.dp, AppThemeLight.primary, RoundedCornerShape(12.dp)),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "No files with extension ${if (fileExtension.isNotEmpty()) ".$fileExtension" else "found"} need to be moved at this time.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Source and destination folders
                if (sourcePath.isNotEmpty() && destinationPath.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Source Folder:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = sourcePath,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Destination Folder:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = destinationPath,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.button,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AggregatedProgressScreen(
    aggregatedStats: AggregatedTaskStats,
    isComplete: Boolean = false,
    onDone: () -> Unit = {}
) {
    var timeoutReached by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        delay(5000) // 5 seconds
        timeoutReached = true
        println("AggregatedProgressScreen - Timeout reached")
    }

    LaunchedEffect(isComplete) { println("AggregatedProgressScreen - isComplete: $isComplete") }

    // Check if there are no files to move
    if (aggregatedStats.totalFiles == 0) {
        EmptyTaskScreen(
            fileExtension = aggregatedStats.currentFileExtension,
            sourcePath = aggregatedStats.currentSourceFolder.substringAfterLast("/"),
            destinationPath = aggregatedStats.currentDestinationFolder.substringAfterLast("/"),
            onDone = onDone
        )
        return
    }

    // Calculate progress as a float between 0 and 1
    val progressValue = when {
        // Force progress to 100% if tasks are complete
        aggregatedStats.currentTask >= aggregatedStats.totalTasks -> 1f
        // Normal calculation if we have files
        aggregatedStats.totalFiles > 0 -> (aggregatedStats.numberOfFilesMoved.toFloat() / aggregatedStats.totalFiles.toFloat())
        // Default fallback
        else -> 0f
    }

    // Check if task count is complete (at or exceeding total tasks)
    val taskProgressComplete = aggregatedStats.currentTask >= aggregatedStats.totalTasks

    // Calculate the display values for file counts - show total/total if complete
    val displayedFileMoved =
        if (isComplete || taskProgressComplete) aggregatedStats.totalFiles else aggregatedStats.numberOfFilesMoved

    // Log file count info
//    println(
//        "AggregatedProgressScreen - Files: ${aggregatedStats.numberOfFilesMoved}/${aggregatedStats.totalFiles}, " +
//                "Display: $displayedFileMoved/${aggregatedStats.totalFiles}, " +
//                "isComplete: $isComplete, taskProgressComplete: $taskProgressComplete"
//    )

    // Consider it complete if progress is >= 99% or taskProgressComplete is true
    val effectivelyComplete = isComplete || progressValue >= 0.99f || taskProgressComplete || timeoutReached

//    println("AggregatedProgressScreen - Progress: $progressValue, taskProgressComplete: $taskProgressComplete, effectivelyComplete: $effectivelyComplete")

    // Animate the progress value
    val progress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
    )

    // Create pulsing animation for the app logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Create rotation animation for file icon
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Calculate elapsed time
    var elapsedTimeText by remember { mutableStateOf("Just started") }
    val startTime = remember { aggregatedStats.startTime }

    LaunchedEffect(aggregatedStats.numberOfFilesMoved) {
        val elapsedMs = System.currentTimeMillis() - startTime
        elapsedTimeText = when {
            elapsedMs < 1000 -> if (isComplete) "Less than 1 second" else "Just started"
            elapsedMs < 60000 -> "${elapsedMs / 1000} seconds"
            else -> "${elapsedMs / 60000} minutes ${(elapsedMs % 60000) / 1000} seconds"
        }
    }

    // Format source and destination paths for display
    val sourcePath = aggregatedStats.currentSourceFolder.substringAfterLast("/")
    val destPath = aggregatedStats.currentDestinationFolder.substringAfterLast("/")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator with app logo
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (isComplete) 80.dp else 120.dp),
                progress = progress,
                color = AppThemeLight.primary,
                strokeWidth = 8.dp
            )

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App logo",
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Progress percentage text
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = AppThemeLight.primary
        )

        Spacer(Modifier.height(8.dp))

        // Elapsed time
        Text(
            text = "${if (isComplete) "Total time: " else "Time elapsed: "}$elapsedTimeText",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )

        Spacer(Modifier.height(16.dp))

        // File details card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(2.dp, AppThemeLight.primary, RoundedCornerShape(12.dp)),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Current task progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Task Progress:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "${aggregatedStats.currentTask}/${aggregatedStats.totalTasks}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Task progress indicator
                val taskProgress = if (aggregatedStats.totalTasks > 0) {
                    (aggregatedStats.currentTask.toFloat() / aggregatedStats.totalTasks.toFloat()).coerceIn(
                        0f,
                        1f
                    )
                } else 0f

                LinearProgressIndicator(
                    progress = taskProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFF9800) // Orange color for task progress
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Current operation animation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Source folder",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Destination folder",
                        tint = AppThemeLight.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Divider()

                // Current file being processed
              if (!isComplete) {
                  Spacer(Modifier.height(8.dp))

                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                          contentDescription = "File",
                          tint = AppThemeLight.primary,
                          modifier = Modifier
                              .size(20.dp)
                              .rotate(rotation)
                      )

                      Spacer(Modifier.width(8.dp))

                      Column {
                          Text(
                              text = "Current File:",
                              fontSize = 14.sp,
                              color = Color.Gray
                          )

                          Text(
                              text = aggregatedStats.currentFileName,
                              fontSize = 16.sp,
                              fontWeight = FontWeight.SemiBold,
                              overflow = TextOverflow.Ellipsis,
                              maxLines = 1
                          )
                      }
                  }
              }

                Spacer(Modifier.height(12.dp))

                // Current extension being processed
                if (aggregatedStats.currentFileExtension.isNotEmpty() && !isComplete) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Current File Type:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = ".${aggregatedStats.currentFileExtension}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // Completed extensions
                if (aggregatedStats.completedExtensions.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Processed Extensions:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Only display extensions that had files that were actually moved
                            val movedExtensions = aggregatedStats.completedExtensions.filter {
                                // This assumes that completedExtensions already contains only moved extensions
                                // If not, this would need to be filtered at the data source
                                true
                            }

                            movedExtensions.take(5).forEach { ext ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp, bottom = 6.dp)
                                        .background(
                                            AppThemeLight.primaryVariant.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ".$ext",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colors.onPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (movedExtensions.size > 5) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp, bottom = 6.dp)
                                        .background(
                                            AppThemeLight.primary.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${movedExtensions.size - 5} more",
                                        fontSize = 12.sp,
                                        color = AppThemeLight.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // Progress counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Files Processed:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "$displayedFileMoved/${aggregatedStats.totalFiles}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Current file progress
                if (aggregatedStats.currentFileSize > 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current File Progress:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            // Format bytes in human-readable form
                            val currentBytes = formatBytes(aggregatedStats.currentBytesTransferred)
                            val totalBytes = formatBytes(aggregatedStats.currentFileSize)

                            Text(
                                text = "$currentBytes / $totalBytes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Progress bar for current file
                        val fileProgress = if (aggregatedStats.currentFileSize > 0) {
                            (aggregatedStats.currentBytesTransferred.toFloat() / aggregatedStats.currentFileSize.toFloat()).coerceIn(
                                0f,
                                1f
                            )
                        } else 0f

                        val animatedFileProgress by animateFloatAsState(
                            targetValue = fileProgress,
                            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                        )

                        LinearProgressIndicator(
                            progress = animatedFileProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF4CAF50) // Green color for file progress
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // Total bytes progress
                if (aggregatedStats.totalBytes > 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Progress:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            // Format bytes in human-readable form
                            val transferredBytes = formatBytes(aggregatedStats.totalBytesTransferred)
                            val totalBytes = formatBytes(aggregatedStats.totalBytes)

                            Text(
                                text = "$transferredBytes / $totalBytes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Progress bar for total bytes
                        val bytesProgress = if (aggregatedStats.totalBytes > 0) {
                            (aggregatedStats.totalBytesTransferred.toFloat() / aggregatedStats.totalBytes.toFloat()).coerceIn(
                                0f,
                                1f
                            )
                        } else 0f

                        val animatedBytesProgress by animateFloatAsState(
                            targetValue = bytesProgress,
                            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                        )

                        LinearProgressIndicator(
                            progress = animatedBytesProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF2196F3) // Blue color for total progress
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // Source and destination folders
                if (sourcePath.isNotEmpty() && destPath.isNotEmpty() && !isComplete) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "From:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = sourcePath,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "To:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = destPath,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Show Done button only when processing is complete
//        println("AggregatedProgressScreen - Should show done button: $effectivelyComplete")
        if (effectivelyComplete) {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .padding(top=16.dp)
                    .fillMaxWidth(0.5f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.button,
                    fontWeight = FontWeight.Bold
                )
            }
        }


    }
}
