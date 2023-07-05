package com.example.fileorganizer.data.repository

import androidx.lifecycle.LiveData
import com.example.fileorganizer.TaskRecord
import com.example.fileorganizer.data.database.TaskDao
import kotlinx.coroutines.flow.Flow

class RepositoryImpl(private val taskDbDao: TaskDao) : Repository {

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

    override suspend fun deleteAll() {
        taskDbDao.deleteAllTodos()
    }
}