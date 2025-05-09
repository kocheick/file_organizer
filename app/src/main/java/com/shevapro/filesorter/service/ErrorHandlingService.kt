package com.shevapro.filesorter.service

import android.content.Context
import android.net.Uri
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.model.AppExceptions
import java.io.IOException

/**
 * Service responsible for centralized error handling and recovery for file operations.
 */
class ErrorHandlingService(private val context: Context) {

    /**
     * Handles file operation errors and provides appropriate recovery actions.
     *
     * @param exception The exception that occurred
     * @param sourceUri Optional source URI related to the error
     * @param destinationUri Optional destination URI related to the error
     * @return An AppException with user-friendly error message and recovery action
     */
    fun handleFileOperationError(
        exception: Exception,
        sourceUri: Uri? = null,
        destinationUri: Uri? = null
    ): AppExceptions {
        return when (exception) {
            is IOException -> handleIOException(exception, sourceUri, destinationUri)
            is SecurityException -> handleSecurityException(exception, sourceUri, destinationUri)
            is OutOfMemoryError -> AppExceptions.OutOfMemoryException(
                "The operation requires more memory than is available. Try with fewer files."
            )
            else -> AppExceptions.UnknownError(
                exception.message ?: "An unknown error occurred during the file operation."
            )
        }
    }

    /**
     * Handles IO exceptions specifically.
     *
     * @param exception The IOException that occurred
     * @param sourceUri Optional source URI related to the error
     * @param destinationUri Optional destination URI related to the error
     * @return An AppException with user-friendly error message and recovery action
     */
    private fun handleIOException(
        exception: IOException,
        sourceUri: Uri?,
        destinationUri: Uri?
    ): AppExceptions {
        val message = exception.message ?: "An I/O error occurred during the file operation."

        return when {
            message.contains("does not exist") -> {
                AppExceptions.FileNotFoundException(
                    "The file or directory could not be found. Please check if it exists."
                )
            }
            message.contains("Cannot read") -> {
                AppExceptions.PermissionException(
                    "Cannot read from source. Please grant read permissions.",
                    sourceUri
                )
            }
            message.contains("Cannot write") -> {
                AppExceptions.PermissionException(
                    "Cannot write to destination. Please grant write permissions.",
                    destinationUri
                )
            }
            message.contains("Not enough space") -> {
                AppExceptions.InsufficientSpaceException(
                    "Not enough space in the destination. Please free up some space or choose a different destination."
                )
            }
            else -> AppExceptions.IOException(message)
        }
    }

    /**
     * Handles security exceptions specifically.
     *
     * @param exception The SecurityException that occurred
     * @param sourceUri Optional source URI related to the error
     * @param destinationUri Optional destination URI related to the error
     * @return An AppException with user-friendly error message and recovery action
     */
    private fun handleSecurityException(
        exception: SecurityException,
        sourceUri: Uri?,
        destinationUri: Uri?
    ): AppExceptions {
        val message = exception.message ?: "A security error occurred during the file operation."

        return when {
            message.contains("permission") -> {
                val uri = if (message.contains("read")) sourceUri else destinationUri
                AppExceptions.PermissionException(
                    "Permission denied. Please grant the necessary permissions.",
                    uri
                )
            }
            else -> AppExceptions.SecurityException(message)
        }
    }

    /**
     * Attempts to recover from a permission error by requesting permissions.
     *
     * @param uri The URI for which permissions are needed
     * @return True if recovery was successful, false otherwise
     */
    fun recoverFromPermissionError(uri: Uri?): Boolean {
        if (uri == null) return false

        try {
            // Request permissions for the URI
            Utility.grantPermissionForUri(context, uri)
            return true
        } catch (e: com.shevapro.filesorter.model.PermissionExceptionForUri) {
            // Log the specific permission exception
            android.util.Log.e("ErrorHandlingService", "Permission error for URI: ${e.uri}", e)
            return false
        } catch (e: Exception) {
            // Log any other exception
            android.util.Log.e("ErrorHandlingService", "Error recovering from permission error", e)
            return false
        }
    }

    /**
     * Logs an error for analytics or debugging purposes.
     *
     * @param exception The exception to log
     * @param operation The operation that was being performed
     * @param sourceUri Optional source URI related to the error
     * @param destinationUri Optional destination URI related to the error
     */
    fun logError(
        exception: Exception,
        operation: String,
        sourceUri: Uri? = null,
        destinationUri: Uri? = null
    ) {
        // In a real app, this would log to a logging service
        // For now, we'll just print to console
        println("ERROR in $operation: ${exception.message}")
        println("Source: $sourceUri")
        println("Destination: $destinationUri")
        println("Stack trace: ${exception.stackTraceToString()}")
    }

    /**
     * Provides a user-friendly error message for a given exception.
     *
     * @param exception The exception to get a message for
     * @return A user-friendly error message
     */
    fun getUserFriendlyErrorMessage(exception: Exception): String {
        return when (exception) {
            is IOException -> {
                when {
                    exception.message?.contains("does not exist") == true ->
                        "The file or directory could not be found. Please check if it exists."
                    exception.message?.contains("Cannot read") == true ->
                        "Cannot read from source. Please grant read permissions."
                    exception.message?.contains("Cannot write") == true ->
                        "Cannot write to destination. Please grant write permissions."
                    exception.message?.contains("Not enough space") == true ->
                        "Not enough space in the destination. Please free up some space or choose a different destination."
                    else -> "An error occurred while accessing the file. Please try again."
                }
            }
            is SecurityException -> "Permission denied. Please grant the necessary permissions."
            is OutOfMemoryError -> "The operation requires more memory than is available. Try with fewer files."
            else -> "An unexpected error occurred. Please try again."
        }
    }
}
