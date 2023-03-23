package com.example.fileorganizer

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.fileorganizer.TaskOrder.Companion.EMPTY_ITEM


@Composable
fun AddTaskDialog(
    openDialog: MutableState<Boolean>,
    onTaskItemAdded: (TaskOrder) -> Unit,
) {


    var newTask = EMPTY_ITEM


    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text("Add new item") },
            //alert dialog content/body goes in here
            text = {
                TaskForm(onTaskItemAdded = { newTask = it })
            },
            buttons = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            openDialog.value = false
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
                            onTaskItemAdded(newTask)
                            openDialog.value = false
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
    } else {
        openDialog.value = false
    }
}

@Composable
fun EditTaskDialog(
    taskOrder: TaskOrder?,
    openDialog: MutableState<Boolean>,
    onItemUpdated: (TaskOrder) -> Unit,
) {
    var updatedItem: TaskOrder = EMPTY_ITEM

    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text("Edit item") },
            //alert dialog content/body goes in here
            text = {
                TaskForm(onTaskItemAdded = { updatedItem = it }, taskToBeEdited = taskOrder)
            },
            buttons = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            openDialog.value = false
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
                            onItemUpdated(updatedItem)
                            openDialog.value = false
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
    } else {
        openDialog.value = false
    }
}

@Composable
fun TaskForm(
    onTaskItemAdded: (TaskOrder) -> Unit, taskToBeEdited: TaskOrder? = null
) {
    val src = if (taskToBeEdited != null) Uri.decode(taskToBeEdited.source) else ""
    val dest = if (taskToBeEdited != null) Uri.decode(taskToBeEdited.destination) else ""

    val sourcePath = remember { mutableStateOf(src) }
    val destPath = remember { mutableStateOf(dest) }

    val sourceDirectoryPickerLauncher = pickDirectory(sourcePath)
    val destinationDirectoryPickerLauncher = pickDirectory(destPath)

    val typeTextState = remember { mutableStateOf(TextFieldValue(taskToBeEdited?.extension ?: "")) }


    val typeText = typeTextState.value.text

    val propsText =
        if (taskToBeEdited == null) "Enter new file type, current -> ${typeTextState.value.text.uppercase()}" else "Enter file Extension or Type  :  ${taskToBeEdited.extension.uppercase()}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            //source folder button

            TextButton(
                onClick = {
                    sourceDirectoryPickerLauncher.launch("".toUri())
                },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = colorResource(R.color.fiery_rose),
                        contentColor = colorResource(R.color.white)
                    )
            ) {
                Text(stringResource(R.string.source), textAlign = TextAlign.Center)
            }

            Text(
                sourcePath.value?.substringAfterLast(":")?.replace("/", " > ")
                    ?: "No src selected", maxLines = 2
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            //destination folder button

            TextButton(
                onClick = { destinationDirectoryPickerLauncher.launch("".toUri()) },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = colorResource(R.color.fiery_rose),
                        contentColor = colorResource(R.color.white)
                    )
            ) {
                Text(stringResource(R.string.destination), textAlign = TextAlign.Center)
            }
            Text(
                destPath.value?.substringAfterLast(":")
                    ?.replace("/", " > ")
                    ?: "No src selected", maxLines = 2
            )
        }


    }

    val task = taskToBeEdited?.copy(extension = typeText, source = sourcePath.value, destination = destPath.value
    ) ?: TaskOrder(typeText, sourcePath.value.toString(),  destPath.value.toString())
    onTaskItemAdded(task)
    println(
        "new item being paassed ${task}"
    )
}


@Composable
fun pickDirectory(pathTextState: MutableState<String>): ManagedActivityResultLauncher<Uri?, Uri?> {

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
                pathTextState.value = Uri.decode(it.toString())
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