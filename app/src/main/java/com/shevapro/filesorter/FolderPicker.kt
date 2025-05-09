package com.shevapro.filesorter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.developer.filepicker.controller.DialogSelectionListener
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import com.shevapro.filesorter.ui.getActivity
import java.io.File

/**
 * A utility class for picking folders using TutorialsAndroid FilePicker library.
 * This provides a better user experience by showing both files and folders in the picker.
 */
object FolderPicker {

    /**
     * Shows a folder picker dialog and returns the selected folder's URI.
     *
     * @param context The context to use for showing the dialog
     * @param initialPath The initial path to show in the dialog (optional)
     * @param onFolderSelected Callback that receives the selected folder's URI
     */
    fun showFolderPicker(
        context: Context,
        initialPath: String? = null,
        onFolderSelected: (Uri) -> Unit
    ) {
        // Get the activity from context
        val activity = context.getActivity()
        if (activity == null) {
            // Cannot show picker without activity
            return
        }

        try {
            // Use the TutorialsAndroid FilePicker
            showTutorialsAndroidFilePicker(context, initialPath, onFolderSelected)
        } catch (e: Exception) {
            // If there's an error with the file picker, fall back to SAF
            android.util.Log.e("FolderPicker", "TutorialsAndroid FilePicker failed", e)
            
            fallbackDirectoryPicker(context, onFolderSelected)
        }
    }
    
    /**
     * Shows a folder picker dialog using the TutorialsAndroid FilePicker library.
     * This allows users to see files inside folders when selecting folders.
     *
     * @param context The context to use for showing the dialog
     * @param initialPath The initial path to show in the dialog (optional)
     * @param onFolderSelected Callback that receives the selected folder's URI
     */
    private fun showTutorialsAndroidFilePicker(
        context: Context,
        initialPath: String? = null,
        onFolderSelected: (Uri) -> Unit
    ) {
        val activity = context.getActivity() ?: return
        
        val properties = DialogProperties()
        properties.selection_mode = DialogConfigs.SINGLE_MODE
        properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT

        // Set root directory - if initialPath is provided, use it, otherwise use default
        
        properties.root = File(DialogConfigs.DEFAULT_DIR)
        properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
        properties.offset =      File(  (DialogConfigs.DEFAULT_DIR + initialPath?.substringAfter(DialogConfigs.DEFAULT_DIR))
        )
        properties.extensions = null // No extension filter for directories
        properties.show_hidden_files = false
        
        val dialog = FilePickerDialog(activity, properties)
        dialog.setTitle("Select Folder")
        
        dialog.setDialogSelectionListener(object : DialogSelectionListener {
            override fun onSelectedFilePaths(files: Array<String>) {
                if (files.isNotEmpty()) {
                    val selectedPathString = files[0]
                    val selectedFile = File(selectedPathString)
                    val folderPath = if (selectedFile.isDirectory) {
                        selectedFile
                    } else {
                        selectedFile.parentFile ?: selectedFile // Fallback to selectedFile if parent is null
                    }
                    onFolderSelected(folderPath.toUri())
                }
            }
        })
        
        // Check for permission before showing the dialog
        if (hasStoragePermission(activity)) {
            dialog.show()
        } else {
            requestStoragePermissions(activity, context)
        }
    }

    /**
     * Check if the app has the necessary storage permissions based on Android version
     */
    private fun hasStoragePermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, check for MANAGE_EXTERNAL_STORAGE
            Environment.isExternalStorageManager()
        } else {
            // For older versions, check for READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Request all necessary storage permissions based on Android version
     */
    private fun requestStoragePermissions(activity: Activity, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, request MANAGE_EXTERNAL_STORAGE through settings
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)

                Toast.makeText(
                    context,
                    "Please grant 'All files access' to browse files",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                // Fallback if specific intent is not available
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            }
        } else {
            // For older versions, request READ_EXTERNAL_STORAGE permission directly
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT
            )
            
            Toast.makeText(
                context,
                "Storage permission is required to browse files",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Uses the Android Storage Access Framework (SAF) for folder selection.
     * This acts as a fallback when the FilePicker fails.
     *
     * @param context The context to use for showing the dialog
     * @param onFolderSelected Callback that receives the selected folder's URI
     */
    private fun fallbackDirectoryPicker(context: Context, onFolderSelected: (Uri) -> Unit) {
        try {
            // Get the activity from context
            val activity = context.getActivity() ?: return

            // Create an intent to open the default Android file picker
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            // Start the activity for result
            activity.startActivityForResult(intent, 9999)

            // Note: This is a simplified implementation that doesn't handle the result.
            // In a real implementation, you would need to register an activity result callback.
            // For now, we're just showing the picker to avoid the crash.

            // Inform the user about the fallback
            Toast.makeText(
                context,
                "Using system file picker due to an issue with the FilePicker",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            // If even the fallback fails, log the error
            android.util.Log.e("FolderPicker", "Fallback directory picker failed", e)

            // Inform the user about the failure
            Toast.makeText(
                context,
                "Unable to open file picker. Please try again later.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Shows a folder picker dialog and returns the selected folder's URI as a string.
     * This is a convenience function for use with the existing code that expects a string URI.
     *
     * @param context The context to use for showing the dialog
     * @param onFolderSelected Callback that receives the selected folder's URI as a string
     */
    fun showFolderPickerDialog(
        context: Context,
        onFolderSelected: (String) -> Unit
    ) {
        showFolderPicker(context) { uri ->
            onFolderSelected(uri.toString())
        }
    }
}