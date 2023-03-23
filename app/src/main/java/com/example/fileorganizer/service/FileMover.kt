package com.example.fileorganizer.service

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilenameFilter

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

    suspend fun moveFile(sourcePath: String, destinationPath: String, extension: String) {

        val source = Uri.parse(sourcePath)
        val destination = Uri.parse(destinationPath)
        val sourceFolder = DocumentFile.fromTreeUri(context, source)
        val destFolder = DocumentFile.fromTreeUri(context, destination)
        withContext(Dispatchers.Default) {
            val contentResolver = context.contentResolver
            sourceFolder?.listFiles()?.forEach { file ->
                if (file != null && file.name?.endsWith(extension) == true) {
                    println("found ${file} !")
                    val newFile = destFolder?.createFile(file.type ?: "*/*", file.name!!)

//                    destFolder.createFile("*/*", file.name!!).let {
////                        file.delxete()
//                    }
                    withContext(Dispatchers.IO){ // Open an InputStream to read the content of the original file
                        contentResolver.openInputStream(file.uri)?.use { inputStream ->
                            // Open an OutputStream to write the content of the original file to the new file
                            contentResolver.openOutputStream(newFile?.uri ?: return@use)
                                ?.use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                        }
                    }
                }
                file.delete()
            }


//            val contentResolver = context.contentResolver
//
//            val sourceParcelFileDescriptor =
//                contentResolver.openFileDescriptor(sourceUri!!.uri, "r", null)
//            val sourceFileDescriptor = sourceParcelFileDescriptor?.fileDescriptor
//            val sourceFile = FileInputStream(sourceFileDescriptor).channel
//
//            val destinationFileName = sourceUri?.name
//            val destinationFileUri = Uri.withAppendedPath(destinationUri?.uri, destinationFileName)
//            val destinationParcelFileDescriptor =
//                contentResolver.openFileDescriptor(destinationFileUri, "w", null)
//            val destinationFileDescriptor = destinationParcelFileDescriptor?.fileDescriptor
//            val destinationFile = FileOutputStream(destinationFileDescriptor).channel
//
//            destinationFile.transferFrom(sourceFile, 0, sourceFile.size())
//
//            sourceFile.close()
//            sourceParcelFileDescriptor?.close()
//            destinationFile

//    fun moveFilesEndingWith(extension: String, sourcePath: Uri, destinationPath: Uri) {
//        if (extension.isNotEmpty()) {
//            try {
//                val sourceDir = File(sourcePath)
//                val destinationDir = File(destinationPath)
//
//                val filesToMove = sourceDir.listFiles()?.mapNotNull { file ->
//                    if (file.isFile && file.name?.substringAfterLast(".") == (extension)) file else null
//                } ?: emptyList()
//                val doc = DocumentFile.fromTreeUri(context, sourceDir.toUri())
//                println("doc $doc")
//                println("frdy ${destinationDir.listFiles()}")
//                println("files to move ${filesToMove}")
//
////                if (filesToMove.isNotEmpty()) {
////                    for (file in filesToMove) {
////                        println(file)
////                    }
//////                        Log.d("File Mover Service", "Moving file ${file.name}")
//////
//////                        val success = file.renameTo(File(destinationDir, file.name))
//////                        if (success) {
//////                            Log.d(
//////                                "File Mover Service",
//////                                "File ${file.name} moved to $destinationDir"
//////                            )
//////                        } else {
//////                            Log.d("File Mover Service", "Failed to move file ${file.name}")
//////                        }
//////                    }
////                } else {
////                    Log.d("File Mover Service", "No file ending with $extension in $sourceDir")
////                }
//            } catch (
//                e: Exception
//            ) {
//                Log.e("File Mover Service", "Error happened :${e.message}")
//            }
//        }
//    }
        }
    }
}

private fun Uri.isInDirectory(uriToSearchIn: Uri): Boolean {
    println("your uri to serch $uriToSearchIn")
    return true
}
