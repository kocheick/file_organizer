package com.example.fileorganizer

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.fileorganizer.model.UITaskRecord


@Composable
fun AddTaskDialog(
    item: UITaskRecord? = null,
    onAddItem: (String, String, String) -> Unit,
    onFieldsLeftBlank: (UITaskRecord) -> Unit,
    onDissmiss: () -> Unit
) {


    var extension by remember{ mutableStateOf( item?.extension ?: "") }

    var source = item?.source ?: ""

    var destination = item?.destination ?: ""



    AlertDialog(onDismissRequest = { onDissmiss() },
        title = { Text(stringResource(R.string.add_new_item)) },
        //alert dialog content/body goes in here
        text = {
            TaskForm(
                taskToBeEdited = item,
                onDestinationUriChange = { destination = it },
                onSourceUriChange = {
                    source = it
                },
                onTypeChange = { extension = it },
                extensionLabelText = stringResource(
                    id = R.string.enter_file_extension_or_type_with,
                    extension.uppercase()
                )

            )
        },
        buttons = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = {
                        onDissmiss()
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
                        if (extension.isEmpty() or source.isEmpty() or destination.isEmpty()) {
                            onFieldsLeftBlank(
                                UITaskRecord(
                                    extension = extension, source = source, destination
                                    = destination, id = item?.id ?: 0
                                )
                            )
                        } else {
                            onAddItem(extension, source, destination)

                        }

                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.jet),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text(stringResource(id = R.string.add))
                }
            }
        }
    )
}

@Composable
fun EditTaskDialog(
    taskRecord: UITaskRecord,
    onSaveUpdates: (UITaskRecord) -> Unit,
    onFieldsLeftBlank: (UITaskRecord) -> Unit,
    onDismiss: () -> Unit
) {
    var updatedItem: UITaskRecord by remember(taskRecord) { mutableStateOf(taskRecord) }


    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text("Edit item") },
        //alert dialog content/body goes in here
        text = {
            TaskForm(
                onSourceUriChange = {
                    updatedItem = updatedItem.copy(source = it)


                },
                onDestinationUriChange = {
                    updatedItem = updatedItem.copy(destination = it)
                },
                taskToBeEdited = updatedItem,
                onTypeChange = { updatedItem = updatedItem.copy(extension = it) },
                extensionLabelText = "Update file type, current -> ${updatedItem.extension.uppercase()}"
            )
        },
        buttons = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = {
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.fiery_rose),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        if (updatedItem.extension.isEmpty() or updatedItem.source.isEmpty() or updatedItem.destination.isEmpty()) {
                            onFieldsLeftBlank(updatedItem)
                        } else {
                            onSaveUpdates(updatedItem)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.jet),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text("Save")
                }
            }
        }
    )

}
@Composable
 fun MissingFieldDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.missing_field_alert)) },
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

    val sourceDirectoryPickerLauncher = pickDirectory({ sourcePath.value = it })
    val destinationDirectoryPickerLauncher = pickDirectory({ destPath.value = it })

    val formattedSource = Utility.formatUriToUIString(sourcePath.value ?: stringResource(R.string.no_folder_selected))
    val formattedDestination = Utility.formatUriToUIString(destPath.value ?: stringResource(R.string.no_folder_selected))

    val typeTextState = remember { mutableStateOf(TextFieldValue(taskToBeEdited?.extension ?: "")) }


    val typeText = typeTextState.value.text




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
            onValueChange = { typeTextState.value = it }, modifier = Modifier
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
            onPick = { sourceDirectoryPickerLauncher.launch( sourcePath.value?.toUri() ?:  src?.toUri() ?:"".toUri()) }
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
                destinationDirectoryPickerLauncher.launch(destPath.value?.toUri() ?: dest?.toUri() ?:  "".toUri())
            })

        sourcePath.value?.let { onSourceUriChange(it) }
        destPath.value?.let { onDestinationUriChange(it) }
        onTypeChange(typeText)

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


@Composable
fun pickDirectory(pickedUri: (String) -> Unit): ManagedActivityResultLauncher<Uri?, Uri?> {

    val result =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let {
                println("your URI path: ${it.path}")
                println("your URI encoded path: ${it.encodedPath}")
                println("your URI path fragments: ${it.pathSegments}")
                println("your URI scheme: ${it.scheme}")
                println("your URI authority: ${it.authority}")
                println("your URI encoded authority: ${it.encodedAuthority}")
                println("your URI full: ${Uri.decode(it.toString())}")
                println(Uri.decode(it.toString()))
                pickedUri(Uri.decode(it.toString()))
            }
        }
    return result
}


fun checkAndRequestFileStoragePermission(
    context: Context,
    permission: String,
    launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
        // Permission already granted, launch directory picker
        launcher.launch("*/*")
    } else {
        // Permission not granted, request it
        launcher.launch(permission)
    }
}