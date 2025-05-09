package com.shevapro.filesorter.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class UiState {
    object Loading : UiState()
    data class Processing(val stats : TaskStats) : UiState()
    data class ProcessingComplete(val stats : TaskStats) : UiState()
    data class Data(val records: List<UITaskRecord> ,val exception: AppExceptions? = null) : UiState()
//    data class Error(val messagesssage:String) : MainState()
}

data class AppState(val state: UiState = UiState.Loading)



sealed class Outcome<out T : Any> {
    data class Success<out T : Any>(val value: T) : Outcome<T>()
    data class Error(val message: String, val cause: Exception? = null) : Outcome<Nothing>()
}
