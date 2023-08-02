package com.shevapro.filesorter.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.*
import com.shevapro.filesorter.R
import com.shevapro.filesorter.Utility.emptyInteractionSource
import com.shevapro.filesorter.model.EmptyContentException
import com.shevapro.filesorter.model.MissingFieldException
import com.shevapro.filesorter.model.NoFileFoundException
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.ui.components.LoadingScreen
import com.shevapro.filesorter.ui.components.NotificationDialog
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import com.shevapro.filesorter.AddTaskDialog
import com.shevapro.filesorter.EditTaskDialog
import com.shevapro.filesorter.RemovalDialog
import com.shevapro.filesorter.TaskListContent
import com.shevapro.filesorter.ui.theme.AppTheme


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainScreen(viewModel: MainViewModel) {

    val mainState: UiState by viewModel.mainState.collectAsState()
    val itemCount =
        remember(mainState) { if (mainState is UiState.Data) (mainState as UiState.Data).records.size else 0 }

    val isAddDialogOpen by viewModel.isAddDialogpOpen.collectAsState()
    val isEditDialogOpen by viewModel.isEditDialogpOpen.collectAsState()


//    var itemToEdit: UITaskRecord? by remember { mutableStateOf(null) }
    val itemToEdit by viewModel.itemToEdit.collectAsState()
    val itemToRemove by viewModel.itemToRemove.collectAsState()
    val itemToAdd by viewModel.itemToAdd.collectAsState()



    AppTheme {
        Scaffold(
            topBar = { TopBarLayout() },
            floatingActionButton = {
                ActionButtons(itemCount = itemCount,
                    onAddNewTaskItem = {
                        viewModel.openAddDialog()
                        if (itemToAdd == null) viewModel.onUpdateItemToAdd(UITaskRecord.EMPTY_OBJECT)
                    },

                    onExecuteTasksClicked = {
                        viewModel.processTasks()
                    })
            },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = false,
            content = { paddingValues: PaddingValues ->
                paddingValues

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                    when (val currentState = mainState) {
                        is UiState.Loading -> {
                            LoadingScreen()
                        }

                        is UiState.Data
                        -> {
                            val items = currentState.records


                            if (items.isEmpty()) EmptyContentScreen()
                            else {
                                val isRemovalDialogOpen = rememberSaveable { mutableStateOf(false) }

                                TaskListContent(
                                    tasksList = items,
                                    onItemClick = { clickedTask ->

                                        viewModel.onUpdateItemToEdit(clickedTask)

                                        viewModel.openEditDialog()
                                    },
                                    onRemoveItem = { itemToBeRemoved ->
                                        viewModel.onUpdateItemToRemove(itemToBeRemoved)
                                        isRemovalDialogOpen.value = true

                                    }, onToggleState = { itemToBeToggled ->
                                        viewModel.toggleStateFor(itemToBeToggled)
                                    })



                                AnimatedVisibility(
                                    isEditDialogOpen && itemToEdit != null
                                ) {
                                    EditTaskDialog(
                                        itemToBeEdited = itemToEdit ?: return@AnimatedVisibility,
                                        onUpdateItem = { viewModel.updateItem(it) },
                                        onSaveUpdates = { viewModel.onUpdateItemToEdit(it) },
                                        onDismiss = {
                                            viewModel.closeEditDialog()
//                                            viewModel.onUpdateItemToEdit(null)
                                        })
                                }

                                if (isRemovalDialogOpen.value && itemToRemove != null) AnimatedVisibility(
                                    isRemovalDialogOpen.value
                                ) {
                                    RemovalDialog(
                                        item = itemToRemove ?: return@AnimatedVisibility,
                                        onConfirm = {
                                            viewModel.removeItem(
                                                itemToRemove ?: return@RemovalDialog
                                            )
                                            viewModel.onUpdateItemToRemove(null)
                                        },
                                        onDismiss = {
                                            isRemovalDialogOpen.value = false
                                            viewModel.onUpdateItemToRemove(null)
                                        })
                                }

                            }

                            AnimatedVisibility(currentState.exception != null) {
                                when (val exception = currentState.exception) {

                                    is MissingFieldException -> {
                                        NotificationDialog(title = stringResource(id = R.string.missing_field),
                                            message = exception.errorMessage,
                                            onDismiss = {
//                                        if (itemToAdd != null && !isAddDialogOpen.value) isAddDialogOpen.value = true
//                                        else if (itemToEdit != null && !isEditDialogOpen.value) isEditDialogOpen.value = true

                                                viewModel.dismissError()

                                            })
                                    }

                                    is NoFileFoundException -> {
                                        NotificationDialog(title = "Result",
                                            message = exception.message
                                                ?: return@AnimatedVisibility,
                                            onDismiss = {
                                                viewModel.dismissError()
                                            })
                                    }

                                    is EmptyContentException -> {
                                        NotificationDialog(title = "Alert",
                                            message = exception.message
                                                ?: return@AnimatedVisibility,
                                            onDismiss = {
                                                viewModel.dismissError()
                                            })
                                    }

                                    else -> {
                                        NotificationDialog(title = "Oops.. an error occurred.",
                                            message = exception?.message
                                                ?: return@AnimatedVisibility,
                                            onDismiss = {
                                                viewModel.dismissError()
                                            })
                                    }
                                }
                            }
                        }

                    }


                    AnimatedVisibility(isAddDialogOpen) {
                        AddTaskDialog(onAddItem = { extension, src, dest ->
                            viewModel.addNewItemWith(extension, src, dest)
//                            viewModel.onUpdateItemToAdd(null)

                        },
                            onSaveUpdates = {
                                viewModel.onUpdateItemToAdd(it)
                            },

                            item = itemToAdd ?: return@AnimatedVisibility,
                            onDismiss = {
                                viewModel.closeAddDialog()
                                // cpmment this line out will let user add new item from previous properties instead of blank/new item
                            })
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Image(modifier = Modifier.size(620.dp), painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription ="App logo.", alpha = 0.02F , contentScale = ContentScale.Fit)

        Text(text = stringResource(R.string.no_item_created), fontSize = 24.sp) }
}


@Preview
@Composable
private fun TopBarLayout() {
    TopAppBar(title = {
        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "App logo")
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
fun ActionButtons(
    itemCount: Int,
    onAddNewTaskItem: () -> Unit,
    onExecuteTasksClicked: () -> Unit
) {

    val processBackgroundColor = remember { Animatable(initialValue = Color.LightGray.copy(0.8f)) }

    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            processBackgroundColor.animateTo(Color(R.color.jet).copy(0.4f))
        } else {
            processBackgroundColor.animateTo(Color.LightGray)
        }

    }
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
            interactionSource = remember {
                if (itemCount > 0)
                    MutableInteractionSource()
                else emptyInteractionSource
            },
            modifier = Modifier.size(32.dp),
            onClick = {
                onExecuteTasksClicked()
            },
            backgroundColor = processBackgroundColor.value,
        ) {
            Icon(
                Icons.Filled.Done,
                contentDescription = "Execute",
                tint = Color.White.copy(0.8f)
            )
        }
    }
}