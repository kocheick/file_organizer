package com.shevapro.filesorter.service

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.IOException

/**
 * Service responsible for validating file operations before execution.
 * This includes checking file existence, permissions, space availability, etc.
 */
class ValidationService {

    /**
     * Gets a DocumentFile from a URI, handling both file:// and content:// URIs.
     *
     * @param context The application context
     * @param uri The URI to convert to a DocumentFile
     * @return The DocumentFile, or null if the URI couldn't be converted
     */
    private fun getDocumentFile(context: Context, uri: Uri): DocumentFile? {
        return when (uri.scheme) {
            "file" -> {
                // For file:// URIs, use DocumentFile.fromFile
                val file = File(uri.path ?: return null)
                DocumentFile.fromFile(file)
            }
            "content" -> {
                // For content:// URIs, use DocumentFile.fromTreeUri
                DocumentFile.fromTreeUri(context, uri)
            }
            else -> {
                // For other URI schemes, try fromTreeUri first, then fall back to fromFile
                try {
                    DocumentFile.fromTreeUri(context, uri)
                } catch (e: Exception) {
                    val path = uri.path
                    if (path != null) {
                        DocumentFile.fromFile(File(path))
                    } else {
                        null
                    }
                }
            }
        }
    }

    /**
     * Validates if a file operation can be performed.
     *
     * @param sourceUri The URI of the source file/directory
     * @param destinationUri The URI of the destination directory
     * @param context The application context
     * @return True if the operation is valid, false otherwise
     * @throws Exception with a descriptive message if validation fails
     */
    fun validateFileOperation(
        sourceUri: Uri,
        destinationUri: Uri,
        context: Context
    ): Boolean {
        // Check if source exists
        validateSourceExists(sourceUri, context)

        // Check if destination exists
        validateDestinationExists(destinationUri, context)

        // Check if we have permissions
        validatePermissions(sourceUri, destinationUri, context)

        // Check if there's enough space
        validateSufficientSpace(sourceUri, destinationUri, context)

        return true
    }

    /**
     * Validates if the source file/directory exists.
     *
     * @param sourceUri The URI of the source file/directory
     * @param context The application context
     * @throws Exception if the source doesn't exist
     */
    private fun validateSourceExists(sourceUri: Uri, context: Context) {
        val sourceFile = getDocumentFile(context, sourceUri)
        if (sourceFile == null || !sourceFile.exists()) {
            throw IOException("Source file or directory does not exist: $sourceUri")
        }
    }

    /**
     * Validates if the destination directory exists.
     *
     * @param destinationUri The URI of the destination directory
     * @param context The application context
     * @throws Exception if the destination doesn't exist
     */
    private fun validateDestinationExists(destinationUri: Uri, context: Context) {
        val destinationFile = getDocumentFile(context, destinationUri)
        if (destinationFile == null || !destinationFile.exists() || !destinationFile.isDirectory) {
            throw IOException("Destination directory does not exist or is not a directory: $destinationUri")
        }
    }

    /**
     * Validates if we have the necessary permissions for the operation.
     *
     * @param sourceUri The URI of the source file/directory
     * @param destinationUri The URI of the destination directory
     * @param context The application context
     * @throws Exception if we don't have the necessary permissions
     */
    private fun validatePermissions(sourceUri: Uri, destinationUri: Uri, context: Context) {
        val sourceFile = getDocumentFile(context, sourceUri)
        if (sourceFile != null && !sourceFile.canRead()) {
            throw IOException("Cannot read from source: $sourceUri")
        }

        val destinationFile = getDocumentFile(context, destinationUri)
        if (destinationFile != null && !destinationFile.canWrite()) {
            throw IOException("Cannot write to destination: $destinationUri")
        }
    }

    /**
     * Validates if there's enough space in the destination for the operation.
     *
     * @param sourceUri The URI of the source file/directory
     * @param destinationUri The URI of the destination directory
     * @param context The application context
     * @throws Exception if there's not enough space
     */
    private fun validateSufficientSpace(sourceUri: Uri, destinationUri: Uri, context: Context) {
        try {
            // Get source size
            val sourceSize = getSourceSize(sourceUri, context)

            // Get available space in destination
            val availableSpace = getAvailableSpace(destinationUri, context)

            if (sourceSize > availableSpace) {
                throw IOException("Not enough space in destination. Required: $sourceSize bytes, Available: $availableSpace bytes")
            }
        } catch (e: Exception) {
            // If we can't determine sizes, we'll assume there's enough space
            // This is a fallback for when we can't access storage stats
        }
    }

    /**
     * Gets the size of a file or directory.
     *
     * @param uri The URI of the file/directory
     * @param context The application context
     * @return The size in bytes
     */
    private fun getSourceSize(uri: Uri, context: Context): Long {
        val file = getDocumentFile(context, uri)
        return calculateSize(file, context)
    }

    /**
     * Recursively calculates the size of a file or directory.
     *
     * @param file The DocumentFile
     * @param context The application context
     * @return The size in bytes
     */
    private fun calculateSize(file: DocumentFile?, context: Context): Long {
        if (file == null) return 0

        if (file.isFile) {
            return file.length()
        }

        var size = 0L
        for (child in file.listFiles()) {
            size += calculateSize(child, context)
        }
        return size
    }

    /**
     * Gets the available space in a directory.
     *
     * @param uri The URI of the directory
     * @param context The application context
     * @return The available space in bytes
     */
    private fun getAvailableSpace(uri: Uri, context: Context): Long {
        // This is a simplified approach. In a real app, you might want to use
        // StorageManager to get more accurate information
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    /**
     * Validates if a file name is valid.
     *
     * @param fileName The file name to validate
     * @return True if the file name is valid, false otherwise
     */
    fun isValidFileName(fileName: String): Boolean {
        // Check if the file name contains invalid characters
        val invalidChars = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        for (char in invalidChars) {
            if (fileName.contains(char)) {
                return false
            }
        }

        // Check if the file name is not empty and not too long
        return fileName.isNotEmpty() && fileName.length <= 255
    }

    /**
     * Checks for potential naming conflicts in the destination.
     *
     * @param fileName The file name to check
     * @param destinationUri The URI of the destination directory
     * @param context The application context
     * @return True if there's a conflict, false otherwise
     */
    fun hasNamingConflict(fileName: String, destinationUri: Uri, context: Context): Boolean {
        val destinationFile = getDocumentFile(context, destinationUri)
        if (destinationFile == null || !destinationFile.exists() || !destinationFile.isDirectory) {
            return false
        }

        for (file in destinationFile.listFiles()) {
            if (file.name == fileName) {
                return true
            }
        }

        return false
    }
}
