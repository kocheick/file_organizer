package com.shevapro.filesorter.model

sealed class ProcessingResult<out T>{
    data class Success<out R>(val value : R): ProcessingResult<R>()
    data class Failure(val message:String, val throwable: Throwable?): ProcessingResult<Nothing>()

}
