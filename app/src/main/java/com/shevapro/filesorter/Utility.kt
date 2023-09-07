package com.shevapro.filesorter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document.MIME_TYPE_DIR
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.shevapro.filesorter.ui.getActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.security.Permission

object Utility {
    const val MATCH_2_CHARS_AFTER_SIGN = "%[\\dA-Za-z]{2}"
    const val AVERAGE_MANUAL_FILE_MOVE_PER_SECOND = 0.42
    fun swapPaths(sourcePath: MutableState<String>, destPath: MutableState<String>) {
        val temp = sourcePath.value
        sourcePath.value = destPath.value
        destPath.value = temp

    }

   private val isRoot: (String?) -> Boolean =
        { path -> if (path == null) false else Uri.parse(path).lastPathSegment.equals("primary:") }

    private fun formatUri(uri: String): String = uri.substringAfterLast(":").replace("/", " > ")
    fun formatUriToUIString(uri: String): String  {
        val formatedUri = formatUri(
            Uri.decode(uri)
        )
        val characterCount = formatedUri.length

        return if (isRoot(uri))  "Primary Root"
         else if (characterCount > 50) formatedUri.substringBefore(">") + ">>>" + formatedUri.substringAfterLast(
            ">" ) else formatedUri
    }

    val emptyInteractionSource = object : MutableInteractionSource {
        override val interactions: Flow<Interaction>
            get() = emptyFlow()

        override suspend fun emit(interaction: Interaction) {
        }

        override fun tryEmit(interaction: Interaction): Boolean {
            return false
        }

    }

    // Check if the app has permission to write to a specific URI
    fun hasPermission(context: Context): Boolean {
        return context.packageManager.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.packageName
            )== PackageManager.PERMISSION_GRANTED




    }

    fun grantUrisPermissions(
        sourceFileUri: Uri = Uri.EMPTY,
        destinationFolderUri: Uri = Uri.EMPTY,
        context: Context
    ) {

        if (sourceFileUri != Uri.EMPTY)  grantPermissionForUri(context, sourceFileUri)
        if (destinationFolderUri != Uri.EMPTY)  grantPermissionForUri(context, destinationFolderUri)
    }

    private fun grantPermissionForUri(
        context: Context,
        sourceFileUri: Uri,
    ) {
        val takeFlags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.getActivity()?.apply {
            grantUriPermission(context.packageName, sourceFileUri, takeFlags)
            contentResolver!!.takePersistableUriPermission(sourceFileUri, takeFlags)
            DocumentFile.fromTreeUri(context,sourceFileUri)!!.uri

        }
    }
    fun generateGoldenRatioColor(input: String): Long {
        val hash = input.hashCode()

        // Calculate the hue using the golden ratio / 2
        val goldenRatio = 0.618033988749895 / 2
        val hue = ((hash * goldenRatio) %0.8f).toFloat() // Keep hue value between 0 and 1
//        val hue = ((hash * goldenRatio) % 1.0f).toFloat() // Keep hue value between 0 and 1
        val saturation = 0.45f // Adjust as needed
        val lightness = 0.85f // Adjust as needed

        val color = Color.HSVToColor(floatArrayOf(hue * 360.0f, saturation, lightness))

        val red = Color.red(color).toLong()
        val green = Color.green(color).toLong()
        val blue = Color.blue(color).toLong()

        val alpha = 150L

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }
     fun generateColorFomInput(input: String): Long {
        val hash = input.hashCode()
        val hue = (hash and 0xFF) / 255.0f * 360.0f
        val saturation = 0.5f // Adjust as needed
        val lightness = 0.85f // Adjust as needed

//         val saturation = 0.7f // Adjust as needed
//         val lightness = 0.6f // Adjust as needed

        val color = Color.HSVToColor(floatArrayOf(hue, saturation, lightness))

        val red = Color.red(color).toLong()
        val green = Color.green(color).toLong()
        val blue = Color.blue(color).toLong()

        val alpha = 255L

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    open class OpenDirectory : ActivityResultContract<Uri?, Uri?>() {
        @CallSuper

        override fun createIntent(context: Context, input: Uri?): Intent {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//                    type = "*/*"
//                    addCategory(Intent.CATEGORY_OPENABLE)
//                    putExtra(Intent.EXTRA_MIME_TYPES, input)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    setDataAndType(input,MIME_TYPE_DIR)

                }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, input)
//            }

            return intent
        }

        final override fun getSynchronousResult(
            context: Context,
            input: Uri?
        ): SynchronousResult<Uri?>? = null

        final override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            println(resultCode)
            return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
        }
    }
    class OpenDirectoryTree : ActivityResultContracts.OpenDocumentTree() {
        override fun createIntent(context: Context, input: Uri?): Intent {
            super.createIntent(context, input)

            val intent =
                (if(input == Uri.EMPTY )Intent(Intent.ACTION_OPEN_DOCUMENT_TREE) else Intent(Intent.ACTION_OPEN_DOCUMENT_TREE,input) )
                    .apply {
//                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, input)
            }





            return intent
        }
    }


}