package com.example.fileorganizer.data.repository

import androidx.lifecycle.LiveData
import com.example.fileorganizer.TaskRecord
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