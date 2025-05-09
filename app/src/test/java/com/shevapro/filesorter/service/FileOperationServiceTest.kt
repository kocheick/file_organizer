package com.shevapro.filesorter.service

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.shevapro.filesorter.model.TaskStats
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FileOperationServiceTest {

    private lateinit var fileOperationService: FileOperationService
    private lateinit var statsService: StatsService
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var sourceUri: Uri
    private lateinit var destinationUri: Uri
    private lateinit var sourceFolder: DocumentFile
    private lateinit var destinationFolder: DocumentFile

    @Before
    fun setup() {
        statsService = mockk(relaxed = true)
        fileOperationService = FileOperationService(statsService)

        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        sourceUri = mockk<Uri>()
        destinationUri = mockk<Uri>()
        sourceFolder = mockk<DocumentFile>()
        destinationFolder = mockk<DocumentFile>()

        // Mock static methods
        mockkStatic(DocumentFile::class)
        mockkStatic(Uri::class)

        // Setup common mocks
        every { context.contentResolver } returns contentResolver
        every { Uri.parse(any()) } returnsArgument 0

        every { sourceUri.scheme } returns "content"
        every { destinationUri.scheme } returns "content"

        every { DocumentFile.fromTreeUri(context, sourceUri) } returns sourceFolder
        every { DocumentFile.fromTreeUri(context, destinationUri) } returns destinationFolder
    }

    @Test
    fun `getFilesExtensionsForFolder should return list of extensions`() {
        // Given
        val path = "content://path/to/folder"
        val file1 = mockk<DocumentFile>()
        val file2 = mockk<DocumentFile>()
        val file3 = mockk<DocumentFile>()

        every { file1.isFile } returns true
        every { file2.isFile } returns true
        every { file3.isFile } returns false

        every { file1.name } returns "document.pdf"
        every { file2.name } returns "image.jpg"

        every { sourceFolder.listFiles() } returns arrayOf(file1, file2, file3)

        // When
        val result = fileOperationService.getFilesExtensionsForFolder(path, context)

        // Then
        assertEquals(2, result.size)
        assertEquals(listOf("pdf", "jpg"), result)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `moveFilesByType should move files with matching extension`() = runTest {
        // Given
        val source = "content://path/to/source"
        val destination = "content://path/to/destination"
        val extension = "pdf"

        val file1 = mockk<DocumentFile>(relaxed = true)
        val file2 = mockk<DocumentFile>(relaxed = true)
        val file3 = mockk<DocumentFile>(relaxed = true)

        every { file1.isFile } returns true
        every { file2.isFile } returns true
        every { file3.isFile } returns true

        every { file1.name } returns "document.pdf"
        every { file2.name } returns "image.jpg"
        every { file3.name } returns "another.pdf"

        every { file1.type } returns "application/pdf"
        every { file3.type } returns "application/pdf"

        every { file1.uri } returns mockk(relaxed = true)
        every { file3.uri } returns mockk(relaxed = true)

        every { sourceFolder.listFiles() } returns arrayOf(file1, file2, file3)

        val destFile1 = mockk<DocumentFile>(relaxed = true)
        val destFile2 = mockk<DocumentFile>(relaxed = true)

        every { destinationFolder.createFile("application/pdf", "document.pdf") } returns destFile1
        every { destinationFolder.createFile("application/pdf", "another.pdf") } returns destFile2

        val inputStream1 = ByteArrayInputStream("test content 1".toByteArray())
        val inputStream2 = ByteArrayInputStream("test content 2".toByteArray())
        val outputStream1 = ByteArrayOutputStream()
        val outputStream2 = ByteArrayOutputStream()

        every { contentResolver.openInputStream(file1.uri) } returns inputStream1
        every { contentResolver.openInputStream(file3.uri) } returns inputStream2
        every { contentResolver.openOutputStream(destFile1.uri) } returns outputStream1
        every { contentResolver.openOutputStream(destFile2.uri) } returns outputStream2

        every { file1.delete() } returns true
        every { file3.delete() } returns true

        val progressUpdates = mutableListOf<TaskStats>()
        val progressCallback: (TaskStats) -> Unit = { progressUpdates.add(it) }

        // When
        fileOperationService.moveFilesByType(source, destination, extension, context, progressCallback)

        // Then
        coVerify { statsService.logTaskDetails(source, destination, extension, 2) }
        assertEquals(2, progressUpdates.size)
        assertEquals("document.pdf", progressUpdates[0].currentFileName)
        assertEquals("another.pdf", progressUpdates[1].currentFileName)
        assertEquals(2, progressUpdates[1].totalFiles)
    }

    @Test(expected = Exception::class)
    fun `moveFilesByType should throw exception when source folder is not accessible`() = runTest {
        // Given
        val source = "content://path/to/source"
        val destination = "content://path/to/destination"
        val extension = "pdf"

        every { DocumentFile.fromTreeUri(context, any()) } returns null

        // When/Then
        fileOperationService.moveFilesByType(source, destination, extension, context)
    }

    @Test(expected = Exception::class)
    fun `getFilesExtensionsForFolder should throw exception when folder is not accessible`() {
        // Given
        val path = "content://path/to/folder"

        every { DocumentFile.fromTreeUri(context, any()) } returns null

        // When/Then
        fileOperationService.getFilesExtensionsForFolder(path, context)
    }
}
