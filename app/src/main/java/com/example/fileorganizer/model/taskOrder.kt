package com.example.fileorganizer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_list")
data class TaskOrder(
    @ColumnInfo(name = "task_type") val type: String,
    @ColumnInfo(name = "task_from")val from: String,
    @ColumnInfo(name = "task_to")val to: String,
    @PrimaryKey(autoGenerate = true)
                     var id: Int = 0)