package com.example.fileorganizer.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fileorganizer.*
import com.example.fileorganizer.R
import com.example.fileorganizer.model.MainState
import com.example.fileorganizer.model.UITaskRecord
import com.example.fileorganizer.ui.components.ErrorMessage
import com.example.fileorganizer.ui.components.LoadingScreen
import com.example.fileorganizer.ui.viewmodel.MainViewModel


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainScreen(viewModel: MainViewModel) {

    val mainState: MainState by viewModel.mainState.collectAsState()
    val isAddDialogOpen = remember { mutableStateOf(false) }

    var showMissingFieldError by remember { mutableStateOf(false) }


    var itemToEdit: UITaskRecord? by remember { mutableStateOf(null) }
    var itemToRemove: UITaskRecord? by remember { mutableStateOf(null) }

    AppTheme {
        Scaffold(
            topBar = { TopBarLayout() },
            floatingActionButton = {
                ActionButtons(
                    onAddNewTaskItem = {
                        isAddDialogOpen.value = true
                    },

                    onExecuteTasksClicked = { viewModel.processTasks() })
            },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = false,
            content = { paddingValues: PaddingValues ->
                paddingValues

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {


                    when (mainState) {
                        is MainState.Loading -> LoadingScreen()
                        is MainState.Error -> {
                            ErrorMessage((mainState as MainState.Error).message)
                        }

                        is MainState.Success -> {
                            val mainState = (mainState as MainState.Success)
                            val records = mainState.records
                            val isEditDialogOpen = remember { mutableStateOf(false) }
                            val isRemovalDialogOpen = remember { mutableStateOf(false) }

                            if (records.isEmpty())
                                EmptyContentScreen()
                             else
                                 TaskListContent(
                                   tasksList =  records,
                                    onEditItmClicked = { clickedTask ->

                                        itemToEdit = clickedTask

                                        isEditDialogOpen.value = true
                                    },
                                 onRemoveItem = { itemToBeRemoved ->
                                     itemToRemove = itemToBeRemoved
                                     isRemovalDialogOpen.value = true

                                 })



                            AnimatedVisibility (isAddDialogOpen.value) {
                                AddTaskDialog(onTaskItemAdded = { itemToAdd ->
                                    viewModel.addTask(itemToAdd)
                                },
                                    onFieldsLeftBlank = {
                                        showMissingFieldError = !showMissingFieldError
                                    }, onDissmiss = { isAddDialogOpen.value = false })
                            }

                            AnimatedVisibility (isEditDialogOpen.value && itemToEdit != null) {
                                EditTaskDialog(
                                    taskRecord = itemToEdit ?: return@AnimatedVisibility,
                                    onSaveUpdates = { viewModel.updateItem(it) },
                                    onFieldsLeftBlank = {
                                        showMissingFieldError = !showMissingFieldError
                                    }, onDismiss = {
                                        isEditDialogOpen.value = false
                                        itemToEdit = null
                                    })
                            }

                            AnimatedVisibility (isRemovalDialogOpen.value && itemToRemove != null){
                                RemovalDialog(item = itemToRemove ?: return@AnimatedVisibility, onConfirm = {viewModel.removeItem(itemToRemove ?: return@RemovalDialog)
                                                                                                            itemToRemove = null}, onDismiss =  {
                                    isRemovalDialogOpen.value = false
                                    itemToRemove = null
                                })
                            }


                            AnimatedVisibility (showMissingFieldError) {
                                MissingFieldDialog("Please, verify all inputs are filled", onDismiss = { showMissingFieldError = false})
                            }


                        }

                    }

                }
            },
            bottomBar = {
                //BottomBarLayout()
            }
        )
    }

}

@Composable
private fun EmptyContentScreen() {
    Text(
        text = stringResource(R.string.no_item_created),
        fontSize = 24.sp
    )
}

@Composable
private fun MissingFieldDialog(message: String, onDismiss:()->Unit) {
    AlertDialog(onDismissRequest = { onDismiss( )},
        title = { Text("Missing Field Alert ") },
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
                    Text("Ok,I undertand.")
                }
            }
        })
}

@Composable
fun RemovalDialog(item: UITaskRecord, onConfirm:()->Unit,onDismiss:()->Unit) {
    AlertDialog(onDismissRequest = { onDismiss( )},
        title = { Text("Remove item with type ${item.extension}") },
        text = { Text(text = "Are you sure to permanently remove this item ?") },
        buttons = {
            Row(
                Modifier.fillMaxWidth()
                    .padding(end = 12.dp),
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
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.LightGray,
                        backgroundColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            }
        })
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
fun TaskListContent(tasksList: List<UITaskRecord>, onEditItmClicked: (UITaskRecord) -> Unit, onRemoveItem:(UITaskRecord) -> Unit) {


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.raisin_black).copy(0.00f))
    ) {

        items(tasksList) { task ->

            TaskItem(task,
                onClick = { onEditItmClicked(task) },
                onRemoveClick = { onRemoveItem(task) })


        }


    }
}


@Composable
fun ActionButtons(
    onAddNewTaskItem: () -> Unit,
    onExecuteTasksClicked: () -> Unit
) {


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 64.dp)
    ) {

        FloatingActionButton(
            onClick = { onAddNewTaskItem() },
            backgroundColor = colorResource(R.color.fiery_rose)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Make a move")
        }


        Spacer(modifier = Modifier.padding(8.dp))

        FloatingActionButton(
            modifier = Modifier.size(32.dp),
            onClick = { onExecuteTasksClicked() },
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