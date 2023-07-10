package com.example.fileorganizer.model

import androidx.compose.runtime.Immutable

@Immutable
sealed  class MainState {
    object Loading : MainState()
    data class Data(val records:List<UITaskRecord>, val exception:Throwable?= null) : MainState()
//    data class Error(val messagesssage:String) : MainState()
}

data class AppState(val state: MainState = MainState.Loading)
