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
import com.shevapro.filesorter.data.repository.RuleRepository
import com.shevapro.filesorter.model.ConditionType
import com.shevapro.filesorter.model.LogicalOperator
import com.shevapro.filesorter.model.Rule
import com.shevapro.filesorter.model.RuleCondition
import com.shevapro.filesorter.model.ScheduleType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Service for executing scheduled rules in the background
 */
//class RuleExecutionService : Service() {
//    private val ruleRepository: RuleRepository by inject()
//    private val fileMover: FileMover by inject()
//    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//    private val TAG = "RuleExecutionService"
//
//    companion object {
//        private const val NOTIFICATION_ID = 1001
//        private const val CHANNEL_ID = "rule_execution_channel"
//
//        fun startService(context: Context) {
//            val intent = Intent(context, RuleExecutionService::class.java)
//            context.startService(intent)
//        }
//
//        fun stopService(context: Context) {
//            val intent = Intent(context, RuleExecutionService::class.java)
//            context.stopService(intent)
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d(TAG, "Service created")
//
//        // Create notification channel
//        NotificationHelper.createNotificationChannel(
//            this,
//            CHANNEL_ID,
//            "Rule Execution",
//            "Background service for executing scheduled rules"
//        )
//
//        // Start as a foreground service
//        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("File Organizer")
//            .setContentText("Monitoring for scheduled rules")
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .build()
//
//        startForeground(NOTIFICATION_ID, notification)
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "Service started")
//
//        // Start the rule execution loop
//        serviceScope.launch {
//            executeScheduledRules()
//        }
//
//        return START_STICKY
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(TAG, "Service destroyed")
//        serviceScope.cancel()
//    }
//
//    private suspend fun executeScheduledRules() {
//        while (true) {
//            try {
//                Log.d(TAG, "Checking for scheduled rules")
//
//                // Get all active scheduled rules
//                val scheduledRules = ruleRepository.getScheduledRules().first().filter { it.isActive }
//
//                val now = Date()
//
//                // Execute rules that are due
//                scheduledRules.forEach { rule ->
//                    rule.scheduleTime?.let { scheduleTime ->
//                        val shouldExecute = when (rule.scheduleType) {
//                            ScheduleType.ONCE -> {
//                                // For one-time schedules, execute if the time has passed
//                                scheduleTime.before(now)
//                            }
//
//                            ScheduleType.DAILY -> {
//                                // For daily schedules, compare only the time part (H:M)
//                                val scheduleCalendar = Calendar.getInstance()
//                                scheduleCalendar.time = scheduleTime
//
//                                val nowCalendar = Calendar.getInstance()
//
//                                // Set scheduleCalendar to today with the same time
//                                scheduleCalendar.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR))
//                                scheduleCalendar.set(Calendar.MONTH, nowCalendar.get(Calendar.MONTH))
//                                scheduleCalendar.set(Calendar.DAY_OF_MONTH, nowCalendar.get(Calendar.DAY_OF_MONTH))
//
//                                // Check if the time has passed today
//                                scheduleCalendar.before(nowCalendar)
//                            }
//                            ScheduleType.WEEKLY -> {
//                                // For weekly schedules, check if it's the right day of week and time has passed
//                                val scheduleCalendar = Calendar.getInstance()
//                                scheduleCalendar.time = scheduleTime
//
//                                val nowCalendar = Calendar.getInstance()
//
//                                // First check if it's the same day of the week
//                                val isSameDayOfWeek =
//                                    scheduleCalendar.get(Calendar.DAY_OF_WEEK) == nowCalendar.get(Calendar.DAY_OF_WEEK)
//
//                                if (isSameDayOfWeek) {
//                                    // If it's the right day, check if the scheduled time has passed
//                                    val scheduleTimeCalendar = Calendar.getInstance()
//                                    scheduleTimeCalendar.time = scheduleTime
//
//                                    // Set to today with the scheduled time
//                                    scheduleTimeCalendar.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR))
//                                    scheduleTimeCalendar.set(Calendar.MONTH, nowCalendar.get(Calendar.MONTH))
//                                    scheduleTimeCalendar.set(
//                                        Calendar.DAY_OF_MONTH,
//                                        nowCalendar.get(Calendar.DAY_OF_MONTH)
//                                    )
//
//                                    // Check if the time has passed today
//                                    scheduleTimeCalendar.before(nowCalendar)
//                                } else {
//                                    false // Not the right day of the week
//                                }
//                            }
//                        }
//
//                        if (shouldExecute) {
//                            Log.d(TAG, "Executing rule: ${rule.name}")
//                            executeRule(rule)
//
//                            // Update the next execution time
//                            val nextExecutionTime = calculateNextExecutionTime(scheduleTime, rule.scheduleType)
//                            val updatedRule = rule.copy(scheduleTime = nextExecutionTime)
//
//                            // For ONCE type, also set isScheduled to false after execution
//                            val finalRule = if (rule.scheduleType == ScheduleType.ONCE) {
//                                updatedRule.copy(isScheduled = false)
//                            } else {
//                                updatedRule
//                            }
//
//                            ruleRepository.updateRule(finalRule)
//                        }
//                    }
//                }
//
//                // Check every minute
//                delay(TimeUnit.MINUTES.toMillis(1))
//            } catch (e: Exception) {
//                Log.e(TAG, "Error executing scheduled rules", e)
//                delay(TimeUnit.MINUTES.toMillis(5))
//            }
//        }
//    }
//
//    private fun calculateNextExecutionTime(currentTime: Date, scheduleType: ScheduleType): Date {
//        val calendar = Calendar.getInstance()
//        calendar.time = currentTime
//
//        when (scheduleType) {
//            ScheduleType.ONCE -> {
//                // For one-time schedules, don't change the date
//            }
//            ScheduleType.DAILY -> {
//                // For daily schedules, set to tomorrow at the same time
//                calendar.add(Calendar.DAY_OF_MONTH, 1)
//
//                // Get tomorrow's date
//                val tomorrow = Calendar.getInstance().apply {
//                    add(Calendar.DAY_OF_MONTH, 1)
//                }
//
//                // Set to tomorrow but keep the time
//                calendar.set(Calendar.YEAR, tomorrow.get(Calendar.YEAR))
//                calendar.set(Calendar.MONTH, tomorrow.get(Calendar.MONTH))
//                calendar.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH))
//
//                calendar.set(Calendar.SECOND, 0) // Reset seconds
//            }
//            ScheduleType.WEEKLY -> {
//                // For weekly schedules, set to next week, same day and time
//                calendar.add(Calendar.DAY_OF_MONTH, 7)
//                calendar.set(Calendar.SECOND, 0) // Reset seconds
//            }
//        }
//
//        return calendar.time
//    }
//
//    private suspend fun executeRule(rule: Rule) {
//        try {
//            // We need a source directory to scan for files
//            // For simplicity, we'll use the Downloads directory
//            val sourceDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
//
//            // For each condition of type FILE_TYPE, move matching files
//            rule.conditions.filter { it.type == ConditionType.FILE_TYPE }
//                .forEach { condition ->
//                    try {
//                        // Move files of this type from source to destination
//                        fileMover.moveFilesByType(
//                            source = sourceDir, // Source directory
//                            destination = rule.destination, // Destination directory
//                            extension = condition.value, // File extension
//                            context = applicationContext
//                        )
//                        Log.d(TAG, "Moved files with extension ${condition.value} from $sourceDir to ${rule.destination}")
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Error moving files with extension ${condition.value}", e)
//                    }
//                }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error executing rule: ${rule.name}", e)
//        }
//    }
//
//    private fun matchesRule(file: File, rule: Rule): Boolean {
//        val conditions = rule.conditions
//        if (conditions.isEmpty()) return false
//
//        return when (rule.logicalOperator) {
//            LogicalOperator.AND -> conditions.all { matchesCondition(file, it) }
//            LogicalOperator.OR -> conditions.any { matchesCondition(file, it) }
//        }
//    }
//
//    private fun matchesCondition(file: File, condition: RuleCondition): Boolean {
//        return when (condition.type) {
//            ConditionType.FILE_TYPE -> matchesFileType(file, condition)
//            ConditionType.NAME_PATTERN -> matchesNamePattern(file, condition)
//            ConditionType.DATE -> matchesDate(file, condition)
//            ConditionType.SIZE -> matchesSize(file, condition)
//        }
//    }
//
//    private fun matchesFileType(file: File, condition: RuleCondition): Boolean {
//        val extension = file.extension.lowercase()
//
//        return when (condition.operator) {
//            "equals" -> extension == condition.value.lowercase()
//            "contains" -> extension.contains(condition.value.lowercase())
//            "starts_with" -> extension.startsWith(condition.value.lowercase())
//            "ends_with" -> extension.endsWith(condition.value.lowercase())
//            else -> false
//        }
//    }
//
//    private fun matchesNamePattern(file: File, condition: RuleCondition): Boolean {
//        val fileName = file.name.lowercase()
//
//        return when (condition.operator) {
//            "equals" -> fileName == condition.value.lowercase()
//            "contains" -> fileName.contains(condition.value.lowercase())
//            "starts_with" -> fileName.startsWith(condition.value.lowercase())
//            "ends_with" -> fileName.endsWith(condition.value.lowercase())
//            else -> false
//        }
//    }
//
//    private fun matchesDate(file: File, condition: RuleCondition): Boolean {
//        val fileDate = Date(file.lastModified())
//        val conditionDate = try {
//            Date(condition.value.toLong())
//        } catch (e: Exception) {
//            return false
//        }
//
//        return when (condition.operator) {
//            "equals" -> fileDate.time == conditionDate.time
//            "greater_than" -> fileDate.after(conditionDate)
//            "less_than" -> fileDate.before(conditionDate)
//            else -> false
//        }
//    }
//
//    private fun matchesSize(file: File, condition: RuleCondition): Boolean {
//        val fileSize = file.length()
//        val conditionSize = try {
//            condition.value.toLong()
//        } catch (e: Exception) {
//            return false
//        }
//
//        return when (condition.operator) {
//            "equals" -> fileSize == conditionSize
//            "greater_than" -> fileSize > conditionSize
//            "less_than" -> fileSize < conditionSize
//            else -> false
//        }
//    }
//}

/**
 * Helper class for creating notification channels
 */
