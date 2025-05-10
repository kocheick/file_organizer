package com.shevapro.filesorter.model

import androidx.compose.runtime.Immutable

@Immutable
data class UiState(
    // State type flags
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val isProcessingComplete: Boolean = false,
    val isProcessingMultipleTasks: Boolean = false,
    val isProcessingMultipleTasksComplete: Boolean = false,
    val isData: Boolean = false,

    // Data for each state type
    val records: List<UITaskRecord> = emptyList(),
    val exception: AppExceptions? = null,
    val stats: TaskStats? = null,
    val aggregatedStats: AggregatedTaskStats? = null
) {
    companion object {
        // Factory methods for creating different state types
        fun loading() = UiState(isLoading = true)

        fun processing(stats: TaskStats) = UiState(
            isProcessing = true,
            stats = stats
        )

        fun processingComplete(stats: TaskStats) = UiState(
            isProcessingComplete = true,
            stats = stats
        )

        fun processingMultipleTasks(aggregatedStats: AggregatedTaskStats) = UiState(
            isProcessingMultipleTasks = true,
            aggregatedStats = aggregatedStats
        )

        fun processingMultipleTasksComplete(aggregatedStats: AggregatedTaskStats) = UiState(
            isProcessingMultipleTasksComplete = true,
            aggregatedStats = aggregatedStats
        )

        fun data(records: List<UITaskRecord>, exception: AppExceptions? = null) = UiState(
            isData = true,
            records = records,
            exception = exception
        )
    }
}

data class AppState(val state: UiState = UiState.loading())

sealed class Outcome<out T : Any> {
    data class Success<out T : Any>(val value: T) : Outcome<T>()
    data class Error(val message: String, val cause: Exception? = null) : Outcome<Nothing>()
}
