package com.example.fileorganizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fileorganizer.*
import com.example.fileorganizer.R
import com.example.fileorganizer.ui.viewmodel.MainViewModel


@Composable
fun TaskScreen(viewModel: MainViewModel) {

    val tasksList: List<TaskOrder> by viewModel.tasks.observeAsState(listOf())

    AppTheme {
        Scaffold(
            topBar = { TopBarLayout() },
            floatingActionButton = {
                AddTaskButton(onTaskItemAdded = { itemToAdd -> viewModel.addTask(itemToAdd) })
            },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = false,
            content = {
                TaskListContent(tasksList)
            },
            bottomBar = {
                //BottomBarLayout()
            }
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

@Preview
@Composable
fun BottomBarLayout() {

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
fun TaskListContent(tasksList: List<TaskOrder>) {
    if (tasksList.isNullOrEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.no_item_created), fontSize = 24.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.raisin_black).copy(0.00f))
        ) {

            items(tasksList) { task ->

                TaskItem(task)
            }
        }
    }
}


@Composable
fun AddTaskButton(
    onTaskItemAdded: (TaskOrder) -> Unit
) {

    val isDialogOpen = remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        FloatingActionButton(
            onClick = { isDialogOpen.value = true },
            backgroundColor = colorResource(R.color.fiery_rose)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Make a move")
        }

        ShowDialog(isDialogOpen, onTaskItemAdded = { onTaskItemAdded(it) })

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