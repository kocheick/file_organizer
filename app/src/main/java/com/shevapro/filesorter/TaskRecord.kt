package com.shevapro.filesorter

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.shevapro.filesorter.model.UITaskRecord

@Entity(tableName = "task_records")
data class TaskRecord(
    @ColumnInfo(name = "type_or_extension") val extension: String,
    @ColumnInfo(name = "source_uri") val source: String,
    @ColumnInfo(name = "destination_uri") val destination: String,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @PrimaryKey(autoGenerate = true)
    val id: Int
) {
    companion object {
        val EMPTY_ITEM = TaskRecord("", "", "", false, 0)
    }

    fun toUITaskRecord(): UITaskRecord {
        val fileType = extension.uppercase()
//        val sourceFolder = formatUriToUIString(source)
//        val destinationFolder = formatUriToUIString(destination)

        return UITaskRecord(fileType, Uri.decode(source), Uri.decode(destination), isActive, id)
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