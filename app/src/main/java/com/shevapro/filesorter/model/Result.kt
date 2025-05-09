package com.shevapro.filesorter.model

/**
 * A generic class that holds a value or an error.
 * This class is used for error propagation through layers.
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with a value.
     */
    data class Success<out T>(val data: T) : Result<T>()
    
    /**
     * Represents a failed operation with an error.
     */
    data class Error(val error: ErrorEvent) : Result<Nothing>()
    
    /**
     * Returns true if this is a Success, false otherwise.
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Returns true if this is an Error, false otherwise.
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Returns the encapsulated value if this is a Success or null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * Returns the encapsulated error if this is an Error or null otherwise.
     */
    fun errorOrNull(): ErrorEvent? = when (this) {
        is Success -> null
        is Error -> error
    }
    
    /**
     * Applies the given transform function to the encapsulated value if this is a Success.
     * Returns a new Result with the transformed value or the original error.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(error)
        }
    }
    
    /**
     * Applies the given transform function to the encapsulated value if this is a Success.
     * Returns the result of the transform function or the original error.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> {
        return when (this) {
            is Success -> transform(data)
            is Error -> Error(error)
        }
    }
    
    /**
     * Performs the given action on the encapsulated value if this is a Success.
     * Returns the original Result unchanged.
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Performs the given action on the encapsulated error if this is an Error.
     * Returns the original Result unchanged.
     */
    inline fun onError(action: (ErrorEvent) -> Unit): Result<T> {
        if (this is Error) action(error)
        return this
    }
    
    companion object {
        /**
         * Creates a Success result with the given value.
         */
        fun <T> success(data: T): Result<T> = Success(data)
        
        /**
         * Creates an Error result with the given error.
         */
        fun <T> error(error: ErrorEvent): Result<T> = Error(error)
        
        /**
         * Creates an Error result with the given error message.
         */
        fun <T> error(message: String): Result<T> = Error(
            ErrorEvent.Unknown(message = message)
        )
        
        /**
         * Wraps a function call in a Result, catching any exceptions and converting them to Error results.
         */
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(
                    ErrorEvent.Unknown(
                        message = e.message ?: "An unexpected error occurred.",
                        exception = e
                    )
                )
            }
        }
    }
}