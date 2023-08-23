package com.shevapro.filesorter.model

import android.net.Uri

data class NoFileFoundException(val errorMessage: String) : Exception(errorMessage)
data class EmptyContentException(val errorMessage: String) : Exception(errorMessage)
data class MissingFieldException(val errorMessage: String) : Exception(errorMessage)
data class PermissionExceptionForUri(val uri: Uri, val errorMessage: String) : Exception(errorMessage)
