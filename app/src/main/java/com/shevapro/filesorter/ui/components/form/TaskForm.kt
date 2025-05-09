@file:OptIn(ExperimentalPermissionsApi::class)

package com.shevapro.filesorter.ui.components.form

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Environment
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shevapro.filesorter.FolderPicker
import com.shevapro.filesorter.R
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.Utility.grantUrisPermissions
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.ui.components.PermissionAction
import com.shevapro.filesorter.ui.components.StoragePermissionDialog

/**
 * Unified task form for both adding and editing tasks
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskFormEditor(
    taskToBeEdited: UITaskRecord,
    onSourceUriChange: (String) -> Unit,
    onDestinationUriChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    extensionLabelText: String,
    typesFromSelectedSource: List<String> = emptyList()
) {
    val src = Uri.decode(taskToBeEdited.source)
    val dest = Uri.decode(taskToBeEdited.destination)

    val sourcePath = remember { mutableStateOf(src) }
    val destPath = remember { mutableStateOf(dest) }

    val formattedSource = Utility.formatUriToUIString(
        Uri.decode(sourcePath.value) ?: stringResource(R.string.no_folder_selected)
    )
    val formattedDestination = Utility.formatUriToUIString(
        Uri.decode(destPath.value) ?: stringResource(R.string.no_folder_selected)
    )

    val typeTextState = remember { mutableStateOf(TextFieldValue(taskToBeEdited.extension)) }

    // New toggle state for showing manual file type input
    var showManualTypeInput by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Define required permissions based on Android version
    val requiredPermissions = when {
        Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        }
        Build.VERSION.SDK_INT >= VERSION_CODES.R -> {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        else -> {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    // Create permission state at parent level
    val permissionsState = rememberMultiplePermissionsState(requiredPermissions) { permissionsResult ->
        val allGranted = permissionsResult.values.all { it }
        if (!allGranted) {
            showPermissionDialog = true
        }
    }

    // Check for all files access permission (Android 11+)
    val allFilesAccessGranted = if (Build.VERSION.SDK_INT >= VERSION_CODES.R) {
        remember {
            mutableStateOf(Environment.isExternalStorageManager())
        }
    } else {
        remember { mutableStateOf(true) }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.R) {
            allFilesAccessGranted.value = Environment.isExternalStorageManager()
        }
    }

    // Permission check logic
    val needsAllFilesAccess = Build.VERSION.SDK_INT >= VERSION_CODES.R && !allFilesAccessGranted.value
    val needsRuntimePermissions = !permissionsState.allPermissionsGranted

    // Function to launch folder picker if permissions are granted
    val launchFolderPicker = { isSource: Boolean ->
        if ((!needsAllFilesAccess || Build.VERSION.SDK_INT < VERSION_CODES.R) &&
            (!needsRuntimePermissions || Build.VERSION.SDK_INT >= VERSION_CODES.R && allFilesAccessGranted.value)
        ) {
            // Permissions granted, launch folder picker
            FolderPicker.showFolderPicker(context, if (isSource) sourcePath.value else destPath.value) { uri ->
                grantUrisPermissions(uri, context = context)
                if (isSource) {
                    sourcePath.value = uri.toString()
                     onSourceUriChange(uri.toString())
                } else {
                    destPath.value = uri.toString()
                    onDestinationUriChange(uri.toString())
                }
            }
        } else {
            // Permissions not granted, show dialog
            showPermissionDialog = true
        }
    }

    // Show permission dialog if needed
    if (showPermissionDialog) {
        StoragePermissionDialog(
            onDismiss = {
                showPermissionDialog = false
            },
            onPermissionAction = { action ->
                when (action) {
                    PermissionAction.REQUEST_STORAGE_PERMISSION -> {
                        permissionsState.launchMultiplePermissionRequest()
                    }

                    PermissionAction.REQUEST_ALL_FILES_ACCESS -> {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:${context.packageName}")
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                        }
                        // Check in a version-safe way
                        if (Build.VERSION.SDK_INT >= VERSION_CODES.R) {
                            allFilesAccessGranted.value = Environment.isExternalStorageManager()
                        }
                    }

                    PermissionAction.OPEN_SETTINGS -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:${context.packageName}")
                        context.startActivity(intent)
                    }
                }
                showPermissionDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            maxLines = 1,
            text = stringResource(R.string.selectFolders),
            fontWeight = FontWeight.SemiBold
        )
        // PICK SOURCE BUTTON
        FolderPickerButton(
            text = stringResource(R.string.source),
            path = formattedSource.ifBlank { stringResource(R.string.no_folder_selected) },
            onClick = { launchFolderPicker(true) }
        )

        // Show file types from source if available
        AnimatedVisibility(typesFromSelectedSource.isNotEmpty()) {
            Column {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    maxLines = 1,
                    text = "Pick the type of file you wish to move",
                    fontWeight = FontWeight.Medium
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .heightIn(max = 120.dp),
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    typesFromSelectedSource.forEach { type ->
                        val borderBackground =
                            if (type == taskToBeEdited.extension) colorResource(id = R.color.fiery_rose) else Color.Unspecified
                        val background =
                            if (type == taskToBeEdited.extension) colorResource(id = R.color.fiery_rose).copy(0.3f) else colorResource(
                                id = R.color.fiery_rose
                            ).copy(0.2f)
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                                .defaultMinSize(minWidth = 40.dp)
                                .background(
                                    background,
                                    RoundedCornerShape(16.dp)
                                )
                                .border(2.dp, borderBackground, RoundedCornerShape(16.dp))
                                .padding(8.dp)
                                .clickable {
                                    onTypeChange(type)
                                    typeTextState.value = TextFieldValue(type)
                                }
                        ) {
                            Text(
                                type,
                                fontWeight = if (type == taskToBeEdited.extension) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Toggle to show manual file type input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = showManualTypeInput,
                onCheckedChange = { showManualTypeInput = it }
            )
            Text(
                text = "Enter file type manually",
                modifier = Modifier.clickable { showManualTypeInput = !showManualTypeInput }
            )
        }

        // Manual file type input, only visible when toggle is on
        AnimatedVisibility(visible = showManualTypeInput) {
            Column {
                Text(
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    text = extensionLabelText,
                    modifier = Modifier.padding(top = 8.dp)
                )
                TextField(
                    singleLine = true,
                    maxLines = 1,
                    label = { Text(stringResource(R.string.enter_file_type), color = Color.DarkGray) },
                    value = typeTextState.value,
                    onValueChange = {
                        typeTextState.value = it
                        onTypeChange(it.text)
                    },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .width(180.dp)
                )
            }
        }

        // SWAP BUTTON
        SwapPathsButton(
            modifier = Modifier.align(Alignment.End),
            isActive = !sourcePath.value.isNullOrEmpty() or !destPath.value.isNullOrEmpty(),
            onClick = {
                val tempSource = sourcePath.value
                sourcePath.value = destPath.value
                destPath.value = tempSource
                onSourceUriChange(sourcePath.value)
                onDestinationUriChange(destPath.value)
            }
        )

        // PICK DEST BUTTON
        FolderPickerButton(
            text = stringResource(R.string.destination),
            path = formattedDestination.ifBlank { stringResource(R.string.no_folder_selected) },
            onClick = { launchFolderPicker(false) }
        )
    }
}
