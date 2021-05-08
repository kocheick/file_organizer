package com.example.fileorganizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fileorganizer.R
import com.example.fileorganizer.TaskOrder
import com.example.fileorganizer.samples
import java.util.*


@Composable
fun MainContentLayout() {
    AppTheme {
        Scaffold(
            drawerContent = { },
            topBar = { TopBarLayout() },
            floatingActionButton = { NewTaskButtons() },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = false,
            content = { TaskListLayout() },
            bottomBar = {}
        )
    }

}

@Preview
@Composable
private fun TopBarLayout() {
    TopAppBar(title = {
        Text(stringResource(R.string.app_name))
    }, backgroundColor = colorResource(R.color.lavender_blush), elevation = 2.dp)
}

@Composable
fun BottomBarLayout() {

    val scaffoldState: ScaffoldState = rememberScaffoldState()

    BottomAppBar(
        backgroundColor = colorResource(R.color.lavender_blush),
        cutoutShape = CircleShape
    ) {
        IconButton(onClick = {
            //   scaffoldState.drawerState.open()
        }) {
            Icon(Icons.Filled.Menu, "MenuIcon")
        }
    }
}

@Composable
fun TaskListLayout() {

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
            .background(colorResource(R.color.raisin_black).copy(0.00f))
    ) {
        items(samples) { task ->
            TaskItemLayout(task)
        }
    }
}


@Composable
fun TaskItemLayout(
    task: TaskOrder,
    onTaskClick: (TaskOrder) -> Unit = {},
    onTaskEditClick: (TaskOrder) -> Unit = {}
) {

    val fileType = task.type.toString().toUpperCase(Locale.ROOT)
    val sourceFolder = task.from.toString()
    val destinationFolder = task.to.toString()

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically

    ) {

        Row(
            modifier = Modifier.clickable(
                indication = rememberRipple(),
                interactionSource = MutableInteractionSource(),
                onClick = {
                    onTaskClick(task)
                    println("task got clicked")
                }
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // type/extension layout
            Box(
                modifier = Modifier.size(50.dp).width(8.dp).background(
                    shape = CircleShape, color = colorResource(R.color.middle_blue_green).copy(0.4f)
                )
            ) {
                Text(
                    fileType,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold
                )

            }

            // source & dest. folders info layout
            Column(
                modifier = Modifier.width(250.dp).padding(start = 16.dp)
            ) {

                FolderPath("from", sourceFolder)
                FolderPath("to    ", destinationFolder)
            }
        }


        //Edit button
        TextButton(
            content = { Text("Edit") },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(R.color.lavender_blush),
                contentColor = colorResource(R.color.jet)
            ), modifier = Modifier.padding(end = 8.dp),
            onClick = {
                onTaskEditClick(task)
                println("Edit button clicked")
            }
        )
    }

}


@Composable
fun NewTaskButtons() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val openDialog = remember { mutableStateOf(false) }

        FloatingActionButton(
            onClick = { openDialog.value = true },
            backgroundColor = colorResource(R.color.fiery_rose)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Make a move")
        }

        AddTaskItemLayout(openDialog)

        Spacer(modifier = Modifier.padding(8.dp))

        FloatingActionButton(
            modifier = Modifier.size(32.dp),
            onClick = { println("Exec button clicked") },
            backgroundColor = colorResource(R.color.jet)
        ) {
            Icon(
                Icons.Filled.Done,
                contentDescription = "Execute",
                tint = colorResource(R.color.lavender_blush)
            )

        }

    }
}

@Composable
private fun AddTaskItemLayout(openDialog: MutableState<Boolean>) {
    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text("Add new item") },
            //alert dialog content/body goes in here
            text = { NewTaskItemInputLayout() },
            confirmButton = {
                TextButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.jet),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDialog.value = false },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.fiery_rose),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    } else {
        openDialog.value = false
    }
}

@Composable
private fun FolderPath(source: String, destination: String) {

    Row(horizontalArrangement = Arrangement.SpaceBetween) {
        Text(source, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
        Text(
            " : ",
            color = colorResource(R.color.jet),
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        )
        Text("/$destination/", color = colorResource(R.color.jet))
    }


}

@Composable
fun NewTaskItemInputLayout() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val typeTextState = remember { mutableStateOf(TextFieldValue()) }

        Text("Selected type: ${typeTextState.value.text.toUpperCase(Locale.ROOT)}")
        TextField(
            label = { Text("Enter file type") },
            value = typeTextState.value,
            onValueChange = { typeTextState.value = it }, modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("/file source path/")
            TextButton(
                onClick = {}, colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = colorResource(R.color.fiery_rose),
                        contentColor = colorResource(R.color.white)
                    )
            ) {
                Text("Source folder", textAlign = TextAlign.Center)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("/file dest path/")
            TextButton(
                onClick = {}, colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = colorResource(R.color.fiery_rose),
                        contentColor = colorResource(R.color.white)
                    )
            ) {
                Text("Dest. folder", textAlign = TextAlign.Center)
            }
        }


    }
}


