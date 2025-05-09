package com.shevapro.filesorter.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.Utility.AVERAGE_MANUAL_FILE_MOVE_PER_SECOND
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.model.MostUsed
import com.shevapro.filesorter.service.FileMover
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * ViewModel responsible for statistics-related operations.
 */
class StatsViewModel(
    private val app: Application,
    private val fileMover: FileMover
) : AndroidViewModel(app) {

    private var _appStats: MutableStateFlow<AppStatistic> = MutableStateFlow(AppStatistic())
    val appStats get() = _appStats.asStateFlow()

    init {
        initStats()
    }

    /**
     * Initializes the statistics from the FileMover service.
     */
    private fun initStats() {
        viewModelScope.launch {
            fileMover.getStats().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(), mutableListOf()
            ).collect { list ->
                if (list.isNotEmpty()) {
                    val numberOfFilesMoved = list.sumOf { it.numberOfFileMoved }
                    val mostMovedFileByType: String =
                        list.groupBy { it.extension }.mapValues { it.value.size }
                            .maxBy { it.value }.key
                    val topSource: String = list.groupBy { it.source }.mapValues { it.value.size }
                        .maxBy { it.value }.key
                    val topDestination: String =
                        list.groupBy { it.target }.mapValues { it.value.size }
                            .maxBy { it.value }.key

                    val approxTimeSaved =
                        (numberOfFilesMoved * AVERAGE_MANUAL_FILE_MOVE_PER_SECOND).roundToInt()
                    val item = AppStatistic(
                        totalFilesMoved = numberOfFilesMoved,
                        mostUsed = MostUsed(
                            topSourceFolder = Utility.formatUriToUIString(
                                Uri.decode(topSource)
                            ),
                            topDestinationFolder = Utility.formatUriToUIString(Uri.decode(topDestination)),
                            topMovedFileByType = mostMovedFileByType
                        ),
                        timeSavedInMinutes = approxTimeSaved
                    )

                    _appStats.value = item
                }
            }
        }
    }

    /**
     * Resets the statistics.
     */
    fun resetStats() {
        viewModelScope.launch {
            fileMover.resetStats()
        }
    }

    /**
     * Gets the total number of files moved.
     *
     * @return The total number of files moved
     */
    fun getTotalFilesMoved(): Int {
        return _appStats.value.totalFilesMoved
    }

    /**
     * Gets the most used file type.
     *
     * @return The most used file type
     */
    fun getMostUsedFileType(): String {
        return _appStats.value.mostUsed.topMovedFileByType
    }

    /**
     * Gets the most used source directory.
     *
     * @return The most used source directory
     */
    fun getMostUsedSource(): String {
        return _appStats.value.mostUsed.topSourceFolder
    }

    /**
     * Gets the most used destination directory.
     *
     * @return The most used destination directory
     */
    fun getMostUsedDestination(): String {
        return _appStats.value.mostUsed.topDestinationFolder
    }

    /**
     * Gets the approximate time saved in minutes.
     *
     * @return The approximate time saved in minutes
     */
    fun getTimeSavedInMinutes(): Int {
        return _appStats.value.timeSavedInMinutes
    }
}
