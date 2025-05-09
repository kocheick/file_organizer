package com.shevapro.filesorter.service

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.ErrorEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.IOException

/**
 * Global error handler for the application.
 * This class centralizes error handling and provides a standardized way to report and handle errors.
 */
class GlobalErrorHandler(private val context: Context) {

    // Flow of error events that can be collected by UI components
    private val _errorEvents = MutableSharedFlow<ErrorEvent>()
    val errorEvents: SharedFlow<ErrorEvent> = _errorEvents.asSharedFlow()

    /**
     * Handles an exception and converts it to an ErrorEvent.
     *
     * @param exception The exception to handle
     * @param sourceUri Optional source URI related to the error
     * @param destinationUri Optional destination URI related to the error
     * @param operation Optional name of the operation that caused the error
     * @return An ErrorEvent representing the error
     */
    suspend fun handleException(
        exception: Exception,
        sourceUri: Uri? = null,
        destinationUri: Uri? = null,
        operation: String? = null
    ): ErrorEvent {
        val errorEvent = convertExceptionToErrorEvent(exception, sourceUri, destinationUri)

        // Log the error
        logError(exception, operation, sourceUri, destinationUri)

        // Emit the error event
        _errorEvents.emit(errorEvent)

        return errorEvent
    }

    /**
     * Converts an exception to an ErrorEvent.
     *
     * @param exception The exception to convert
     * @param sourceUri Optional source URI related to the error
     * @param destinationUri Optional destination URI related to the error
     * @return An ErrorEvent representing the error
     */
    private fun convertExceptionToErrorEvent(
        exception: Exception,
        sourceUri: Uri? = null,
        destinationUri: Uri? = null
    ): ErrorEvent {
        return when (exception) {
            is IOException -> handleIOException(exception, sourceUri, destinationUri)
            is SecurityException -> handleSecurityException(exception, sourceUri, destinationUri)
            is OutOfMemoryError -> ErrorEvent.FileOperation(
                message = "The operation requires more memory than is available. Try with fewer files.",
                isRecoverable = false,
                sourceUri = sourceUri,
                destinationUri = destinationUri,
                exception = exception
            )
            else -> ErrorEvent.Unknown(
                message = exception.message ?: "An unknown error occurred during the file operation.",
                isRecoverable = false,
                exception = exception
            )
        }
    }

    /**
     * Handles IO exceptions specifically.
     *
     * @param exception The IOException that occurred
     * @param sourceUri Optional source URI related to the error
     * @param destinationUri Optional destination URI related to the error
     * @return An ErrorEvent representing the error
     */
    private fun handleIOException(
        exception: IOException,
        sourceUri: Uri?,
        destinationUri: Uri?
    ): ErrorEvent {
        val message = exception.message ?: "An I/O error occurred during the file operation."

        return when {
            message.contains("does not exist") -> {
                ErrorEvent.FileOperation(
                    message = "The file or directory could not be found. Please check if it exists.",
                    isRecoverable = false,
                    sourceUri = sourceUri,
                    destinationUri = destinationUri,
                    exception = exception
                )
            }
            message.contains("Cannot read") -> {
                sourceUri?.let {
                    ErrorEvent.Permission(
                        message = "Cannot read from source. Please grant read permissions.",
                        isRecoverable = true,
                        uri = it,
                        exception = exception
                    )
                } ?: ErrorEvent.FileOperation(
                    message = "Cannot read from source. Please grant read permissions.",
                    isRecoverable = false,
                    sourceUri = sourceUri,
                    destinationUri = destinationUri,
                    exception = exception
                )
            }
            message.contains("Cannot write") -> {
                destinationUri?.let {
                    ErrorEvent.Permission(
                        message = "Cannot write to destination. Please grant write permissions.",
                        isRecoverable = true,
                        uri = it,
                        exception = exception
                    )
                } ?: ErrorEvent.FileOperation(
                    message = "Cannot write to destination. Please grant write permissions.",
                    isRecoverable = false,
                    sourceUri = sourceUri,
                    destinationUri = destinationUri,
                    exception = exception
                )
            }
            message.contains("Not enough space") -> {
                ErrorEvent.FileOperation(
                    message = "Not enough space in the destination. Please free up some space or choose a different destination.",
                    isRecoverable = true,
                    sourceUri = sourceUri,
                    destinationUri = destinationUri,
                    exception = exception
                )
            }
            else -> ErrorEvent.FileOperation(
                message = message,
                isRecoverable = false,
                sourceUri = sourceUri,
                destinationUri = destinationUri,
                exception = exception
            )
        }
    }

    /**
     * Handles security exceptions specifically.
     *
     * @param exception The SecurityException that occurred
     * @param sourceUri Optional source URI related to the error
     * @param destinationUri Optional destination URI related to the error
     * @return An ErrorEvent representing the error
     */
    private fun handleSecurityException(
        exception: SecurityException,
        sourceUri: Uri?,
        destinationUri: Uri?
    ): ErrorEvent {
        val message = exception.message ?: "A security error occurred during the file operation."

        return when {
            message.contains("permission") -> {
                val uri = if (message.contains("read")) sourceUri else destinationUri
                uri?.let {
                    ErrorEvent.Permission(
                        message = "Permission denied. Please grant the necessary permissions.",
                        isRecoverable = true,
                        uri = it,
                        exception = exception
                    )
                } ?: ErrorEvent.FileOperation(
                    message = "Permission denied. Please grant the necessary permissions.",
                    isRecoverable = false,
                    sourceUri = sourceUri,
                    destinationUri = destinationUri,
                    exception = exception
                )
            }
            else -> ErrorEvent.FileOperation(
                message = message,
                isRecoverable = false,
                sourceUri = sourceUri,
                destinationUri = destinationUri,
                exception = exception
            )
        }
    }

    /**
     * Attempts to recover from an error.
     *
     * @param errorEvent The error event to recover from
     * @return True if recovery was successful, false otherwise
     */
    fun recoverFromError(errorEvent: ErrorEvent): Boolean {
        if (!errorEvent.isRecoverable) return false

        return when (errorEvent) {
            is ErrorEvent.Permission -> {
                try {
                    // Request permissions for the URI
                    Utility.grantPermissionForUri(context, errorEvent.uri)
                    true
                } catch (e: com.shevapro.filesorter.model.PermissionExceptionForUri) {
                    // Log the specific permission exception
                    android.util.Log.e("GlobalErrorHandler", "Permission error for URI: ${e.uri}", e)

                    // Show a toast message to the user (already shown in Utility.grantPermissionForUri)
                    // No need to show another toast here

                    false
                } catch (e: Exception) {
                    // Log any other exception
                    android.util.Log.e("GlobalErrorHandler", "Error recovering from permission error", e)
                    false
                }
            }
            else -> false
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
        operation: String? = null,
        sourceUri: Uri? = null,
        destinationUri: Uri? = null
    ) {
        // In a real app, this would log to a logging service
        // For now, we'll just print to console
        println("ERROR in ${operation ?: "unknown operation"}: ${exception.message}")
        println("Source: $sourceUri")
        println("Destination: $destinationUri")
        println("Stack trace: ${exception.stackTraceToString()}")
    }

    /**
     * Shows a toast message for an error.
     *
     * @param errorEvent The error event to show a message for
     */
    fun showErrorToast(errorEvent: ErrorEvent) {
        Toast.makeText(context, errorEvent.message, Toast.LENGTH_LONG).show()
    }
}
