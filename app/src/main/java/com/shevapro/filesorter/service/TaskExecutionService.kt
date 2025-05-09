package com.shevapro.filesorter.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shevapro.filesorter.R
import com.shevapro.filesorter.data.repository.Repository
import com.shevapro.filesorter.model.ScheduleType
import com.shevapro.filesorter.model.TaskRecord
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Service for executing scheduled tasks in the background
 */
class TaskExecutionService : Service() {
    private val taskRepository: Repository by inject()
    private val fileMover: FileMover by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "TaskExecutionService"

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "task_execution_channel"

        fun startService(context: Context) {
            val intent = Intent(context, TaskExecutionService::class.java)
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TaskExecutionService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Create notification channel
        NotificationHelper.createNotificationChannel(
            this,
            CHANNEL_ID,
            "Task Execution",
            "Background service for executing scheduled tasks"
        )

        // Start as a foreground service
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("File Organizer")
            .setContentText("Monitoring for scheduled tasks")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Start the task execution loop
        serviceScope.launch {
            executeScheduledTasks()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        serviceScope.cancel()
    }

    private suspend fun executeScheduledTasks() {
        while (true) {
            try {
                Log.d(TAG, "Checking for scheduled tasks")

                // Get all active scheduled tasks
                val scheduledTasks = taskRepository.getScheduledTasks().first().filter { it.isActive }

                val now = Date()

                // Execute tasks that are due
                scheduledTasks.forEach { task ->
                    task.scheduleTime?.let { scheduleTime ->
                        val shouldExecute = when (task.scheduleType) {
                            ScheduleType.ONCE -> {
                                // For one-time schedules, execute if the time has passed
                                scheduleTime.before(now)
                            }

                            ScheduleType.DAILY -> {
                                // For daily schedules, compare only the time part (H:M)
                                val scheduleCalendar = Calendar.getInstance()
                                scheduleCalendar.time = scheduleTime

                                val nowCalendar = Calendar.getInstance()

                                // Set scheduleCalendar to today with the same time
                                scheduleCalendar.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR))
                                scheduleCalendar.set(Calendar.MONTH, nowCalendar.get(Calendar.MONTH))
                                scheduleCalendar.set(Calendar.DAY_OF_MONTH, nowCalendar.get(Calendar.DAY_OF_MONTH))

                                // Check if the time has passed today
                                scheduleCalendar.before(nowCalendar)
                            }

                            ScheduleType.WEEKLY -> {
                                // For weekly schedules, check if it's the right day of week and time has passed
                                val scheduleCalendar = Calendar.getInstance()
                                scheduleCalendar.time = scheduleTime

                                val nowCalendar = Calendar.getInstance()

                                // First check if it's the same day of the week
                                val isSameDayOfWeek =
                                    scheduleCalendar.get(Calendar.DAY_OF_WEEK) == nowCalendar.get(Calendar.DAY_OF_WEEK)

                                if (isSameDayOfWeek) {
                                    // If it's the right day, check if the scheduled time has passed
                                    val scheduleTimeCalendar = Calendar.getInstance()
                                    scheduleTimeCalendar.time = scheduleTime

                                    // Set to today with the scheduled time
                                    scheduleTimeCalendar.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR))
                                    scheduleTimeCalendar.set(Calendar.MONTH, nowCalendar.get(Calendar.MONTH))
                                    scheduleTimeCalendar.set(
                                        Calendar.DAY_OF_MONTH,
                                        nowCalendar.get(Calendar.DAY_OF_MONTH)
                                    )

                                    // Check if the time has passed today
                                    scheduleTimeCalendar.before(nowCalendar)
                                } else {
                                    false // Not the right day of the week
                                }
                            }
                            ScheduleType.NEVER -> false // Task is not scheduled, so it should not execute
                        }

                        if (shouldExecute) {
                            Log.d(TAG, "Executing task: ${task.extension}")
                            executeTask(task)

                            // Update the next execution time
                            val nextExecutionTime = calculateNextExecutionTime(scheduleTime, task.scheduleType)
                            val updatedTask = task.copy(scheduleTime = nextExecutionTime)

                            // For ONCE type, also set isScheduled to false after execution
                            val finalTask = if (task.scheduleType == ScheduleType.ONCE) {
                                updatedTask.copy(isScheduled = false, isActive = false) // Also deactivate after one-time execution
                            } else {
                                updatedTask
                            }

                            taskRepository.updateTask(finalTask)
                        }
                    }
                }

                // Check every minute
                delay(TimeUnit.MINUTES.toMillis(1))
            } catch (e: Exception) {
                Log.e(TAG, "Error executing scheduled tasks", e)
                delay(TimeUnit.MINUTES.toMillis(5))
            }
        }
    }

    private fun calculateNextExecutionTime(currentTime: Date, scheduleType: ScheduleType): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentTime

        when (scheduleType) {
            ScheduleType.ONCE -> {
                // For one-time schedules, don't change the date
            }
            ScheduleType.DAILY -> {
                // For daily schedules, set to tomorrow at the same time
                calendar.add(Calendar.DAY_OF_MONTH, 1)

                // Get tomorrow's date
                val tomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }

                // Set to tomorrow but keep the time
                calendar.set(Calendar.YEAR, tomorrow.get(Calendar.YEAR))
                calendar.set(Calendar.MONTH, tomorrow.get(Calendar.MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH))

                calendar.set(Calendar.SECOND, 0) // Reset seconds
            }
            ScheduleType.WEEKLY -> {
                // For weekly schedules, set to next week, same day and time
                calendar.add(Calendar.DAY_OF_MONTH, 7)
                calendar.set(Calendar.SECOND, 0) // Reset seconds
            }
            ScheduleType.NEVER -> {
                // For NEVER, do not update the execution time
            }
        }

        return calendar.time
    }

    private suspend fun executeTask(task: TaskRecord) {
        try {
            // We need a source directory to scan for files
            val sourceDir = task.source
            val destinationDir = task.destination
            val extension = task.extension

            try {
                // Move files of this type from source to destination
                fileMover.moveFilesByType(
                    source = sourceDir, // Source directory
                    destination = destinationDir, // Destination directory
                    extension = extension, // File extension
                    context = applicationContext
                )
                Log.d(TAG, "Moved files with extension $extension from $sourceDir to $destinationDir")
            } catch (e: Exception) {
                Log.e(TAG, "Error moving files with extension $extension", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing task: ${task.extension}", e)
        }
    }
}
