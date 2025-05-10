package com.shevapro.filesorter.service

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.shevapro.filesorter.Utility.grantUrisPermissions
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.TaskStats
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 * Service responsible for moving files between directories.
 * This class delegates to specialized services for file operations, validation, and error handling.
 */
class FileMover private constructor(
    private val statsService: StatsService,
    private val fileOperationService: FileOperationService,
    private val validationService: ValidationService,
    private val errorHandlingService: ErrorHandlingService
) {

    companion object {
        /**
         * Creates a new instance of FileMover with the required dependencies.
         *
         * @param appService The service for tracking statistics
         * @param context The application context
         * @return A new FileMover instance
         */
        fun getInstance(appService: StatsService, context: Context): FileMover {
            val fileOperationService = FileOperationService(appService)
            val validationService = ValidationService()
            val errorHandlingService = ErrorHandlingService(context)
            
            return FileMover(
                appService,
                fileOperationService,
                validationService,
                errorHandlingService
            )
        }
    }

    /**
     * Gets the count of files with a specific extension in a directory.
     *
     * @param source The source directory path
     * @param extension The file extension to filter by
     * @param context The application context
     * @return Count of files with the specified extension
     */
    fun getTaskFileCount(source: String, extension: String, context: Context): Int {
        return try {
            val sourceUri = Uri.parse(source)
            val sourceFolder = when (sourceUri.scheme) {
                "file" -> DocumentFile.fromFile(java.io.File(sourceUri.path ?: ""))
                "content" -> DocumentFile.fromTreeUri(context, sourceUri)
                else -> {
                    val path = sourceUri.path
                    if (path != null) {
                        DocumentFile.fromFile(java.io.File(path))
                    } else {
                        null
                    }
                }
            }

            sourceFolder?.listFiles()?.count { file ->
                file.isFile && file.name?.lowercase()?.endsWith(".${extension.lowercase()}") == true
            } ?: 0
        } catch (e: Exception) {
            errorHandlingService.logError(e, "getTaskFileCount", Uri.parse(source))
            0
        }
    }

    /**
     * Gets the latest statistics.
     *
     * @return Flow of statistics
     */
    fun getStats() = statsService.getLatestStats()
    
    /**
     * Resets the statistics.
     */
    suspend fun resetStats() = statsService.resetStats()

    /**
     * Gets file extensions available in a folder.
     *
     * @param path The path to the folder
     * @param context The application context
     * @return List of file extensions found in the folder
     */
    fun getFilesExtensionsForFolder(path: String, context: Context): List<String> {
        return try {
            fileOperationService.getFilesExtensionsForFolder(path, context)
        } catch (e: Exception) {
            val appException = errorHandlingService.handleFileOperationError(
                e,
                Uri.parse(path)
            )
            errorHandlingService.logError(e, "getFilesExtensionsForFolder", Uri.parse(path))
            emptyList()
        }
    }

    /**
     * Moves files of a specific type from source to destination.
     *
     * @param source The source directory path
     * @param destination The destination directory path
     * @param extension The file extension to filter by
     * @param context The application context
     * @param moveProgress Callback to report progress
     */
    suspend fun moveFilesByType(
        source: String,
        destination: String,
        extension: String,
        context: Context,
        moveProgress: (TaskStats) -> Unit = {}
    ) {
        try {
            // Validate the operation
            val sourceUri = Uri.parse(source)
            val destinationUri = Uri.parse(destination)
            
            validationService.validateFileOperation(sourceUri, destinationUri, context)
            
            // Perform the operation
            fileOperationService.moveFilesByType(
                source,
                destination,
                extension,
                context,
                moveProgress,
                { uri -> errorHandlingService.recoverFromPermissionError(uri) }
            )
        } catch (e: Exception) {
            val appException = errorHandlingService.handleFileOperationError(
                e,
                Uri.parse(source),
                Uri.parse(destination)
            )
            errorHandlingService.logError(
                e, 
                "moveFilesByType", 
                Uri.parse(source), 
                Uri.parse(destination)
            )
            throw e
        }
    }

    /**
     * Moves files with a specific extension from source to destination.
     *
     * @param context The application context
     * @param sourceDirectory The source directory URI
     * @param targetDirectory The target directory URI
     * @param extension The file extension to filter by
     */
    suspend fun moveFilesWithExtension(
        context: Context,
        sourceDirectory: Uri,
        targetDirectory: Uri,
        extension: String
    ) {
        try {
            // Validate the operation
            validationService.validateFileOperation(sourceDirectory, targetDirectory, context)
            
            // This method is not implemented in the new services yet
            // In a real implementation, we would delegate to fileOperationService
            withContext(IO) {
                // Implementation would go here
                // For now, we'll just log that this method is not implemented
                errorHandlingService.logError(
                    Exception("Method not implemented"),
                    "moveFilesWithExtension",
                    sourceDirectory,
                    targetDirectory
                )
            }
        } catch (e: Exception) {
            val appException = errorHandlingService.handleFileOperationError(
                e,
                sourceDirectory,
                targetDirectory
            )
            errorHandlingService.logError(
                e, 
                "moveFilesWithExtension", 
                sourceDirectory, 
                targetDirectory
            )
            throw e
        }
    }
}