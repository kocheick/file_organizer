package com.example.fileorganizer.service

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

class FileMover(private val context: Context) {

    fun getFilesWithExtension(uri: Uri, extension: String): List<File> {
        val contentResolver = context.contentResolver

        val files = mutableListOf<File>()
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val selectionArgs = arrayOf("%.$extension")

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)

        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val filePath = cursor.getString(columnIndex)
                println(filePath)
                files.add(File(filePath))
            }
            cursor.close()
        }

        return files
    }

    fun getFilesWithExtension(folderPath: String, extension: String): List<File> {
        val files = mutableListOf<File>()
        val folder = File(folderPath)

        if (folder.exists() && folder.isDirectory) {
            val fileFilter = FileFilter { file ->
                file.isFile && file.extension == extension
            }
            files.addAll(folder.listFiles(fileFilter)?.toList() ?: emptyList())
        }
        println(files)
        return files
    }

    fun getFilesFromFolder(folderUri: Uri): List<File> {
        val contentResolver = context.contentResolver
        val files = mutableListOf<File>()

        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
            "_data"
        )

        val cursor = contentResolver.query(folderUri, projection, null, null, null)

        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            val dataIndex = it.getColumnIndex("_data")

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val size = it.getLong(sizeIndex)
                val path = it.getString(dataIndex)

                if (path != null) {
                    val file = File(path)

                    // Filter out folders
                    if (!file.isDirectory) {
                        files.add(file)
                    }
                }
            }
        }

        return files
    }

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun getFiles(directory: Uri, extension: String): List<Uri> {

//        val filesFound = mutableListOf<File>()
//
//        val externalStorageVolumes = mutableListOf<File>()
//        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
//        val storageVolumes = storageManager.primaryStorageVolume
//
//            println("storage volume $storageVolumes")
//            if (storageVolumes.isPrimary && storageVolumes.isRemovable) {
//                println("volume is primary $storageVolumes")
//                val volumePath = storageVolumes.directory?.path!!
//                val volumeFile = File(volumePath)
//                externalStorageVolumes.add(volumeFile)
//            }
//
//
//        externalStorageVolumes.forEach { volume ->
//            println("external storage volume $volume")
//
//            volume.listFiles()?.forEach { file ->
//                println("file  ${file.name}")
//
//                if (file.isFile && file.name.endsWith(extension)) {
//                    filesFound.add(file)
//                }
//            }
//        }
//
//// Now you have a list of files with the specified extension
//// You can do whatever you want with them
//
//        println("your files $filesFound")

        val directoryUri = directory
        val contentResolver = context.contentResolver


        val folderDocument = DocumentFile.fromTreeUri(context, directoryUri)

        println(folderDocument)

        if (folderDocument != null) {
            val files = folderDocument.listFiles()

            files.forEach { file ->
                print(file.name)
                if (file.isFile) {
                    // Do something with the file
                    Log.d("TAG", "Found file: ${file.name}")
                }
            }
        }


        return withContext(Dispatchers.IO) {
            val files = mutableListOf<Uri>()
            val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
            val selectionArgs = arrayOf("%$extension")
            val sortOrder = "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"

            directoryUri.takeIf { it != Uri.EMPTY }?.let { uriToSearchIn ->
                val cursor =
                    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
                print(cursor.toString())
                cursor?.use {
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    while (it.moveToNext()) {
                        val id = it.getLong(idColumn)
                        val fileUri = Uri.withAppendedPath(uri, id.toString())
                        if (fileUri.isInDirectory(uriToSearchIn)) {
                            files.add(fileUri)
                        }
                    }
                }
            }
            files
        }
    }

    suspend fun moveFiles(sourcePath: String, destinationPath: String, extension: String) {

        val source = Uri.parse((sourcePath))
        val destination = Uri.parse(destinationPath)
        val sourceFolder = DocumentFile.fromTreeUri(context, source) ?: DocumentFile.fromSingleUri(context, source)
        val destFolder = DocumentFile.fromTreeUri(context, destination) ?: DocumentFile.fromSingleUri(context, destination)

        if (sourceFolder == null || !sourceFolder.exists() || !sourceFolder.isDirectory) {
            throw Exception("Source folder does not exist or is not accessible. ${sourceFolder?.uri}")

        }

        if (destFolder == null || !destFolder.exists() || !destFolder.isDirectory) {
            throw Exception("Destination folder does not exist or is not accessible.")
        }
        // Open an InputStream to read the content of the original file

            val contentResolver = context.contentResolver
            var counter = 0
            sourceFolder.listFiles().forEach { sourceFile ->
println("file #$counter ${sourceFile.uri}")
                sourceFile?.let { file ->
                    if (file.name?.endsWith(extension) == true) {
                        counter++

                        println("found ${file.uri} !")
                        val newFile = destFolder.createFile(file.type ?: "*/*", file.name!!)
                            ?: throw Exception("Failed to copy file in the destination file.")

                        contentResolver.openInputStream(file.uri)?.use { inputStream ->
                            // Open an OutputStream to write the content of the original file to the new file
                            contentResolver.openOutputStream(newFile.uri)
                                ?.use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                        }
                        // Delete the original file
                        if (!file.delete()) {
                            throw IOException("Failed to delete the original file.")
                        }
                    }
                }
            }

            if (counter == 0)  {throw NoFileFoundException("No file found with $extension extension.")}


    }
}


private fun Uri.isInDirectory(uriToSearchIn: Uri): Boolean {
    println("your uri to serch $uriToSearchIn")
    return true
}

data class NoFileFoundException(val errorMessage: String) : Exception(errorMessage)
