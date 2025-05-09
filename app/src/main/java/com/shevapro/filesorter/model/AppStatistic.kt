package com.shevapro.filesorter.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.Date

data class AppStatistic(
    val totalFilesMoved: Int = 0,
    val mostUsed: MostUsed = MostUsed(),
    val frequency: Frequency = Frequency(),
    val timeSavedInMinutes: Int = 0
)

data class MostUsed(
    val topSourceFolder: String = "",
    val topDestinationFolder: String = "",
    val topMovedFileByType: String = ""
)

data class Frequency(val weekly: Int = 0, val monthly: Int = 0) {

}


data class TaskStats(
    val totalFiles: Int = 0,
    val numberOfFilesMoved: Int = 0,
    val currentFileName: String,
    val sourceFolder: String = "",
    val destinationFolder: String = "",
    val fileExtension: String = "",
    val fileType: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val currentFileSize: Long = 0,
    val currentBytesTransferred: Long = 0,
    val totalBytesTransferred: Long = 0,
    val totalBytes: Long = 0
)


@Entity(tableName = "app_stats")
data class AppStatisticRecord(
    val totalFilesMoved: Int = 0,
    val timeSavedInMinutes: Int = 0,
    @PrimaryKey(autoGenerate = true)
    val id: Int
)
@Entity(tableName = "move_infos")
data class MoveStat(
val source:String, val target:String,val extension:String,val numberOfFileMoved: Int = 0,
                    val timestamp: Long = System.currentTimeMillis(),    @PrimaryKey(autoGenerate = true )val id:Long =0)
