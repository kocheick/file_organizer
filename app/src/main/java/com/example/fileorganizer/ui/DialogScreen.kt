package com.example.fileorganizer

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.runtime.*
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
import com.example.fileorganizer.TaskRecord.Companion.EMPTY_ITEM
import com.example.fileorganizer.model.UITaskRecord


@Composable
fun AddTaskDialog(
    onTaskItemAdded: (TaskRecord) -> Unit,
    onFieldsLeftBlank: () -> Unit,
    onDissmiss: () -> Unit
) {


    var newTask = EMPTY_ITEM


    AlertDialog(onDismissRequest = { onDissmiss() },
        title = { Text("Add new item") },
        //alert dialog content/body goes in here
        text = {
            TaskForm(
                onDestinationUriChange = { newTask = newTask.copy(destination = it) },
                onSourceUriChange = {
                    newTask = newTask.copy(source = it)
                },
                onTypeChange = { newTask = newTask.copy(extension = it) })
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
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        if (newTask.extension.isEmpty() or newTask.source.isEmpty() or newTask.destination.isEmpty()) {
                            onFieldsLeftBlank()
                        } else {
                            onTaskItemAdded(newTask)
                            onDissmiss()
                        }

                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.jet),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text("Add")
                }
            }
        }
    )
}

@Composable
fun EditTaskDialog(
    taskRecord: UITaskRecord,
    onSaveUpdates: (UITaskRecord) -> Unit,
    onFieldsLeftBlank: () -> Unit,
    onDismiss: () -> Unit
) {
    var updatedItem: UITaskRecord by remember(taskRecord) { mutableStateOf(taskRecord) }


    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text("Edit item") },
        //alert dialog content/body goes in here
        text = {
            TaskForm(
                onSourceUriChange = {
                    updatedItem = updatedItem.copy(source = it, id = taskRecord.id)


                },
                onDestinationUriChange = {
                    updatedItem = updatedItem.copy(destination = it, id = taskRecord.id)
                },
                taskToBeEdited = updatedItem,
                onTypeChange = { updatedItem = updatedItem.copy(extension = it) })
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
                            onFieldsLeftBlank()
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
fun TaskForm(
    taskToBeEdited: UITaskRecord? = null,
    onSourceUriChange: (String) -> Unit,
    onDestinationUriChange: (String) -> Unit,
    onTypeChange: (String) -> Unit
) {
    val src = if (taskToBeEdited != null) Uri.decode(taskToBeEdited.source) else null
    val dest = if (taskToBeEdited != null) Uri.decode(taskToBeEdited.destination) else null

    val sourcePath = remember { mutableStateOf(src ) }
    val destPath = remember { mutableStateOf(dest) }

    val sourceDirectoryPickerLauncher = pickDirectory({ sourcePath.value = it })
    val destinationDirectoryPickerLauncher = pickDirectory({ destPath.value = it })

    val formattedSource = Utility.formatUriToUIString(sourcePath.value ?: "No folder selected")
    val formattedDestination = Utility.formatUriToUIString(destPath.value ?: "No folder selected")

    val typeTextState = remember { mutableStateOf(TextFieldValue(taskToBeEdited?.extension ?: "")) }


    val typeText = typeTextState.value.text


    val propsText =
        if (taskToBeEdited != null) "Update file type, current -> ${taskToBeEdited.extension.uppercase()}" else "Enter file Extension or Type  :  ${typeTextState.value.text.uppercase()}"




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
            text = propsText
        )
        TextField(
            singleLine = true, maxLines = 1,
            label = { Text("Enter file type") },
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
            buttonText = stringResource(R.string.source),
            path = formattedSource,
            onPick = { sourceDirectoryPickerLauncher.launch(src?.toUri() ?: "".toUri()) }
        )
        // SWAP BUTTON
        SwapPathsButton(
            modifier =  Modifier.align(Alignment.End),
            isActive = !sourcePath.value.isNullOrEmpty() or !destPath.value.isNullOrEmpty(),
            onClick = { swipePath(sourcePath as MutableState<String>, destPath as MutableState<String>) }
        )

// PICK DEST BUTTON

        FolderPickerButton(
            buttonText = stringResource(R.string.destination),
            path = formattedDestination,
            onPick = {
                destinationDirectoryPickerLauncher.launch(dest?.toUri() ?: "".toUri())
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
private fun SwapPathsButton(modifier: Modifier, isActive:Boolean,
                            onClick:()->Unit
) {
    Button(
        enabled = isActive,
        onClick = {onClick()},
        modifier = modifier
            .wrapContentSize()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp)),

        ) {
        Column(modifier= Modifier.wrapContentWidth(),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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
    buttonText: String,
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
            Text(buttonText, textAlign = TextAlign.Center)
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
fun pickDirectory(pickedUri: (String)->Unit): ManagedActivityResultLauncher<Uri?, Uri?> {

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