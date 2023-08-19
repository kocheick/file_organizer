package com.shevapro.filesorter.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class AppStatistic(
    val numberOfFileMoved: Int = 0,
    val MostMovedFileByType: String = "",
    val frequency: Frequency = Frequency(),
    val mostUsed: MostUsed = MostUsed(),
    val timeSavedInMinutes: Int = 0
)

data class MostUsed(
    val topSourceFolder: String = "",
    val topDestinationFolder: String = ""
)

data class Frequency(val weekly: Int = 0, val monthly: Int = 0) {

}


data class TaskStats(
    val totalFiles: Int = 0,
    val numberOfFilesMoved: Int = 0,
    val currentFileName: String
)


@Entity(tableName = "app_stats")
data class UsageRecords(
    val numberOfFileMoved: Int = 0,
    val MostMovedFileByType: String = "",
    val timeSavedInMinutes: Int = 0,
    @PrimaryKey(autoGenerate = true)
    val id: Int
)