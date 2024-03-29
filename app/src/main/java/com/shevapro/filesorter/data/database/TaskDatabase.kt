package com.shevapro.filesorter.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shevapro.filesorter.model.MoveStat
import com.shevapro.filesorter.model.TaskRecord

@Database(entities = [TaskRecord::class,MoveStat::class], version = 3, exportSchema = false)
//@TypeConverters(UriConverter::class)

abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun appStats(): StatsDao

    companion object {
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        TaskDatabase::class.java,
                        "task-database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}