package com.shevapro.filesorter.data.database

import androidx.room.*
import com.shevapro.filesorter.model.TaskRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * from task_records")
    fun getAll(): Flow<List<TaskRecord>>

    @Query("SELECT * from task_records where id = :id")
    fun getById(id: Int): TaskRecord?

    @Query("SELECT * from task_records WHERE is_scheduled = 1")
    fun getScheduledTasks(): Flow<List<TaskRecord>>

    @Query("SELECT * from task_records WHERE is_active = 1")
    fun getActiveTasks(): Flow<List<TaskRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TaskRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: TaskRecord)

    @Delete
    suspend fun delete(item: TaskRecord)

    @Query("DELETE FROM task_records")
    suspend fun deleteAllTodos()


}
