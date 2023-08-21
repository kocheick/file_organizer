package com.shevapro.filesorter.service

import com.shevapro.filesorter.data.database.StatsDao
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.model.Frequency
import com.shevapro.filesorter.model.MostUsed
import com.shevapro.filesorter.model.MoveStat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

class StatsService(private val appStats: StatsDao) {
    suspend fun insertMoveInfo(source: String, destination: String, extension: String, itemQuantity: Int) {
            val item = MoveStat(source,destination,extension,itemQuantity)
        appStats.insert(item)
    }

     fun getLatestStats() = appStats.getAll()




}