package com.example.fileorganizer.model

sealed class MainState {
    object Loading : MainState()
    data class Success(val records:List<UITaskRecord>) : MainState()
    data class Error(val message:String) : MainState()
}
