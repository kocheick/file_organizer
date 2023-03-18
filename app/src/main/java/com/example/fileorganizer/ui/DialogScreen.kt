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
fun ShowDialog(
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
    taskOrder: TaskOrder,
    openDialog: MutableState<Boolean>,
    onTaskItemSaved: (TaskOrder) -> Unit,
) {
    var newTask = taskOrder

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
                        onClick = { openDialog.value = false },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = colorResource(R.color.fiery_rose),
                            backgroundColor = Color.LightGray.copy(0.0f)
                        )
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            onTaskItemSaved(newTask)

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
fun TaskForm(
    onTaskItemAdded: (TaskOrder) -> Unit, newTask: TaskOrder? = null
) {
    val destPath = remember { mutableStateOf<Uri?>(null) }
    val sourcePath = remember { mutableStateOf<Uri?>(null) }

    val sourceDirectoryPickerLauncher = pickDirectory(sourcePath)
    val destinationDirectoryPickerLauncher = pickDirectory(destPath)

    val typeTextState = remember { mutableStateOf(TextFieldValue()) }


    val typeText = typeTextState.value.text
    val newTask =
        TaskOrder(typeText, sourcePath.value.toString(), destPath.value.toString())
    onTaskItemAdded(newTask)
    println(
        "new item being paassed ${newTask}"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            text = "Enter file Extension or Type  :  ${typeTextState.value.text.uppercase()}"
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                sourcePath.value?.path?.substringAfterLast(":")?.replace("/", " > ")
                    ?: "No src selected", maxLines = 2
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                destPath.value?.path?.substringAfterLast(":")
                    ?.replace("/", " > ")
                    ?: "No src selected", maxLines = 2
            )
        }


    }
}


@Composable
fun pickDirectory(pathTextState: MutableState<Uri?>): ManagedActivityResultLauncher<Uri?, Uri?> {

    val result =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let {
                pathTextState.value = it
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