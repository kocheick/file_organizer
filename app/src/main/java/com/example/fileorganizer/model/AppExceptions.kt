package com.example.fileorganizer.model

data class NoFileFoundException(val errorMessage: String) : Exception(errorMessage)
data class EmptyContentException(val errorMessage: String) : Exception(errorMessage)
