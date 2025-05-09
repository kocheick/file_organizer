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
    // Calculate progress as a float between 0 and 1
    val progressValue = if (currentMoveStats.totalFiles > 0) {
        (currentMoveStats.numberOfFilesMoved.toFloat() / currentMoveStats.totalFiles.toFloat())
    } else 0f

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
                        text = "${currentMoveStats.numberOfFilesMoved}/${currentMoveStats.totalFiles}",
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

        // Add space before the Done button
        Spacer(Modifier.height(24.dp))

        // Show Done button only when processing is complete
        AnimatedVisibility(visible = isComplete) {
            Button(
                onClick = onDone,
                modifier = Modifier
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
