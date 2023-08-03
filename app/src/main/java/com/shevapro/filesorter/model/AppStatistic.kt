package com.shevapro.filesorter.model

data class AppStatistic(
    val filesMoved:Int,
    val MostMovedFileByType:String,
    val frequency : Frequency,
    val mostUsed: MostUsed,
    val timeSavedInMinutes : Int)

data class MostUsed(
    val topSourceFolder:String,
    val topDestinationFolder:String
)

data class Frequency(val weekly:Int, val monthly:Int) {

}
