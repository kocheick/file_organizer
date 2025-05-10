package com.shevapro.filesorter.service

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.shevapro.filesorter.model.TaskStats
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Service responsible for core file operations such as moving, copying, and deleting files.
 */
class FileOperationService(private val statsService: StatsService) {

    /**
     * Moves files of a specific type from source to destination.
     *
     * @param source The URI of the source directory
     * @param destination The URI of the destination directory
     * @param extension The file extension to filter by
     * @param context The application context
     * @param moveProgress Callback to report progress
     * @param permissionCallback Callback to handle permission requests
     */
    private val TAG = "FileOperationService" // Assuming TAG is defined at class level or will be added
    suspend fun moveFilesByType(
        source: String,
        destination: String,
        extension: String,
        context: Context,
        moveProgress: (TaskStats) -> Unit = {},
        permissionCallback: (Uri) -> Unit = {}
    ) {
        Log.d(TAG, "moveFilesByType - Source: $source, Destination: $destination, Extension: $extension")

        // Normalize URIs and log them
        val sourceUri = normalizeUri(source)
        val destUri = normalizeUri(destination)
        Log.d(TAG, "Normalized URIs - Source: $sourceUri, Destination: $destUri")

        val (sourceFolder, destinationFolder) = getFoldersAsDocumentFiles(
            source,
            destination,
            context
        )
        Log.d(TAG, "Retrieved DocumentFiles - Source: ${sourceFolder.uri}, Destination: ${destinationFolder.uri}")
        Log.d(TAG, "Source exists: ${sourceFolder.exists()}, can read: ${sourceFolder.canRead()}")
        Log.d(TAG, "Destination exists: ${destinationFolder.exists()}, can write: ${destinationFolder.canWrite()}")

        val filesToMove = getFilesToMove(sourceFolder, extension)
        Log.d(TAG, "Found ${filesToMove.size} files with extension '$extension'")


        if (filesToMove.isNotEmpty()) {
            Log.d(TAG, "Starting to move files...")

            moveFiles(
                filesToMove,
                destinationFolder,
                context.contentResolver,
                moveProgress,
                permissionCallback
            )
        } else {
            Log.d(TAG, "No files to move.")
            // Report empty task to the UI
            val emptyTaskStats = TaskStats(
                totalFiles = 0,
                numberOfFilesMoved = 0,
                currentFileName = "",
                sourceFolder = source,
                destinationFolder = destination,
                fileExtension = extension,
                startTime = System.currentTimeMillis()
            )
            moveProgress(emptyTaskStats)
        }

        statsService.logTaskDetails(source,
            destination,
            extension.lowercase().trim(), filesToMove.size)
    }

    /**
     * Gets files with a specific extension from a folder.
     *
     * @param sourceFolder The source folder as a DocumentFile
     * @param extension The file extension to filter by
     * @return List of DocumentFiles matching the extension
     */
    private fun getFilesToMove(
        sourceFolder: DocumentFile,
        extension: String
    ) = sourceFolder
        .listFiles()
        .filter { file ->
            // Make sure we only get files with the correct extension
            file.isFile && file.name?.let { name ->
                // Ensure we're not picking up files that already have duplicate extensions
                // like "file.mp3.mp3" which would cause problems when moved
                val normalizedName = name.lowercase()
                val normalizedExt = extension.lowercase()

                // Simple check for extension at the end
                normalizedName.endsWith(".$normalizedExt") &&
                        // Make sure it's not already duplicated
                        !normalizedName.endsWith(".$normalizedExt.$normalizedExt")
            } == true
        }

    /**
     * Gets file extensions available in a folder.
     *
     * @param path The path to the folder
     * @param context The application context
     * @return List of file extensions found in the folder
     */
    fun getFilesExtensionsForFolder(path: String, context: Context): List<String> {
        val folderPath = normalizeUri(path)
        val folder = getDocumentFile(context, folderPath)
            ?: throw Exception("Selected folder is not accessible or the app is not granted the required permissions.")

        return folder.listFiles().filter { it.isFile }
            .map { val fileName = it.name; fileName?.substringAfterLast(".") ?: "" }
            .filter { it.isNotEmpty() }.toSet().toList()
    }

    /**
     * Gets a DocumentFile from a URI.
     *
     * @param context The application context
     * @param pathUri The URI of the path
     * @return The DocumentFile or null if not accessible
     */
    private fun getDocumentFile(context: Context, pathUri: Uri): DocumentFile? {
        return when (pathUri.scheme) {
            "file" -> {
                // For file:// URIs, use DocumentFile.fromFile with proper path extraction
                val path = pathUri.path ?: return null

                // Convert paths like /sdcard/ to their real paths
                val cleanPath = tryConvertToRealPath(path)

                val file = java.io.File(cleanPath)
                if (!file.exists()) {
                    println("File does not exist: $cleanPath (original URI path: ${pathUri.path})")
                }

                DocumentFile.fromFile(file)
            }
            "content" -> {
                // For content:// URIs, use DocumentFile.fromTreeUri
                DocumentFile.fromTreeUri(context, pathUri)
            }
            else -> {
                // For other URI schemes, try fromTreeUri first, then fall back to fromFile
                val documentFile = DocumentFile.fromTreeUri(context, pathUri)
                if (documentFile != null && documentFile.canRead()) {
                    documentFile
                } else {
                    val path = pathUri.path
                    if (path != null) {
                        DocumentFile.fromFile(java.io.File(path))
                    } else {
                        null
                    }
                }
            }
        }
    }

    /**
     * Gets source and destination folders as DocumentFiles.
     *
     * @param source The source path
     * @param destination The destination path
     * @param context The application context
     * @return Pair of source and destination DocumentFiles
     */
    private fun getFoldersAsDocumentFiles(
        source: String,
        destination: String, 
        context: Context
    ): Pair<DocumentFile, DocumentFile> {
        val sourceFileUri = normalizeUri(source)
        val destinationFolderUri = normalizeUri(destination)

        val sourceFolder = getDocumentFile(context, sourceFileUri)
            ?: throw Exception("Source folder does not exist or is not accessible.")
        val destinationFolder = getDocumentFile(context, destinationFolderUri)
            ?: throw Exception("Destination folder does not exist or is not accessible.")

        return Pair(sourceFolder, destinationFolder)
    }

    /**
     * Normalizes a URI string into a proper Uri object, handling different URI formats.
     *
     * @param uriString The URI string to normalize
     * @return Normalized Uri object
     */
    private fun normalizeUri(uriString: String): Uri {
        val uri = Uri.parse(uriString)

        // If it's a file:// URI with just a path component, ensure proper formatting
        if (uri.scheme == "file" && uri.host == null) {
            val path = uri.path ?: return uri

            // Check if this is actually a path without scheme
            if (path.startsWith("/") && !uriString.startsWith("file://")) {
                return Uri.fromFile(File(path))
            }
        }

        // Handle the case when no scheme is provided
        if (uri.scheme == null) {
            // Assume it's a file path
            return Uri.fromFile(File(uriString))
        }

        return uri
    }

    /**
     * Tries to convert various Android path formats to the actual path.
     * Handles formats like /sdcard/, /storage/self/primary/, etc.
     *
     * @param path The path to convert
     * @return The real path
     */
    private fun tryConvertToRealPath(path: String): String {
        // The standard Android data path that most paths should resolve to
        val standardDataPath = "/storage/emulated/0/"

        return when {
            // Android common shorthand for primary storage
            path.startsWith("/sdcard/") -> {
                standardDataPath + path.substring("/sdcard/".length)
            }
            // Common format on some devices
            path.startsWith("/storage/self/primary/") -> {
                standardDataPath + path.substring("/storage/self/primary/".length)
            }
            // Handle paths that might include encoded spaces or special characters
            path.contains("%20") -> {
                path.replace("%20", " ")
            }
            // Default: return the original path
            else -> path
        }
    }

    /**
     * Moves files from source to destination.
     *
     * @param filesToMove List of files to move
     * @param destinationFolder The destination folder
     * @param contentResolver The content resolver
     * @param onShareProgress Callback to report progress
     * @param retryCallback Callback for retry operations
     */
    private suspend fun moveFiles(
        filesToMove: List<DocumentFile>,
        destinationFolder: DocumentFile,
        contentResolver: ContentResolver,
        onShareProgress: (TaskStats) -> Unit = {},
        retryCallback: (Uri) -> Unit = {},
    ) {
        val total = filesToMove.size
        if (total == 0) return

        // Get source folder from the first file's parent URI
        val sourceFolder = filesToMove.firstOrNull()?.parentFile?.uri?.toString() ?: ""
        val destFolder = destinationFolder.uri.toString()
        // Get file extension from the first file
        val fileExtension = filesToMove.firstOrNull()?.name?.substringAfterLast(".", "") ?: ""
        val fileType = filesToMove.firstOrNull()?.type ?: "*/*"

        // Calculate total size of all files
        var totalBytes = 0L
        filesToMove.forEach { file ->
            totalBytes += file.length()
        }

        Log.d(TAG, "Total bytes to transfer: $totalBytes")

        var stats = TaskStats(
            totalFiles = total,
            numberOfFilesMoved = 0,
            currentFileName = "",
            sourceFolder = sourceFolder,
            destinationFolder = destFolder,
            fileExtension = fileExtension,
            fileType = fileType,
            startTime = System.currentTimeMillis(),
            totalBytes = totalBytes
        )

        // Send initial progress update
        onShareProgress(stats)

        filesToMove.forEachIndexed { index, file ->
            Log.d(TAG, "Moving ${file.name} (${index + 1}/$total)")
            withContext(IO) {
                val originalName = file.name!!

                // Get the base name and original extension to prevent duplication
                val lastDotIndex = originalName.lastIndexOf('.')
                val baseName = if (lastDotIndex > 0) originalName.substring(0, lastDotIndex) else originalName
                val originalExtension = if (lastDotIndex > 0) originalName.substring(lastDotIndex + 1) else ""

                // Prevent extension duplication by using explicit MIME type handling
                // Always use a generic MIME type to prevent automatic extension addition
                val mimeType = "*/*"

                // Make sure we're not working with an already duplicated filename
                var properFileName = originalName
                val extensionLower = originalExtension.lowercase()

                // Check for already duplicated extensions like "file.mp3.mp3"
                if (extensionLower.isNotEmpty() && baseName.lowercase().endsWith(".$extensionLower")) {
                    // Found a case like "file.mp3.mp3" - fix it by removing the duplicate
                    val baseNameWithoutDupe = baseName.substring(0, baseName.length - extensionLower.length - 1)
                    properFileName = "$baseNameWithoutDupe.$originalExtension"
                    Log.d(TAG, "Fixed duplicate extension: $originalName -> $properFileName")
                }

                Log.d(TAG, "Moving file with name: $properFileName")

                // Always use generic MIME type to prevent extension manipulation
                val destinationFile = destinationFolder.createFile(mimeType, properFileName)

                if (destinationFile != null) {
                    val fileSize = file.length()

                    stats = stats.copy(
                        currentFileName = file.name!!,
                        fileType = file.type ?: "*/*",
                        currentFileSize = fileSize,
                        currentBytesTransferred = 0
                    )
                    // Send progress update before starting file copy
                    onShareProgress(stats)
delay(1000)
                    contentResolver.openOutputStream(destinationFile.uri)
                        ?.use { outputStream ->
                            contentResolver.openInputStream(file.uri)?.use { inputStream ->
                                // Custom copy implementation with progress reporting
                                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                var bytesRead: Int
                                var totalBytesRead = 0L
                                var lastProgressUpdate = 0L

                                while (inputStream.read(buffer).also { bytesRead = it } >= 0) {
                                    outputStream.write(buffer, 0, bytesRead)
                                    totalBytesRead += bytesRead

                                    // Update total bytes transferred
                                    val newTotalBytesTransferred = stats.totalBytesTransferred + bytesRead

                                    // Update progress every 100ms or when buffer is full
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastProgressUpdate > 100 || bytesRead < DEFAULT_BUFFER_SIZE) {
                                        stats = stats.copy(
                                            currentBytesTransferred = totalBytesRead,
                                            totalBytesTransferred = newTotalBytesTransferred
                                        )
                                        onShareProgress(stats)
                                        lastProgressUpdate = currentTime
                                    }
                                }

                                // Ensure final progress is reported
                                // Correctly calculate totalBytesTransferred:
                                // stats.totalBytesTransferred (before this line) contains the sum of bytes from previous files
                                // PLUS whatever was last reported for the current file's progress (stats.currentBytesTransferred).
                                // To get the base total from previous files, subtract the current file's last reported progress.
                                val totalBytesTransferredFromPreviousFiles = stats.totalBytesTransferred - stats.currentBytesTransferred
                                stats = stats.copy(
                                    currentBytesTransferred = fileSize, // Current file is now fully processed
                                    totalBytesTransferred = totalBytesTransferredFromPreviousFiles + fileSize, // Add current file's full size to the base
                                    numberOfFilesMoved = stats.numberOfFilesMoved, // No change to numberOfFilesMoved at this point
                                    totalFiles = stats.totalFiles // totalFiles does not change during the operation
                                )
                                onShareProgress(stats)
                            }
                                ?: throw Exception("An error occurred while copying file ${file.name} into destination folder. ${file.uri}")
                        }
                        ?: throw Exception("An error occurred while copying file ${file.name} from source folder. ${file.uri}")

                    // Delete the original file
                    if (!file.delete()) {
                        throw Exception("Failed to delete original from source folder. ${file.uri}")
                    }

                    // Update stats with completed file count after successful move
                    stats = stats.copy(
                        numberOfFilesMoved = index + 1,  // Add 1 since index is zero-based
                        currentBytesTransferred = 0,     // Reset for next file
                        currentFileSize = 0              // Reset for next file
                    )
                    // Send progress update after file is moved
                    onShareProgress(stats)
                } else {
                    retryCallback(file.uri)
                }

                if (index < filesToMove.size - 1) {
                    delay(300)
                }
            }
        }

        stats = stats.copy(
            numberOfFilesMoved = total,
            currentBytesTransferred = 0,
            currentFileSize = 0,
            totalBytesTransferred = totalBytes,
            totalBytes = totalBytes
        )

        if (stats.numberOfFilesMoved < stats.totalFiles) {
            stats = stats.copy(numberOfFilesMoved = stats.totalFiles)
        }

        println("Final stats after file moves - Files: ${stats.numberOfFilesMoved}/${stats.totalFiles}, Bytes: ${stats.totalBytesTransferred}/${stats.totalBytes}")
        onShareProgress(stats)
    }
}
