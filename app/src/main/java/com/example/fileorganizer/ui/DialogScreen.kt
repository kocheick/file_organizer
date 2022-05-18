package com.example.fileorganizer

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import java.util.*


@Composable
fun ShowDialog(
    openDialog: MutableState<Boolean>,
    onTaskItemAdded: (TaskOrder) -> Unit,
) {
    var newTask = TaskOrder("", "", "")
    1
    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text("Add new item") },
            //alert dialog content/body goes in here
            text = {
                NewTaskForm(onTaskItemAdded = { newTask = it })
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
fun NewTaskForm(
    onTaskItemAdded: (TaskOrder) -> Unit, newTask: TaskOrder? = null
) {
    val destPath = remember { mutableStateOf("") }
    val sourcePath = remember { mutableStateOf("") }

    val sourceDirectoryPickerLauncher = pickDirectory(sourcePath)

    val destinationDirectoryPickerLauncher = pickDirectory(destPath)


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        val typeTextState = remember { mutableStateOf(TextFieldValue()) }

        Text(
            maxLines = 1,
            text = "File Type/Extension :  ${typeTextState.value.text.uppercase()}"
        )
        TextField(
            singleLine = true, maxLines = 1,
          //
            //  lkjkjlhg]\
            //
            //  '\textStyle = TextStyle(fontSize = 18.sp),
            label = { Text("Enter file type") },
            value = typeTextState.value,
            onValueChange = { typeTextState.value = it }, modifier = Modifier.padding(bottom = 16.dp).width(180.dp)
        )

        Text(maxLines = 1, text = stringResource(R.string.select))

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
                    sourceDirectoryPickerLauncher.launch("".toUri()) },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = colorResource(R.color.fiery_rose),
                        contentColor = colorResource(R.color.white)
                    )
            ) {
                Text(stringResource(R.string.source), textAlign = TextAlign.Center)
            }

            Text(sourcePath.value, maxLines = 2)
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
            Text(destPath.value, maxLines = 2)
        }

        val typeText = typeTextState.value.text
        onTaskItemAdded(TaskOrder(typeText, sourcePath.value, destPath.value))
        println(
            "new item being paassed ${
                TaskOrder(
                    typeText,
                    sourcePath.value,
                    destPath.value
                )
            }"
        )

    }
}

@Composable
private fun pickDirectory(pathTextState: MutableState<String>) =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) {
        pathTextState.value = it.let{
            it?.path.toString()
        }

    }

