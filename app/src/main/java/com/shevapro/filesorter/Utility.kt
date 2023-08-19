package com.shevapro.filesorter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.MutableState
import com.shevapro.filesorter.ui.getActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object Utility {
    const val MATCH_2_CHARS_AFTER_SIGN = "%[\\dA-Za-z]{2}"
    fun swapPaths(sourcePath: MutableState<String>, destPath: MutableState<String>) {
        val temp = sourcePath.value
        sourcePath.value = destPath.value
        destPath.value = temp

    }

    fun formatUriToUIString(uri: String): String = uri.substringAfterLast(":").replace("/", " > ")

    val emptyInteractionSource = object : MutableInteractionSource {
        override val interactions: Flow<Interaction>
            get() = emptyFlow()

        override suspend fun emit(interaction: Interaction) {
        }

        override fun tryEmit(interaction: Interaction): Boolean {
            return false
        }

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
            contentResolver?.takePersistableUriPermission(sourceFileUri, takeFlags)

        }
    }

    class OpenDirectoryTree : ActivityResultContracts.OpenDocumentTree() {
        override fun createIntent(context: Context, input: Uri?): Intent {
            super.createIntent(context, input)

            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE,).apply {
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