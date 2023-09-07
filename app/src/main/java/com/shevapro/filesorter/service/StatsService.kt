package com.shevapro.filesorter.service

import com.shevapro.filesorter.data.database.StatsDao
import com.shevapro.filesorter.model.MoveStat

class StatsService(private val appStats: StatsDao) {
    suspend fun logTaskDetails(source: String, destination: String, extension: String, itemQuantity: Int) {
            val item = MoveStat(source,destination,extension,itemQuantity)
        appStats.insert(item)
    }

     fun getLatestStats() = appStats.getAll()

    suspend fun resetStats() = appStats.deleteAll()




}