package com.example.fileorganizer.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.fileorganizer.TaskOrder
@Dao
interface TaskDao {

        @Query("SELECT * from task_list")
        fun getAll(): LiveData<List<TaskOrder>>

     @Query("SELECT * from task_list where id = :id")
        fun getById(id: Int) : TaskOrder?

        @Insert
        suspend fun insert(item:TaskOrder)

        @Update
        suspend fun update(item:TaskOrder)

        @Delete
        suspend fun delete(item:TaskOrder)

        @Query("DELETE FROM task_list")
        suspend fun deleteAllTodos()




}