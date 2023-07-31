package com.shevapro.filesorter.model

data class NoFileFoundException(val errorMessage: String) : Exception(errorMessage)
data class EmptyContentException(val errorMessage: String) : Exception(errorMessage)
data class MissingFieldException(val errorMessage: String) : Exception(errorMessage)