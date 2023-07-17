package com.example.fileorganizer

import android.Manifest
import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.fileorganizer.model.UITaskRecord
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState


@Composable
fun AddTaskDialog(
    item: UITaskRecord,
    onAddItem: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
    onSaveUpdates: (UITaskRecord?) -> Unit
) {


    var extension = item.extension

    var source = item.source

    var destination = item.destination

    AlertDialog(
        modifier = Modifier
            .wrapContentSize()
            .background(colorResource(id = R.color.fiery_rose)),
        onDismissRequest = {
            onDismiss()
            onSaveUpdates(
                item.copy(
                    extension = extension, source = source, destination
                    = destination, id = item.id
                )
            )
        },
        title = { Text(stringResource(R.string.add_new_item).uppercase()) },
        //alert dialog content/body goes in here
        text = {
            TaskForm(
                taskToBeEdited = item,
                onDestinationUriChange = {
                    destination = it
                    onSaveUpdates(
                        item.copy(
                            extension = extension, source = source, destination
                            = it, id = item.id
                        )
                    )
                },
                onSourceUriChange = {
                    source = it
                    onSaveUpdates(
                        item.copy(
                            extension = extension, source = it, destination
                            = destination, id = item.id
                        )
                    )
                },
                onTypeChange = {
                    extension = it
                    onSaveUpdates(
                        item.copy(
                            extension = it, source = source, destination
                            = destination, id = item.id
                        )
                    )
                },
                extensionLabelText =
                stringResource(
                    id = R.string.enter_file_extension_or_type_with,
                    extension.uppercase()
                )

            )
        },
        buttons = {
            CancelAndAddButtons(
                onDismiss = {
                    onSaveUpdates(null)
                    onDismiss()
                },
                onAddClick = {
                    onAddItem(extension, source, destination)


                }
            )
        }
    )
}


@Composable
fun EditTaskDialog(
    itemToBeEdited: UITaskRecord,
    onUpdateItem: (UITaskRecord) -> Unit,
    onSaveUpdates: (UITaskRecord?) -> Unit,
    onDismiss: () -> Unit
) {

    var extension = itemToBeEdited.extension
    var source = itemToBeEdited.extension
    var destination = itemToBeEdited.extension


    AlertDialog(onDismissRequest = {
        onDismiss()
    },
        title = { Text("Edit item".uppercase()) },
        //alert dialog content/body goes in here
        text = {
            TaskForm(
                onSourceUriChange = {
                    source = it
                    onSaveUpdates(
                        itemToBeEdited.copy(
                            extension = extension, source = source,
                            destination
                            = destination,
                        )
                    )

                },
                onDestinationUriChange = {
                    destination = it
                    onSaveUpdates(
                        itemToBeEdited.copy(
                            extension = extension, source = source,
                            destination
                            = destination,
                        )
                    )
                },
                taskToBeEdited = itemToBeEdited,
                onTypeChange = {
                    extension = it
                    onSaveUpdates(
                        itemToBeEdited.copy(
                            extension = extension, source = source,
                            destination
                            = destination,
                        )
                    )
                },
                extensionLabelText = stringResource(
                    id = R.string.update_file_extension_or_type_current_is,
                    itemToBeEdited.extension.uppercase()
                )
            )
        },
        buttons = {
            CancelAndSaveButtons(onDismiss = {
                onDismiss()
            },
                onSaveUpdates = {
                    onUpdateItem(
                        itemToBeEdited.copy(
                            extension = extension,
                            source = source,
                            destination = destination
                        )
                    )

                })
        }
    )

}

@Composable
private fun CancelAndAddButtons(
    onDismiss: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(2.dp), horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = {
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = colorResource(R.color.jet),
                backgroundColor = Color.LightGray.copy(0.0f)
            )
        ) {
            Text(stringResource(id = R.string.cancel))
        }
        TextButton(
            onClick = {
                onAddClick()
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = colorResource(R.color.fiery_rose),
                backgroundColor = Color.LightGray.copy(0.0f)
            )
        ) {
            Text(stringResource(id = R.string.add))
        }
    }
}

@Composable
private fun CancelAndSaveButtons(
    onDismiss: () -> Unit,
    onSaveUpdates: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(2.dp), horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = {
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = colorResource(R.color.jet),
                backgroundColor = Color.LightGray.copy(0.0f)
            )
        ) {
            Text(stringResource(R.string.cancel))
        }
        TextButton(
            onClick = {
                onSaveUpdates()
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = colorResource(R.color.fiery_rose),
                backgroundColor = Color.LightGray.copy(0.0f)
            )
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

@Composable
fun MissingFieldDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.missing_field)) },
        text = { Text(text = message) },
        buttons = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.fiery_rose),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text(stringResource(R.string.ok_i_undertand))
                }
            }
        })
}

@Composable
fun RemovalDialog(item: UITaskRecord, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = {
            Text(
                stringResource(
                    id = R.string.remove_item_with_extension,
                    item.extension
                )
            )
        },
        text = { Text(text = stringResource(R.string.are_you_sure_to_remove_this_item)) },
        buttons = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    modifier = Modifier.padding(end = 4.dp),
                    onClick = {
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.fiery_rose),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
                TextButton(
                    onClick = {
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        backgroundColor = Color.Red.copy(0.8f)
                    )
                ) {
                    Text(stringResource(id = R.string.delete))
                }
            }
        })
}

@Composable
fun DirectoryPicker(show: Boolean, onDirectorySelected: (String?) -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {
                val intent: Intent? = result.data
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                }
                val uri = intent?.data

                uri?.let {

                    println("your URI path: ${it.path}")
                    println("your URI encoded path: ${it.encodedPath}")
                    println("your URI path fragments: ${it.pathSegments}")
                    println("your URI scheme: ${it.scheme}")
                    println("your URI authority: ${it.authority}")
                    println("your URI encoded authority: ${it.encodedAuthority}")
                    println("your URI full: ${Uri.decode(it.toString())}")
                    println("decode: ${Uri.decode(it.toString())}")
                    println("tostring : ${(it.toString())}")
                    onDirectorySelected(Uri.decode(it.toString()))
                }
            }
        }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TaskForm(
    taskToBeEdited: UITaskRecord? = null,
    onSourceUriChange: (String) -> Unit,
    onDestinationUriChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    extensionLabelText: String
) {
    val src = if (taskToBeEdited != null) Uri.decode(taskToBeEdited.source) else null
    val dest = if (taskToBeEdited != null) Uri.decode(taskToBeEdited.destination) else null

    val sourcePath = remember { mutableStateOf(src) }
    val destPath = remember { mutableStateOf(dest) }

    val isRoot: (String?) -> Boolean =
        { path -> if (path == null) false else Uri.parse(path).lastPathSegment.equals("primary:") }
    val context = LocalContext.current
    val permission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) MANAGE_EXTERNAL_STORAGE else Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
val permissioState = rememberPermissionState(permission = permission )


    val sourceDirectoryPickerLauncher = pickDirectory(context = context.getActivity()?: return, pickedUri = {
        sourcePath.value = it
    })
    val destinationDirectoryPickerLauncher = pickDirectory(context = context.getActivity() ?: return,pickedUri = {
        destPath.value = it
    })

    val formattedSource =
        if (isRoot(sourcePath.value)) "Primary Root" else Utility.formatUriToUIString(
            Uri.decode(sourcePath.value) ?: stringResource(R.string.no_folder_selected)
        )
    val formattedDestination =
        if (isRoot(destPath.value)) "Primary Root" else Utility.formatUriToUIString(
            Uri.decode(destPath.value )?: stringResource(R.string.no_folder_selected)
        )

    val typeTextState = remember { mutableStateOf(TextFieldValue(taskToBeEdited?.extension ?: "")) }




    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            text = extensionLabelText
        )
        TextField(
            singleLine = true, maxLines = 1,
            label = { Text(stringResource(R.string.enter_file_type)) },
            value = typeTextState.value,
            onValueChange = {
                typeTextState.value = it
                onTypeChange(it.text)
            }, modifier = Modifier
                .padding(bottom = 16.dp)
                .width(180.dp)
        )

        Text(
            maxLines = 1,
            text = stringResource(R.string.selectFolders),
            fontWeight = FontWeight.SemiBold
        )
// PICK SOURCE BUTTON

        FolderPickerButton(
            text = stringResource(R.string.source),
            path = formattedSource.ifBlank { stringResource(R.string.no_folder_selected) },
            onPick = {
                when(permissioState.status){
                    is PermissionStatus.Granted -> {
                        println("GRANTED")

                        sourceDirectoryPickerLauncher.launch(sourcePath.value?.toUri()
                        )
                    }
                    is PermissionStatus.Denied ->{
                        println("DENIED")
                        permissioState.launchPermissionRequest()
                    }
                }

//                checkAndRequestFileStoragePermission(context,android.Manifest.permission.MANAGE_DOCUMENTS,sourceDirectoryPickerLauncher)
//                checkAndRequestFileStoragePermission(context.getActivity()!!,Manifest.permission.WRITE_EXTERNAL_STORAGE,sourceDirectoryPickerLauncher)
            }

        )
        // SWAP BUTTON
        SwapPathsButton(
            modifier = Modifier.align(Alignment.End),
            isActive = !sourcePath.value.isNullOrEmpty() or !destPath.value.isNullOrEmpty(),
            onClick = {
                swipePath(
                    sourcePath as MutableState<String>,
                    destPath as MutableState<String>
                )
            }
        )

// PICK DEST BUTTON

        FolderPickerButton(
            text = stringResource(R.string.destination),
            path = formattedDestination.ifBlank { stringResource(R.string.no_folder_selected) },
            onPick = {
//                destinationDirectoryPickerLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
//                checkAndRequestFileStoragePermission(context.getActivity()!!,Manifest.permission.WRITE_EXTERNAL_STORAGE,destinationDirectoryPickerLauncher)
                when(permissioState.status){
                    is PermissionStatus.Granted -> {
                        destinationDirectoryPickerLauncher.launch(destPath.value?.toUri())
                    }
                    is PermissionStatus.Denied ->{
                        permissioState.launchPermissionRequest()
                    }
                }
            })

        sourcePath.value?.let { onSourceUriChange(it) }
        destPath.value?.let { onDestinationUriChange(it) }

        println("your task ${taskToBeEdited}")
//        val task = taskToBeEdited?.copy(
//            extension = typeText, source = sourcePath.value , destination = destPath.value
//        )?.toTaskRecord() ?: TaskRecord(typeText, sourcePath.value.toString(), destPath.value.toString(),0)
//        val uiTask = task.toUITaskRecord()
//        onTaskItemAdded(task)


    }

}

@Composable
private fun SwapPathsButton(
    modifier: Modifier, isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        enabled = isActive,
        onClick = { onClick() },
        modifier = modifier
            .wrapContentSize()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp)),

        ) {
        Column(
            modifier = Modifier.wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row {
                Icon(
                    Icons.Rounded.DoubleArrow, modifier = Modifier
                        .rotate(90F)
                        .wrapContentSize(),
                    contentDescription = stringResource(id = R.string.reverse_button)
                )
                Icon(
                    Icons.Rounded.DoubleArrow, modifier = Modifier
                        .rotate(-90F)
                        .wrapContentSize(),
                    contentDescription = stringResource(id = R.string.reverse_button)
                )
            }
            Text("swap paths", fontWeight = FontWeight.Light, fontSize = 10.sp)
        }
    }
}

@Composable
private fun FolderPickerButton(
    text: String,
    path: String,
    onPick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextButton(
            modifier = Modifier
                .defaultMinSize(minWidth = 110.dp)
                .padding(end = 8.dp),
            onClick = {
                onPick()
            },
            colors = ButtonDefaults
                .buttonColors(
                    backgroundColor = colorResource(R.color.fiery_rose),
                    contentColor = colorResource(R.color.white)
                )
        ) {
            Text(text, textAlign = TextAlign.Center)
        }

        Text(
            path, maxLines = 2
        )
    }
}

private fun swipePath(
    sourcePath: MutableState<String>,
    destPath: MutableState<String>
) {
    val oldSrc = sourcePath.value
    sourcePath.value = destPath.value
    destPath.value = oldSrc
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun pickDirectory(context: Activity, pickedUri: (String) -> Unit): ManagedActivityResultLauncher<Uri?, Uri?> {



    val launcher =
        rememberLauncherForActivityResult(contract = Utility.OpenDirectoryTree()) { result ->

//                    intent.data= Uri.parse(initialDirectory)
//                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    result?.let {
                        val takeFlags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or FLAG_GRANT_READ_URI_PERMISSION

                        println("your URI path: ${it.path}")
                        println("your URI encoded path: ${it.encodedPath}")
                        println("your URI path fragments: ${it.pathSegments}")
                        println("your URI scheme: ${it.scheme}")
                        println("your URI authority: ${it.authority}")
                        println("your URI encoded authority: ${it.encodedAuthority}")
                        println("your URI full: ${Uri.decode(it.toString())}")
                        println("decode: ${Uri.decode(it.toString())}")
                        println("tostring : ${(it.toString())}")
                        context.getActivity()?.contentResolver?.takePersistableUriPermission(it, takeFlags)
                        context.getActivity()?.grantUriPermission(context.packageName,it, takeFlags)
                        pickedUri((it.toString()))

                    }

        }

//    val permissionState = rememberPermissionState(permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE){
//            isGranted ->
//        if (isGranted) {
//        } else {
//
//        }
//    }

    return launcher
}

@Composable
fun CheckAndRequestFileStoragePermission(
    permission: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val context = LocalContext.current


    val permissionGranted = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            launcher.launch(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            })
        }
    }

    val permissionCheck = { permission: String ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    if (!permissionCheck(permission)) {
        permissionGranted.launch(permission)
    }
}
fun checkAndRequestFileStoragePermission(context: Activity,
    permission: String,
     launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
        // Permission already granted, launch directory picker
        launcher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
    } else {
        // Permission not granted, request it
        ActivityCompat.requestPermissions(context, arrayOf(permission), 0)
    }
}