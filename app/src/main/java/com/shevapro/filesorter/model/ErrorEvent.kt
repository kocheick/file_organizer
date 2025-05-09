package com.shevapro.filesorter.model

import android.net.Uri

/**
 * Represents an error event in the application.
 * This class is used for standardized error reporting across the application.
 */
sealed class ErrorEvent {
    /**
     * The error message to display to the user.
     */
    abstract val message: String
    
    /**
     * Whether the error can be recovered from.
     */
    abstract val isRecoverable: Boolean
    
    /**
     * The source URI related to the error, if any.
     */
    open val sourceUri: Uri? = null
    
    /**
     * The destination URI related to the error, if any.
     */
    open val destinationUri: Uri? = null
    
    /**
     * The original exception that caused this error, if any.
     */
    open val exception: Exception? = null
    
    /**
     * File operation error.
     */
    data class FileOperation(
        override val message: String,
        override val isRecoverable: Boolean,
        override val sourceUri: Uri? = null,
        override val destinationUri: Uri? = null,
        override val exception: Exception? = null
    ) : ErrorEvent()
    
    /**
     * Permission error.
     */
    data class Permission(
        override val message: String,
        override val isRecoverable: Boolean = true,
        val uri: Uri,
        override val exception: Exception? = null
    ) : ErrorEvent() {
        override val sourceUri: Uri? = uri
    }
    
    /**
     * Network error.
     */
    data class Network(
        override val message: String,
        override val isRecoverable: Boolean = true,
        override val exception: Exception? = null
    ) : ErrorEvent()
    
    /**
     * Database error.
     */
    data class Database(
        override val message: String,
        override val isRecoverable: Boolean = false,
        override val exception: Exception? = null
    ) : ErrorEvent()
    
    /**
     * Validation error.
     */
    data class Validation(
        override val message: String,
        override val isRecoverable: Boolean = true,
        val field: String? = null,
        override val exception: Exception? = null
    ) : ErrorEvent()
    
    /**
     * Unknown error.
     */
    data class Unknown(
        override val message: String = "An unexpected error occurred. Please try again.",
        override val isRecoverable: Boolean = false,
        override val exception: Exception? = null
    ) : ErrorEvent()
}