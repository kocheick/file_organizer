package com.shevapro.filesorter.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.Utility.emptyInteractionSource
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.ui.components.dialog.AddTaskDialog
import com.shevapro.filesorter.ui.components.dialog.EditTaskDialog
import com.shevapro.filesorter.ui.components.dialog.RemovalDialog
import com.shevapro.filesorter.ui.components.main.ActionButtonsComponent
import com.shevapro.filesorter.ui.components.main.EmptyContentComponent
import com.shevapro.filesorter.ui.components.main.HeaderComponent
import com.shevapro.filesorter.ui.getActivity
import com.shevapro.filesorter.ui.theme.AppTheme
import com.shevapro.filesorter.ui.viewmodel.MainViewModel


@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToEditTask: (UITaskRecord) -> Unit = {}
) {

    val mainState: UiState by viewModel.mainState.collectAsState()
    val itemCount =
        remember(mainState) { if (mainState is UiState.Data) (mainState as UiState.Data).records.size else 0 }

    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsState()
    val isEditDialogOpen by viewModel.isEditDialogOpen.collectAsState()


//    var itemToEdit: UITaskRecord? by remember { mutableStateOf(null) }
    val itemToEdit by viewModel.itemToEdit.collectAsState()
    val itemToRemove by viewModel.itemToRemove.collectAsState()
    val itemToAdd by viewModel.itemToAdd.collectAsState()

    val foundExtensions : List<String> by viewModel.foundExtensions.collectAsState()

    val appStats: AppStatistic by viewModel.appStats.collectAsState()

    // We no longer check for storage permissions at app startup
    // Instead, permissions are requested when the user selects a folder
    val context = LocalContext.current




    AppTheme {
        Scaffold(
            topBar = { HeaderComponent() },
            floatingActionButton = {
                ActionButtonsComponent(itemCount = itemCount,
                    onAddNewTaskItem = {
                        onNavigateToAddTask()
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


                                val isRemovalDialogOpen = rememberSaveable { mutableStateOf(false) }
                                Column(
                                    Modifier
                                        .animateContentSize()
                                    , horizontalAlignment = Alignment.CenterHorizontally){

                                    Stats(appStatistic = appStats)
                                    if (items.isEmpty())
                                        EmptyContentComponent()
                                    else {   TaskListContent(
                                        tasksList = items,
                                        onItemClick = { clickedTask ->
                                            onNavigateToEditTask(clickedTask)
                                        },
                                        onRemoveItem = { itemToBeRemoved ->
                                            viewModel.onUpdateItemToRemove(itemToBeRemoved)
                                            isRemovalDialogOpen.value = true

                                        }, onToggleState = { itemToBeToggled ->
                                            viewModel.toggleStateFor(itemToBeToggled)
                                        })
                                }



                                // Edit dialog replaced with navigation to edit screen

                                if (isRemovalDialogOpen.value && itemToRemove != null) AnimatedVisibility(
                                    visible = isRemovalDialogOpen.value
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


                    // Add dialog replaced with navigation to add screen
                    if (mainState is UiState.Data) AnimatedVisibility(visible = (mainState as UiState.Data).exception != null) {
                        when (val exception = (mainState as UiState.Data).exception) {

                            is AppExceptions.MissingFieldException -> {
                                NotificationDialog(title = stringResource(id = R.string.input_error),
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

                                    }, onDismiss = {
                                        viewModel.dismissError()
                                    })
                            }

                            is AppExceptions.UnknownError -> {
                                NotificationDialog(title = "Oops.. an unknown error occurred.",
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
