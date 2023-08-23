package com.shevapro.filesorter.service

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.Utility.grantUrisPermissions
import com.shevapro.filesorter.model.EmptyContentException
import com.shevapro.filesorter.model.PermissionExceptionForUri
import com.shevapro.filesorter.model.TaskStats
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class FileMover private constructor(private val appStatsService: StatsService) {

    companion object {
        fun getInstance(appService: StatsService): FileMover = FileMover(appService)
    }

     fun getStats() = appStatsService.getLatestStats()
    suspend fun resetStats() = appStatsService.resetStats()


    suspend fun moveFilesByType(
        source: String,
        destination: String,
        extension: String,
        context: Context,
        moveProgress: (TaskStats) -> Unit = {}

    ) {
//        try {
            println("getting folder")

            val (sourceFolder, destinationFolder) = getFoldersAsDocumentFiles(
                source,
                destination,
                context
            )
            println("getting files ending in $extension")
            val filesToMove = sourceFolder.listFiles()
                .filter { file -> file.isFile && file.name?.endsWith(extension) == true }
                .onEach {
                    grantUrisPermissions(it.uri,context = context)
                    val hasPermission = Utility.hasPermission(context)
                    println("Do we have permission $hasPermission")
                    if (!hasPermission){
                        throw PermissionExceptionForUri(it.uri,"Permission needed for this move")
                    }
                }
            println("moving files")

            moveFiles(
                filesToMove, destinationFolder, context.contentResolver, moveProgress,{ grantUrisPermissions(it,context = context) },
            )

        appStatsService.insertMoveInfo(source,
            destination,
            extension.lowercase().trim(),filesToMove.size)
//        } catch (exception: Exception) {
//            if (exception is IOException) {
//                val partialUrlString = exception.message?.substringAfter("content://")
//                val uriString = "content://$partialUrlString"
//                val uri = Uri.parse(uriString)
//                askPermissionForUri(uri)
//            }
//        }
    }



    fun askPermissionForUri(uri: Uri) {

    }

    private fun getFoldersAsDocumentFiles(
        source: String,
        destination: String, context: Context
    ): Pair<DocumentFile, DocumentFile> {
        val sourceFileUri = Uri.parse(source)
        val destinationFolderUri = Uri.parse(destination)

        grantUrisPermissions(sourceFileUri, destinationFolderUri, context)

        val sourceFolder = DocumentFile.fromTreeUri(context, sourceFileUri)
            ?: throw Exception("Source folder does not exist or is not accessible.")
        val destinationFolder = DocumentFile.fromTreeUri(context, destinationFolderUri)
            ?: throw Exception("Destination folder does not exist or is not accessible.")


        return Pair(sourceFolder, destinationFolder)
    }


    private suspend fun moveFiles(
        filesToMove: List<DocumentFile>,
        destinationFolder: DocumentFile,
        contentResolver:ContentResolver,
        shareStats: (TaskStats) -> Unit = {},
        retryCallback: (Uri) -> Unit = {},
    ) {
        if (filesToMove.isNotEmpty()) {
            val total = filesToMove.size

            var stats = TaskStats(total,0,"")
            filesToMove.forEachIndexed { index,  file ->

println("moving ${file.name} progres should be $index")
                withContext(IO) {



//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) DocumentsContract.moveDocument(
//                            contentResolver, file.uri,
//                            file.parentFile!!.uri, destinationFolder.uri
//                        )
//                    else {
                        val destinationFile =
                            destinationFolder.createFile(file.type ?: "*/*", file.name!!)
//                                ?: throw Exception("Failed to create new file in destination folder. ${file.uri}")

                        if (destinationFile != null) {
                            stats = stats.copy(
                                numberOfFilesMoved = index,
                                currentFileName = file.name!!
                            )

                            contentResolver.openOutputStream(destinationFile.uri)
                                ?.use { outputStream ->
                                    contentResolver.openInputStream(file.uri)?.use { inputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                        ?: throw Exception("An error occured while copying file ${file.name} into destination folder. ${file.uri}")
                                }
                                ?: throw Exception("An error occured while copying file ${file.name} from source folder. ${file.uri}")

                            // Delete the original file
                            if (!file.delete()) {
                                throw Exception("Failed to delete original from source folder. ${file.uri}")
                            }
                            shareStats(stats)

                        } else {
                            retryCallback(file.uri)
                        }



                        delay(1200)

                }

            }


        } else {
            println("No file file with extension found")
            throw EmptyContentException("")
        }
    }


//    fun getFilesWithExtension(uri: Uri, extension: String): List<File> {
//        val contentResolver = context.contentResolver
//
//        val files = mutableListOf<File>()
//        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
//        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
//        val selectionArgs = arrayOf("%.$extension")
//
//        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
//
//        if (cursor != null) {
//            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
//
//            while (cursor.moveToNext()) {
//                val filePath = cursor.getString(columnIndex)
//                println(filePath)
//                files.add(File(filePath))
//            }
//            cursor.close()
//        }
//
//        return files
//    }
//
//    fun getFilesWithExtension(folderPath: String, extension: String): List<File> {
//        val files = mutableListOf<File>()
//        val folder = File(folderPath)
//
//        if (folder.exists() && folder.isDirectory) {
//            val fileFilter = FileFilter { file ->
//                file.isFile && file.extension == extension
//            }
//            files.addAll(folder.listFiles(fileFilter)?.toList() ?: emptyList())
//        }
//        println(files)
//        return files
//    }
//
//    fun getFilesFromFolder(folderUri: Uri): List<File> {
//        val contentResolver = context.contentResolver
//        val files = mutableListOf<File>()
//
//        val projection = arrayOf(
//            OpenableColumns.DISPLAY_NAME,
//            OpenableColumns.SIZE,
//            "_data"
//        )
//
//        val cursor = contentResolver.query(folderUri, projection, null, null, null)
//
//        cursor?.use {
//            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
//            val dataIndex = it.getColumnIndex("_data")
//
//            while (it.moveToNext()) {
//                val name = it.getString(nameIndex)
//                val size = it.getLong(sizeIndex)
//                val path = it.getString(dataIndex)
//
//                if (path != null) {
//                    val file = File(path)
//
//                    // Filter out folders
//                    if (!file.isDirectory) {
//                        files.add(file)
//                    }
//                }
//            }
//        }
//
//        return files
//    }
//
//    @RequiresApi(Build.VERSION_CODES.R)
//    suspend fun getFiles(directory: Uri, extension: String): List<Uri> {
//
////        val filesFound = mutableListOf<File>()
////
////        val externalStorageVolumes = mutableListOf<File>()
////        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
////        val storageVolumes = storageManager.primaryStorageVolume
////
////            println("storage volume $storageVolumes")
////            if (storageVolumes.isPrimary && storageVolumes.isRemovable) {
////                println("volume is primary $storageVolumes")
////                val volumePath = storageVolumes.directory?.path!!
////                val volumeFile = File(volumePath)
////                externalStorageVolumes.add(volumeFile)
////            }
////
////
////        externalStorageVolumes.forEach { volume ->
////            println("external storage volume $volume")
////
////            volume.listFiles()?.forEach { file ->
////                println("file  ${file.name}")
////
////                if (file.isFile && file.name.endsWith(extension)) {
////                    filesFound.add(file)
////                }
////            }
////        }
////
////// Now you have a list of files with the specified extension
////// You can do whatever you want with them
////
////        println("your files $filesFound")
//
//        val directoryUri = directory
//        val contentResolver = context.contentResolver
//
//
//        val folderDocument = DocumentFile.fromTreeUri(context, directoryUri)
//
//        println(folderDocument)
//
//        if (folderDocument != null) {
//            val files = folderDocument.listFiles()
//
//            files.forEach { file ->
//                print(file.name)
//                if (file.isFile) {
//                    // Do something with the file
//                    Log.d("TAG", "Found file: ${file.name}")
//                }
//            }
//        }
//
//
//        return withContext(Dispatchers.IO) {
//            val files = mutableListOf<Uri>()
//            val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//
//            val projection = arrayOf(
//                MediaStore.Files.FileColumns._ID,
//                MediaStore.Files.FileColumns.DISPLAY_NAME,
//                MediaStore.Files.FileColumns.SIZE,
//                MediaStore.Files.FileColumns.MIME_TYPE
//            )
//            val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
//            val selectionArgs = arrayOf("%$extension")
//            val sortOrder = "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"
//
//            directoryUri.takeIf { it != Uri.EMPTY }?.let { uriToSearchIn ->
//                val cursor =
//                    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
//                print(cursor.toString())
//                cursor?.use {
//                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
//                    while (it.moveToNext()) {
//                        val id = it.getLong(idColumn)
//                        val fileUri = Uri.withAppendedPath(uri, id.toString())
//                        if (fileUri.isInDirectory(uriToSearchIn)) {
//                            files.add(fileUri)
//                        }
//                    }
//                }
//            }
//            files
//        }
//    }
//    suspend fun moveFilesByExtension(
//        sourceUri: String,
//        destinationUri: String,
//        extension: String,
//        contentResolver: ContentResolver = context.contentResolver,
//
//
//    ) {
//
//
//
////        val takeFlags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        withContext(Dispatchers.IO) {
//            // Create the destination folder if it doesn't exist
////
////            contentResolver.takePersistableUriPermission(Uri.parse(destinationUri),takeFlags)
////            contentResolver.takePersistableUriPermission(Uri.parse(sourceUri),takeFlags)
//
//            val destinationFolder = DocumentsContract.createDocument(
//                contentResolver,
//                DocumentsContract.buildDocumentUriUsingTree(Uri.parse(destinationUri), DocumentsContract.getTreeDocumentId(Uri.parse(destinationUri))!!),
//                DocumentsContract.Document.MIME_TYPE_DIR,
//                DocumentsContract.getTreeDocumentId(Uri.parse(destinationUri))
//            )
//
//
//
//            // Query the files with the specified extension from the source URI
//            val projection = arrayOf(
//                MediaStore.MediaColumns._ID,
//                DISPLAY_NAME,
//                SIZE,
//                MIME_TYPE,
//                RELATIVE_PATH
//            )
//            val selection = "$RELATIVE_PATH LIKE ? AND $MIME_TYPE LIKE ?"
//            val selectionArgs = arrayOf("%/$extension", "%/$extension")
//            val sortOrder = "$DISPLAY_NAME ASC"
//
//            val sourceFolderUri = DocumentsContract.buildDocumentUriUsingTree(Uri.parse(sourceUri), DocumentsContract.getTreeDocumentId(Uri.parse(sourceUri))!!)
//            val sourceFolderId = DocumentsContract.getTreeDocumentId(sourceFolderUri)
//
//            val queryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//            } else {
//                MediaStore.Files.getContentUri("external")
//            }
//
//            val queryCursor = contentResolver.query(
//                queryUri,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder
//            )
//
//            println("Found 1 ${queryCursor?.columnNames?.toList()}")
//
//
//            queryCursor?.use { cursor ->
//                val idColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
//                val displayNameColumnIndex = cursor.getColumnIndex(DISPLAY_NAME)
//                val sizeColumnIndex = cursor.getColumnIndex(SIZE)
//                val mimeTypeColumnIndex = cursor.getColumnIndex(MIME_TYPE)
//                val relativePathColumnIndex = cursor.getColumnIndex(RELATIVE_PATH)
//
//                while (cursor.moveToNext()) {
//                    val fileId = cursor.getLong(idColumnIndex)
//                    val displayName = cursor.getString(displayNameColumnIndex)
//                    val size = cursor.getLong(sizeColumnIndex)
//                    val mimeType = cursor.getString(mimeTypeColumnIndex)
//                    val relativePath = cursor.getString(relativePathColumnIndex)
//                    println("Found 2 $displayName")
//
//                    // Copy the file to the destination folder
//                    val destinationFileUri = DocumentsContract.createDocument(
//                        contentResolver,
//                        destinationFolder!!,
//                        mimeType,
//                        displayName
//                    )
//
//
//                    contentResolver.openOutputStream(destinationFileUri!!, "w").use { output ->
//                        contentResolver.openInputStream(ContentUris.withAppendedId(queryUri, fileId)).use { input ->
//                            if (output != null) {
//                                input?.copyTo(output)
//                            }
//                        }
//                    }
//
//                    // Delete the original file
//                    val deleteUri = DocumentsContract.buildDocumentUriUsingTree(sourceFolderUri, "$sourceFolderId/$relativePath/$displayName")
//                    contentResolver.delete(deleteUri, null, null)
//                }
//            }
//        }
//    }
//    suspend fun moveFilesWithExtension(
//        sourceFolderUri: Uri,
//        destinationFolderUri: Uri,
//        extension: String
//    ) {
//        println("Starting move...")
//
//        // Check if the source folder exists
//        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
////        context.contentResolver.takePersistableUriPermission(sourceFolderUri, takeFlags)
////        context.grantUriPermission(context.packageName,sourceFolderUri, takeFlags)
//        println("your file ${(Environment.getExternalStorageDirectory()).listFiles()?.find { it.name.contains("Pictures") }?.listFiles()?.toList()}")
//        val sourceFolder = DocumentFile.fromTreeUri(context, sourceFolderUri)
//        if (sourceFolder == null || !sourceFolder.exists() || !sourceFolder.isDirectory) {
//            println("Source folder does not exist.")
//            return
//        }
//
//        // Check if the destination folder exists
//        val destinationFolder = DocumentFile.fromTreeUri(context, destinationFolderUri)
//        if (destinationFolder == null || !destinationFolder.exists() || !destinationFolder.isDirectory) {
//            println("Destination folder does not exist.")
//            return
//        }
//        val contentResolver = context.getActivity()?.contentResolver!!
//        println("PREPARING QUERY ")
//
//
//
////        context.contentResolver.takePersistableUriPermission(Uri.parse(intent.dataString), takeFlags)
////                    context.grantUriPermission(context.packageName,Uri.parse(intent.dataString), takeFlags)
//        // Define the columns to query
//        val projection = arrayOf(
//            MediaStore.MediaColumns._ID,
//            MediaStore.MediaColumns.DISPLAY_NAME,
//            MediaStore.MediaColumns.MIME_TYPE,
//        )
//
//        // Define the selection criteria
//        val selection = "${MediaStore.MediaColumns.DATA} LIKE ?"
//        val selectionArgs = arrayOf("%.$extension")
//
//        // Query the source folder for files with the given extension
//        val queryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//        } else {
//            MediaStore.Files.getContentUri("external")
//        }
//        val cursor =contentResolver.query(
//            queryUri,
//            projection,
//            selection,
//            selectionArgs,
//            null
//        )
//
//        cursor?.use {
//            val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
//            val nameColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
//            val mimeTypeColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
//
//
//            println("PRINTING ")
//
//            while (cursor.moveToNext()) {
//                val fileId = cursor.getLong(idColumnIndex)
//                val fileName = cursor.getString(nameColumnIndex)
//                val mimeType = cursor.getString(mimeTypeColumnIndex)
//
//                val sourceFileUri = ContentUris.withAppendedId(queryUri, fileId)
//                context.getActivity()?.contentResolver?.takePersistableUriPermission(sourceFileUri.normalizeScheme(), takeFlags)
//                context.getActivity()?.grantUriPermission(context.packageName,sourceFileUri, takeFlags)
//                println("PRINTING PRINTIN $fileId $fileName $sourceFileUri")
//
//                val sourceFile = DocumentFile.fromSingleUri(context, sourceFileUri)
//
//                if (sourceFile != null && sourceFile.isFile) {
//                    println("BEFORE COPYING ")
//
//                    val destinationFile = destinationFolder.createFile(mimeType, fileName)
//                    if (destinationFile != null) {
//                        println("PRINTING COPYING ")
//
//                        contentResolver.openOutputStream(destinationFile.uri)?.use { outputStream ->
//                            contentResolver.openInputStream(sourceFile.uri)?.use { inputStream ->
//                                inputStream.copyTo(outputStream)
//                            }
//                        }
//                    }
//                    // Delete the original file
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        contentResolver.delete(sourceFileUri, null)
//                    } else {
//                        contentResolver.delete(sourceFileUri, selection,selectionArgs)
//
//                    }
////need to use correct file uri
////                        DocumentFile.fromSingleUri(context, sourceFileUri)?.delete()
//                }
//            }
//        }
//    }
//
//    suspend fun moveFiles(sourcePath: String, destinationPath: String, extension: String) {
//
//        val source = Uri.parse((sourcePath))
//        val destination = Uri.parse(Uri.decode(destinationPath))
//        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
//        context.getActivity()?.contentResolver?.takePersistableUriPermission(source, takeFlags)
//        context.getActivity()?.grantUriPermission(context.packageName,source, takeFlags)
//        context.getActivity()?.contentResolver?.takePersistableUriPermission(destination, takeFlags)
//        context.getActivity()?.grantUriPermission(context.packageName,destination, takeFlags)
//
//        val contentResolver = context.contentResolver
//
//
//        val sourceFolder = DocumentFile.fromTreeUri(context, source)
////                ?: DocumentFile.fromSingleUri(context, source)
//        val destFolder = DocumentFile.fromTreeUri(context, destination)
////                ?: DocumentFile.fromSingleUri(context, destination)
//
//
//        if (sourceFolder == null || !sourceFolder.exists() || !sourceFolder.isDirectory) {
//
//            throw Exception("Source folder does not exist or is not accessible. ${sourceFolder?.uri}")
//
//        }
//
//        if (destFolder == null || !destFolder.exists() || !destFolder.isDirectory) {
//            throw Exception("Destination folder does not exist or is not accessible.")
//        }
//        // Open an InputStream to read the content of the original file
//
//        var counter = 0
//        sourceFolder.listFiles().forEach { sourceFile ->
//            println("file #$counter ${sourceFile.uri}")
//            sourceFile?.let { file ->
//                if (file.name?.endsWith(extension) == true) {
//                    counter++
//
//                    println("found ${file.uri} !")
//                    val newFile = destFolder.createFile(file.type ?: "*/*", file.name!!)
//                        ?: throw Exception("Failed to copy file in the destination file.")
//
//                    contentResolver.openInputStream(file.uri)?.use { inputStream ->
//                        // Open an OutputStream to write the content of the original file to the new file
//                        contentResolver.openOutputStream(newFile.uri)
//                            ?.use { outputStream ->
//                                println("to be moved to ")
//                                inputStream.copyTo(outputStream)
//                            }
//                    }
//                    // Delete the original file
//                    if (!file.delete()) {
//                        throw IOException("Failed to delete the original file.")
//                    }
//                }
//            }
//        }
//
//        if (counter == 0) {
//            throw NoFileFoundException("No file found with $extension extension. $counter")
//        }
//
//
//    }
//


}


private fun Uri.isInDirectory(uriToSearchIn: Uri): Boolean {
    println("your uri to serch $uriToSearchIn")
    return true
}

