package com.example.fileorganizer.data.repository

import androidx.lifecycle.LiveData
import com.example.fileorganizer.TaskOrder

interface Repository {

  suspend  fun addTask(taskOrder: TaskOrder)
    suspend fun updateTask(taskOrder: TaskOrder)
   suspend fun deleteTask(taskOrder: TaskOrder)
    fun getTask(taskOrder: TaskOrder) : TaskOrder?
    suspend fun getTaskbyId(taskID: Int) : TaskOrder?
  suspend  fun getTasks(): LiveData<List<TaskOrder>>
  suspend fun deleteAll()
}