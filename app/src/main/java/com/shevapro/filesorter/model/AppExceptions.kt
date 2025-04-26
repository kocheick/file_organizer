package com.shevapro.filesorter.model

import android.net.Uri

data class NoFileFoundException(val errorMessage: String) : Exception(errorMessage)
data class EmptyContentException(val errorMessage: String) : Exception(errorMessage)
data class MissingFieldException(val errorMessage: String) : Exception(errorMessage)
data class PermissionExceptionForUri(val uri: Uri, val errorMessage: String) : Exception(errorMessage)

sealed class AppExceptions {
    data class MissingFieldException(val message: String) : AppExceptions()
    data class NoFileFoundException(val message: String) : AppExceptions()
    data class EmptyContentException(val message: String) : AppExceptions()
    data class PermissionExceptionForUri(val uri: Uri?, val errorMessage: String): AppExceptions()
    data class UnknownError(val message: String): AppExceptions()
    data class PermissionException(val message: String, val uri: Uri? = null): AppExceptions()
    data class IOException(val message: String): AppExceptions()
    data class SecurityException(val message: String): AppExceptions()
    data class InsufficientSpaceException(val message: String): AppExceptions()
    data class FileNotFoundException(val message: String): AppExceptions()
    data class OutOfMemoryException(val message: String): AppExceptions()
}
