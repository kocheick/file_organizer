package com.shevapro.filesorter.data.database

import androidx.room.*
import com.shevapro.filesorter.TaskRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * from task_records")
    fun getAll(): Flow<List<TaskRecord>>

    @Query("SELECT * from task_records where id = :id")
    fun getById(id: Int): TaskRecord?

    @Insert
    suspend fun insert(item: TaskRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: TaskRecord)

    @Delete
    suspend fun delete(item: TaskRecord)

    @Query("DELETE FROM task_records")
    suspend fun deleteAllTodos()


}