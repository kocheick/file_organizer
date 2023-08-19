package com.shevapro.filesorter.data.repository

import com.shevapro.filesorter.model.TaskRecord
import kotlinx.coroutines.flow.Flow

interface Repository {

    suspend fun addTask(taskRecord: TaskRecord)
    suspend fun updateTask(taskRecord: TaskRecord)
    suspend fun deleteTask(taskRecord: TaskRecord)
    fun getTask(taskRecord: TaskRecord): TaskRecord?
    suspend fun getTaskbyId(taskID: Int): TaskRecord?
    suspend fun getTasks(): Flow<List<TaskRecord>>
    suspend fun deleteAll()
}