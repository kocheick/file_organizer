package com.shevapro.filesorter.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.Date

/**
 * Represents the frequency of a scheduled task
 */
enum class ScheduleType {
    NEVER, // Indicates the task is not scheduled
    ONCE,
    DAILY,
    WEEKLY,
    // Add MONTHLY etc. later if needed
}

@Entity(tableName = "task_records")
data class TaskRecord(
    @ColumnInfo(name = "type_or_extension") val extension: String,
    @ColumnInfo(name = "source_uri") val source: String,
    @ColumnInfo(name = "destination_uri") val destination: String,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "is_scheduled") val isScheduled: Boolean = false,
    @ColumnInfo(name = "schedule_time") val scheduleTime: Date? = null,
    @ColumnInfo(name = "schedule_type") val scheduleType: ScheduleType = ScheduleType.NEVER,
    @PrimaryKey(autoGenerate = true)
    val id: Int
) {
    // Transient field, not stored in the database
    @androidx.room.Ignore
    val errorMessage: String? = null

    companion object {
        val EMPTY_ITEM = TaskRecord("", "", "", false, false, null, ScheduleType.NEVER, 0)
    }

    fun toUITaskRecord(): UITaskRecord {
        val fileType = extension.uppercase()
//        val sourceFolder = formatUriToUIString(source)
//        val destinationFolder = formatUriToUIString(destination)

        return UITaskRecord(
            extension = fileType, 
            source = Uri.decode(source), 
            destination = Uri.decode(destination), 
            isActive = isActive, 
            isScheduled = isScheduled,
            scheduleTime = scheduleTime,
            scheduleType = scheduleType,
            errorMessage = errorMessage, 
            id = id
        )
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

/**
 * Type converters for Room database
 */
class TaskConverters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun fromScheduleType(value: String?): ScheduleType? {
        return value?.let { ScheduleType.valueOf(it) }
    }

    @TypeConverter
    fun scheduleTypeToString(scheduleType: ScheduleType?): String? {
        return scheduleType?.name
    }
}
