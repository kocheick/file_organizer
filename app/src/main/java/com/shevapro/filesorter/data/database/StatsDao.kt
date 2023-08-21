package com.shevapro.filesorter.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shevapro.filesorter.model.MoveStat
import com.shevapro.filesorter.model.TaskRecord
import kotlinx.coroutines.flow.Flow
@Dao
interface StatsDao {
    @Query("SELECT * from move_infos")
    fun getAll(): Flow<List<MoveStat>>
//
//    @Query("SELECT * from task_records where id = :id")
//    fun getById(id: Int): TaskRecord?
//
    @Insert
    suspend fun insert(item: MoveStat)
//
//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun update(item: MoveStat)
//
//    @Delete
//    suspend fun delete(item: TaskRecord)

    @Query("DELETE FROM move_infos")
    suspend fun deleteAllTodos()

}