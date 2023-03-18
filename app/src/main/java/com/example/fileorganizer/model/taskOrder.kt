package com.example.fileorganizer

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "task_list")
data class TaskOrder(
    @ColumnInfo(name = "task_extension") val extension: String,
    @ColumnInfo(name = "task_source") val source: String,
    @ColumnInfo(name = "task_destination") val destination: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) {
    companion object {
        val EMPTY_ITEM = TaskOrder("", "", "")
    }
}

data class ScheduleTask(
    @ColumnInfo(name = "task_type") val type: String,
    @ColumnInfo(name = "task_from") val from: Uri,
    @ColumnInfo(name = "task_to") val to: Uri,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)

class UriConverter {
    @TypeConverter
    fun fromUriToString(value: Uri): String {
        return value.toString()
    }

    @TypeConverter
    fun fromStringToUri(value: String): Uri? {
        return Uri.parse(value)
    }
}