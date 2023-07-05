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
    openDialog: MutableState<Boolean>,
    onTaskItemAdded: (TaskRecord) -> Unit,
    onFieldsLeftBlank:()->Unit
) {


    var newTask = EMPTY_ITEM


    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text("Add new item") },
            //alert dialog content/body goes in here
            text = {
                TaskForm(onDestinationUriChange = {newTask = newTask.copy(destination = it)}, onSourceUriChange = {
                    newTask = newTask.copy(source = it)
                }, onTypeChange = { newTask = newTask.copy(extension = it) })
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
                            if (newTask.extension.isEmpty() or newTask.source.isEmpty() or newTask.destination.isEmpty()){
                                onFieldsLeftBlank()
                            }else {
                                onTaskItemAdded(newTask)
                                openDialog.value = false
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
    } else {
        openDialog.value = false
    }
}

@Composable
fun EditTaskDialog(
    taskRecord: UITaskRecord,
    isDialogOpen: MutableState<Boolean>,
    onSaveUpdates: (UITaskRecord) -> Unit,
    onFieldsLeftBlank:()->Unit
) {
    var updatedItem: UITaskRecord by  remember(taskRecord){ mutableStateOf(taskRecord) }


    if (isDialogOpen.value) {
        AlertDialog(onDismissRequest = { isDialogOpen.value = false },
            title = { Text("Edit item") },
            //alert dialog content/body goes in here
            text = {
                TaskForm(onSourceUriChange = { updatedItem = updatedItem.copy(source = it, id = taskRecord?.id ?: 0)


                }, onDestinationUriChange = {updatedItem = updatedItem.copy(destination = it, id = taskRecord?.id ?: 0) }
                    , taskToBeEdited = updatedItem,
                onTypeChange = { updatedItem = updatedItem.copy(extension = it) })
            },
            buttons = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            isDialogOpen.value = false
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
                            if (updatedItem.extension.isEmpty() or updatedItem.source.isEmpty() or updatedItem.destination.isEmpty()){
                                onFieldsLeftBlank()
                            }else {
                                onSaveUpdates(updatedItem)
                                isDialogOpen.value = false
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
    } else {
        isDialogOpen.value = false
    }
}

@Composable
fun TaskForm(
    taskToBeEdited: UITaskRecord? = null
    , onSourceUriChange:(String)->Unit
    , onDestinationUriChange:(String)->Unit
    , onTypeChange:(String)->Unit) {
    val src = if (taskToBeEdited != null ) Uri.decode(taskToBeEdited.source) else null
    val dest = if (taskToBeEdited != null) Uri.decode(taskToBeEdited.destination) else null

    val sourcePath = remember { mutableStateOf(src ?:  "No folder selected") }
    val destPath = remember { mutableStateOf(dest ?:  "No folder selected") }

    val sourceDirectoryPickerLauncher = pickDirectory(sourcePath)
    val destinationDirectoryPickerLauncher = pickDirectory(destPath)

    val formattedSource =  Utility.formatUriToUIString(sourcePath.value)
    val formattedDestination = Utility.formatUriToUIString(destPath.value)

    val typeTextState = remember { mutableStateOf(TextFieldValue(taskToBeEdited?.extension ?: "")) }


    val typeText = typeTextState.value.text


    val propsText =
        if (taskToBeEdited != null) "Enter new file type, current -> ${taskToBeEdited.extension.uppercase()}" else "Enter file Extension or Type  :  ${typeTextState.value.text.uppercase()}"




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
                formattedSource, maxLines = 2
            )
        }
        // SWAP BUTTON
            Button(
                enabled = !sourcePath.value.isNullOrEmpty() or !destPath.value.isNullOrEmpty(),
                onClick = { swipePath(sourcePath, destPath) },
                modifier = Modifier
                    .align(Alignment.End)
                    .wrapContentSize()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp)),

                ) {
                Column{
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
                    Text("swap path", fontWeight = FontWeight.Light, fontSize = 8.sp)
                }
            }

// PICK DEST BUTTON
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            //destination folder button

            TextButton(
                onClick = { destinationDirectoryPickerLauncher.launch("".toUri())
                          },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = colorResource(R.color.fiery_rose),
                        contentColor = colorResource(R.color.white)
                    )
            ) {
                Text(stringResource(R.string.destination), textAlign = TextAlign.Center)
            }
            Text(
                formattedDestination, maxLines = 2
            )
        }
        onSourceUriChange(sourcePath.value)
        onDestinationUriChange(destPath.value)
        onTypeChange(typeText)

        println("your task ${taskToBeEdited}")
//        val task = taskToBeEdited?.copy(
//            extension = typeText, source = sourcePath.value , destination = destPath.value
//        )?.toTaskRecord() ?: TaskRecord(typeText, sourcePath.value.toString(), destPath.value.toString(),0)
//        val uiTask = task.toUITaskRecord()
//        onTaskItemAdded(task)


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