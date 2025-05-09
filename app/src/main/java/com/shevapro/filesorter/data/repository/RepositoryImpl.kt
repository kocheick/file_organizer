package com.shevapro.filesorter.data.repository

import com.shevapro.filesorter.data.database.TaskDao
import com.shevapro.filesorter.model.ConditionType
import com.shevapro.filesorter.model.LogicalOperator
import com.shevapro.filesorter.model.Rule
import com.shevapro.filesorter.model.TaskRecord

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RepositoryImpl(
    private val taskDbDao: TaskDao,
    private val ruleRepository: RuleRepository
) : Repository {

    override suspend fun addTask(taskRecord: TaskRecord) {
        taskDbDao.insert(taskRecord)
    }

    override suspend fun getTaskbyId(taskID: Int): TaskRecord? {
        return taskDbDao.getById(taskID)
    }


    override suspend fun updateTask(taskRecord: TaskRecord) {
        taskDbDao.update(taskRecord)
    }


    override suspend fun deleteTask(taskRecord: TaskRecord) {
        taskDbDao.delete(taskRecord)
    }


    override fun getTask(taskRecord: TaskRecord): TaskRecord? {

        return taskDbDao.getById(taskRecord.id)
    }


    override suspend fun getTasks(): Flow<List<TaskRecord>> = taskDbDao.getAll()

    override suspend fun getScheduledTasks(): Flow<List<TaskRecord>> = taskDbDao.getScheduledTasks()

    override suspend fun getActiveTasks(): Flow<List<TaskRecord>> = taskDbDao.getActiveTasks()

    override suspend fun deleteAll() {
        taskDbDao.deleteAllTodos()
    }

    /**
     * Creates preset tasks for common file types being moved from Downloads folder
     */
    override suspend fun createPresetTasks(downloadsPath: String) {
        // Check if any tasks already exist to avoid duplication
        val existingTasks = taskDbDao.getAll().first()

        // Only create presets if no tasks exist yet
        if (existingTasks.isEmpty()) {
            // Ensure preset rules exist first
            ruleRepository.createPresetRules()

            // Get the preset rules
            val presetRules = ruleRepository.getPresetRules().first()
            println("Preset rules: ${presetRules.size}")

            // Create tasks based on preset rules
            presetRules.forEach { rule ->
                // For each rule, create a task with the same conditions type
                val extensions = rule.conditions.filter { it.type == ConditionType.FILE_TYPE }
                    .map { it.value }

                // Create a task for each category of files
                if (extensions.isNotEmpty()) {
                    // Use the first extension as the task extension identifier
                    // The actual filtering by extension will happen in the file operation service
                    val taskRecord = TaskRecord(
                        extension = extensions.first(),
                        source = downloadsPath,
                        destination = if (rule.destination.startsWith("/")) {
                            "file://${rule.destination}"
                        } else {
                            "file:///sdcard/${rule.destination}"
                        },
                        isActive = false,
                        id = 0
                    )
                    taskDbDao.insert(taskRecord)
                }
            }
        }
    }
}
