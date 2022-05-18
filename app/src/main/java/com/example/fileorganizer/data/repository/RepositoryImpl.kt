package com.example.fileorganizer.data.repository

import androidx.lifecycle.LiveData
import com.example.fileorganizer.TaskOrder
import com.example.fileorganizer.data.database.TaskDao

class RepositoryImpl(private val taskDbDao: TaskDao) : Repository {

    override suspend fun addTask(taskOrder: TaskOrder) {

        taskDbDao.insert(taskOrder)

    }


    override suspend fun updateTask(taskOrder: TaskOrder) {
        taskDbDao.update(taskOrder)
    }


    override suspend fun deleteTask(taskOrder: TaskOrder) {
        taskDbDao.delete(taskOrder)
    }


    override fun getTask(taskOrder: TaskOrder): TaskOrder? {
        return taskDbDao.getById(taskOrder.id)
    }


    override suspend fun getTasks(): LiveData<List<TaskOrder>> = taskDbDao.getAll()

    override suspend fun deleteAll() {
        taskDbDao.deleteAllTodos()
    }
}