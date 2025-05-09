package com.shevapro.filesorter.service

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ValidationServiceTest {

    private lateinit var validationService: ValidationService
    private lateinit var context: Context
    private lateinit var sourceUri: Uri
    private lateinit var destinationUri: Uri
    private lateinit var sourceDocumentFile: DocumentFile
    private lateinit var destinationDocumentFile: DocumentFile

    @Before
    fun setup() {
        validationService = ValidationService()
        context = mockk(relaxed = true)
        sourceUri = mockk<Uri>()
        destinationUri = mockk<Uri>()
        sourceDocumentFile = mockk<DocumentFile>()
        destinationDocumentFile = mockk<DocumentFile>()

        // Mock static methods
        mockkStatic(DocumentFile::class)
        
        // Setup common mocks
        every { sourceUri.scheme } returns "content"
        every { destinationUri.scheme } returns "content"
        
        every { DocumentFile.fromTreeUri(context, sourceUri) } returns sourceDocumentFile
        every { DocumentFile.fromTreeUri(context, destinationUri) } returns destinationDocumentFile
        
        // Default behavior for document files
        every { sourceDocumentFile.exists() } returns true
        every { sourceDocumentFile.isDirectory } returns false
        every { sourceDocumentFile.canRead() } returns true
        every { sourceDocumentFile.length() } returns 1000L
        
        every { destinationDocumentFile.exists() } returns true
        every { destinationDocumentFile.isDirectory } returns true
        every { destinationDocumentFile.canWrite() } returns true
        every { destinationDocumentFile.listFiles() } returns emptyArray()
    }

    @Test
    fun `validateFileOperation should return true when all validations pass`() {
        // Given
        // Default setup is valid
        
        // When/Then
        assertTrue(validationService.validateFileOperation(sourceUri, destinationUri, context))
    }

    @Test(expected = IOException::class)
    fun `validateFileOperation should throw exception when source does not exist`() {
        // Given
        every { sourceDocumentFile.exists() } returns false
        
        // When/Then
        validationService.validateFileOperation(sourceUri, destinationUri, context)
    }

    @Test(expected = IOException::class)
    fun `validateFileOperation should throw exception when destination does not exist`() {
        // Given
        every { destinationDocumentFile.exists() } returns false
        
        // When/Then
        validationService.validateFileOperation(sourceUri, destinationUri, context)
    }

    @Test(expected = IOException::class)
    fun `validateFileOperation should throw exception when destination is not a directory`() {
        // Given
        every { destinationDocumentFile.isDirectory } returns false
        
        // When/Then
        validationService.validateFileOperation(sourceUri, destinationUri, context)
    }

    @Test(expected = IOException::class)
    fun `validateFileOperation should throw exception when source cannot be read`() {
        // Given
        every { sourceDocumentFile.canRead() } returns false
        
        // When/Then
        validationService.validateFileOperation(sourceUri, destinationUri, context)
    }

    @Test(expected = IOException::class)
    fun `validateFileOperation should throw exception when destination cannot be written to`() {
        // Given
        every { destinationDocumentFile.canWrite() } returns false
        
        // When/Then
        validationService.validateFileOperation(sourceUri, destinationUri, context)
    }

    @Test
    fun `isValidFileName should return true for valid file names`() {
        // Given
        val validNames = listOf(
            "file.txt",
            "my_document.pdf",
            "image-123.jpg",
            "document with spaces.docx"
        )
        
        // When/Then
        validNames.forEach { name ->
            assertTrue("Expected $name to be valid", validationService.isValidFileName(name))
        }
    }

    @Test
    fun `isValidFileName should return false for invalid file names`() {
        // Given
        val invalidNames = listOf(
            "",
            "file/with/slashes.txt",
            "file:with:colons.pdf",
            "file*with*stars.jpg",
            "file?with?questions.docx",
            "file\"with\"quotes.txt",
            "file<with>brackets.pdf",
            "file|with|pipes.jpg"
        )
        
        // When/Then
        invalidNames.forEach { name ->
            assertFalse("Expected $name to be invalid", validationService.isValidFileName(name))
        }
    }

    @Test
    fun `hasNamingConflict should return true when file with same name exists in destination`() {
        // Given
        val fileName = "existing.txt"
        val existingFile = mockk<DocumentFile>()
        every { existingFile.name } returns fileName
        every { destinationDocumentFile.listFiles() } returns arrayOf(existingFile)
        
        // When/Then
        assertTrue(validationService.hasNamingConflict(fileName, destinationUri, context))
    }

    @Test
    fun `hasNamingConflict should return false when no file with same name exists in destination`() {
        // Given
        val fileName = "new.txt"
        val existingFile = mockk<DocumentFile>()
        every { existingFile.name } returns "different.txt"
        every { destinationDocumentFile.listFiles() } returns arrayOf(existingFile)
        
        // When/Then
        assertFalse(validationService.hasNamingConflict(fileName, destinationUri, context))
    }
}