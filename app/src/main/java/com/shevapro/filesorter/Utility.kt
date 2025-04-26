package com.shevapro.filesorter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document.MIME_TYPE_DIR
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
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
        // Handle null or empty URI
        if (uri.isNullOrEmpty()) return ""

        // Handle root path
        if (isRoot(uri)) return "Primary Root"

        val decodedUri = Uri.decode(uri)
        val formattedUri = formatUri(decodedUri)

        // Check if this is a top-level folder in storage/emulated/0
        if (decodedUri.contains("/storage/emulated/0/")) {
            val pathSegments = decodedUri.substringAfter("/storage/emulated/0/").split("/")

            // If it's a direct child of the root (like Download, DCIM, etc.)
            if (pathSegments.size == 1 || (pathSegments.size > 1 && pathSegments[1].isEmpty())) {
                return pathSegments[0]
            }

            // For nested folders, show the hierarchy
            return pathSegments.filter { it.isNotEmpty() }.joinToString(" > ")
        }

        // For content URIs or other formats
        val characterCount = formattedUri.length
        return if (characterCount > 50) 
            formattedUri.substringBefore(">") + ">>>" + formattedUri.substringAfterLast(">") 
        else 
            formattedUri
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+), check if we have MANAGE_EXTERNAL_STORAGE permission
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API 29), check if we have the legacy storage permission
            // or if the app has been granted access through SAF
            context.packageManager.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.packageName
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 9 and below, just check the WRITE_EXTERNAL_STORAGE permission
            context.packageManager.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.packageName
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Check if we need to show the storage permission rationale
    fun shouldShowStoragePermissionRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, we can't check rationale for MANAGE_EXTERNAL_STORAGE
            // so we'll always return true to show our custom rationale
            !Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10, check if we should show rationale for WRITE_EXTERNAL_STORAGE
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            // For Android 9 and below, check if we should show rationale for WRITE_EXTERNAL_STORAGE
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    // Open the storage permission settings
    fun openStoragePermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, open the MANAGE_EXTERNAL_STORAGE permission settings
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            } catch (e: Exception) {
                // If the specific intent is not available, fall back to the general storage settings
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10, open the app's settings page
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        } else {
            // For Android 9 and below, open the app's settings page
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
    }

    fun grantUrisPermissions(
        sourceFileUri: Uri = Uri.EMPTY,
        destinationFolderUri: Uri = Uri.EMPTY,
        context: Context
    ) {
        try {
            if (sourceFileUri != Uri.EMPTY) grantPermissionForUri(context, sourceFileUri)
            if (destinationFolderUri != Uri.EMPTY) grantPermissionForUri(context, destinationFolderUri)
        } catch (e: com.shevapro.filesorter.model.PermissionExceptionForUri) {
            // Log the error
            android.util.Log.e("Utility", "Permission error for URI: ${e.uri}", e)

            // Error message is already shown in grantPermissionForUri
            // No need to show another message here
        }
    }

    fun grantPermissionForUri(
        context: Context,
        sourceFileUri: Uri,
    ) {
        val takeFlags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.getActivity()?.apply {
            try {
                // Only grant URI permission if it's not a file URI
                grantUriPermission(context.packageName, sourceFileUri, takeFlags)

                // Only take persistable URI permission for content URIs
                if (sourceFileUri.scheme == "content") {
                    contentResolver!!.takePersistableUriPermission(sourceFileUri, takeFlags)
                    DocumentFile.fromTreeUri(context, sourceFileUri)?.uri
                } else {
                    // For file URIs, we can't take persistable permissions
                    // Log a message for debugging
                    android.util.Log.d("Utility", "Skipping persistable permission for file URI: $sourceFileUri")
                }
            } catch (e: SecurityException) {
                // Log the error
                android.util.Log.e("Utility", "Error granting permission for URI: $sourceFileUri", e)

                // Show a toast message to the user
                android.widget.Toast.makeText(
                    context,
                    "Cannot access this folder. Please select a different folder.",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                // Rethrow as a custom exception that can be caught and handled by the app
                throw com.shevapro.filesorter.model.PermissionExceptionForUri(
                    sourceFileUri,
                    "Cannot access this folder: ${e.message}"
                )
            }
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
