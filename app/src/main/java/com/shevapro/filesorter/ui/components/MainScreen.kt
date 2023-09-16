package com.shevapro.filesorter.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.*
import com.shevapro.filesorter.R
import com.shevapro.filesorter.Utility.emptyInteractionSource
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.model.EmptyContentException
import com.shevapro.filesorter.model.MissingFieldException
import com.shevapro.filesorter.model.NoFileFoundException
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.ui.theme.AppTheme
import com.shevapro.filesorter.ui.viewmodel.MainViewModel


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

    val appStats: AppStatistic by viewModel.appStats.collectAsState()



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
                        viewModel.sortFiles()
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

                            if (items.isEmpty())
                                EmptyContentScreen()
                            else {
                                val isRemovalDialogOpen = rememberSaveable { mutableStateOf(false) }
                                Column(
                                    Modifier
                                        .animateContentSize()
                                    , horizontalAlignment = Alignment.CenterHorizontally){

                                    Stats(appStatistic = appStats)
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
                                }



                                AnimatedVisibility(
                                    isEditDialogOpen && itemToEdit != null
                                ) {
                                    EditTaskDialog(
                                        itemToBeEdited = itemToEdit ?: return@AnimatedVisibility,
                                        onUpdateItem = { viewModel.updateItem(it) },
                                        onSaveUpdates = { viewModel.onUpdateItemToEdit(it) },
                                        onDismiss = {
                                            viewModel.closeEditDialog()
                                        }, onReadErrorMessageForTask = {viewModel.dissmissErrorMessageForTask(it)})
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

                        }

                        is UiState.Processing -> {

                            ProgressScreen(currentState.stats)
                        }
                    }


                    AnimatedVisibility(isAddDialogOpen) {
                        AddTaskDialog(onAddItem = { extension, src, dest ->
                            viewModel.addNewItemWith(extension, src, dest)

                        },
                            onSaveUpdates = {
                                viewModel.onUpdateItemToAdd(it)
                            },

                            item = itemToAdd ?: return@AnimatedVisibility,
                            onDismiss = {
                                viewModel.closeAddDialog()
                            })
                    }
                    if (mainState is UiState.Data) AnimatedVisibility((mainState as UiState.Data).exception != null) {
                        when (val exception = (mainState as UiState.Data).exception) {

                            is AppExceptions.MissingFieldException -> {
                                NotificationDialog(title = stringResource(id = R.string.missing_field),
                                    message = exception.message,
                                    onDismiss = {
//                                        if (itemToAdd != null && !isAddDialogOpen.value) isAddDialogOpen.value = true
//                                        else if (itemToEdit != null && !isEditDialogOpen.value) isEditDialogOpen.value = true

                                        viewModel.dismissError()

                                    })
                            }

                            is AppExceptions.NoFileFoundException -> {
                                NotificationDialog(title = "Result",
                                    message = exception.message
                                        ?: return@AnimatedVisibility,
                                    onDismiss = {
                                        viewModel.dismissError()
                                    })
                            }

                            is AppExceptions.EmptyContentException -> {
                                NotificationDialog(title = "Message",
                                    message = exception.message
                                        ?: return@AnimatedVisibility,
                                    onDismiss = {
                                        viewModel.dismissError()
                                    })
                            }

                            is AppExceptions.PermissionExceptionForUri -> {
                                PermissionRequestDialog(
                                    uri = exception.errorMessage.substringBefore(" from")
                                        ?.substringAfter("content://") ?: "", onAuthorize = {

                                    }, onDismiss = {})
                            }

                            is AppExceptions.UnknownError -> {
                                NotificationDialog(title = "Oops.. an error occurred.",
                                    message = exception.message
                                        ?: return@AnimatedVisibility,
                                    onDismiss = {
                                        viewModel.dismissError()
                                    })
                            }

                            else -> {

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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            modifier = Modifier.size(620.dp),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App logo.",
            alpha = 0.02F,
            contentScale = ContentScale.Fit
        )

        Text(text = stringResource(R.string.no_item_created), fontSize = 24.sp)
    }
}


@Preview
@Composable
private fun TopBarLayout() {
    TopAppBar(title = {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App logo"
        )
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

    val processBackgroundColor = remember { Animatable(initialValue = Color.LightGray) }

    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            processBackgroundColor.animateTo(Color(0xFF5C6BC0))
//            processBackgroundColor.animateTo(Color(R.color.jet).copy(0.4f))

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
            Icon(Icons.Filled.Add, contentDescription = "Add New Item")
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
                contentDescription = "start sorting files",
                tint = Color.White
            )
        }
    }
}