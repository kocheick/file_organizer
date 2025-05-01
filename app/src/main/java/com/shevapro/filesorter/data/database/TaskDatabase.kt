package com.shevapro.filesorter.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shevapro.filesorter.model.MoveStat
import com.shevapro.filesorter.model.Rule
import com.shevapro.filesorter.model.RuleConverters
import com.shevapro.filesorter.model.TaskRecord

@Database(entities = [TaskRecord::class, MoveStat::class, Rule::class], version = 7, exportSchema = false)
@TypeConverters(RuleConverters::class)

abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun appStats(): StatsDao
    abstract fun ruleDao(): RuleDao

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
