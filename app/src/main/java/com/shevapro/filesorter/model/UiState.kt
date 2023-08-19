package com.shevapro.filesorter.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class UiState {
    object Loading : UiState()
    data class Processing(val stats : TaskStats) : UiState()
    data class Data(val records: List<UITaskRecord>, val exception: Throwable? = null) : UiState()
//    data class Error(val messagesssage:String) : MainState()
}

data class AppState(val state: UiState = UiState.Loading)
