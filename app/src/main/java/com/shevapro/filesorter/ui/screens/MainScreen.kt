package com.shevapro.filesorter.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shevapro.filesorter.R
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.model.ScheduleType
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import java.util.Date
import com.shevapro.filesorter.ui.components.LoadingScreen
import com.shevapro.filesorter.ui.components.NotificationDialog
import com.shevapro.filesorter.ui.components.PermissionRequestDialog
import com.shevapro.filesorter.ui.components.ProgressScreen
import com.shevapro.filesorter.ui.components.Stats
import com.shevapro.filesorter.ui.components.TaskListContent
import com.shevapro.filesorter.ui.components.dialog.RemovalDialog
import com.shevapro.filesorter.ui.components.dialog.ScheduleDialog
import com.shevapro.filesorter.ui.components.main.ActionButtonsComponent
import com.shevapro.filesorter.ui.components.main.EmptyContentComponent
import com.shevapro.filesorter.ui.components.main.HeaderComponent
import com.shevapro.filesorter.ui.theme.AppTheme
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import com.shevapro.filesorter.ui.components.ads.AdBanner
import com.shevapro.filesorter.ui.components.ads.AdInterstitial

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToEditTask: (UITaskRecord) -> Unit = {},
    onNavigateToRuleManagement: () -> Unit = {},
    onNavigateToProcessing: () -> Unit = {}
) {

    val mainState: UiState by viewModel.mainState.collectAsState()
    val itemCount =
        remember(mainState) { if (mainState.isData) mainState.records.size else 0 }

    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsState(initial = false)
    val isEditDialogOpen by viewModel.isEditDialogOpen.collectAsState(initial = false)

    // Variable to track when to show the schedule dialog
    val showScheduleDialog = remember { mutableStateOf<UITaskRecord?>(null) }

//    var itemToEdit: UITaskRecord? by remember { mutableStateOf(null) }
    val itemToEdit by viewModel.itemToEdit.collectAsState(initial = null)
    val itemToRemove by viewModel.itemToRemove.collectAsState(initial = null)
    val itemToAdd by viewModel.itemToAdd.collectAsState(initial = null)

    val foundExtensions : List<String> by viewModel.foundExtensions.collectAsState(initial = emptyList())

    val appStats: AppStatistic by viewModel.appStats.collectAsState()

    // State for interstitial ad
    val shouldShowInterstitial by viewModel.shouldShowInterstitial.collectAsState()

    // We no longer check for storage permissions at app startup
    // Instead, permissions are requested when the user selects a folder
    val context = LocalContext.current




        Scaffold(
            topBar = { HeaderComponent(onNavigateToRuleManagement = onNavigateToRuleManagement) },
            floatingActionButton = {
                // Only show FAB when not in Processing or ProcessingComplete state
                if (!mainState.isProcessing && !mainState.isProcessingComplete) {
                    ActionButtonsComponent(
                        itemCount = itemCount,
                        onAddNewTaskItem = {
                            onNavigateToAddTask()
                        },
                        onExecuteTasksClicked = {
                            if (viewModel.hasActiveTasksToProcess()) {
                                viewModel.sortFiles()
                                onNavigateToProcessing()
                            } else {
                                // Show error message when no active tasks
                                viewModel.showNoActiveTasksError()
                            }
                        }
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = false,
            content = { paddingValues: PaddingValues ->
                paddingValues

                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {

                    when {
                        mainState.isLoading -> {
                            LoadingScreen()
                        }

                        mainState.isData -> {
                            val items = mainState.records


                                val isRemovalDialogOpen = rememberSaveable { mutableStateOf(false) }
                                Column(
                                    Modifier
                                        .animateContentSize()
                                    , horizontalAlignment = Alignment.CenterHorizontally){

                                    Stats(appStatistic = appStats)
                                    if (items.isEmpty())
                                        EmptyContentComponent()
                                    else {
                                        TaskListContent(
                                            tasksList = items,
                                            onItemClick = { clickedTask ->
                                                onNavigateToEditTask(clickedTask)
                                            },
                                            onRemoveItem = { itemToBeRemoved ->
                                                viewModel.onUpdateItemToRemove(itemToBeRemoved)
                                                isRemovalDialogOpen.value = true
                                            }, 
                                            onToggleState = { itemToBeToggled ->
                                                viewModel.toggleStateFor(itemToBeToggled)
                                            },
                                            onToggleScheduled = { task ->
                                                // Toggle scheduled state
                                                val updatedTask = task.copy(isScheduled = !task.isScheduled)
                                                viewModel.updateItem(updatedTask)
                                            },
                                            onSchedule = { task ->
                                                // Show schedule dialog
                                                showScheduleDialog.value = task
                                            },
                                            onEditClick = { task ->
                                                onNavigateToEditTask(task)
                                            }
                                        )
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

                                // Show schedule dialog when a task is selected for scheduling
                                showScheduleDialog.value?.let { task ->
                                    ScheduleDialog(
                                        task = task,
                                        onScheduleSet = { scheduleType: ScheduleType, date: Date? ->
                                            // Update the task with the new schedule based on selected type
                                            val updatedTask = if (scheduleType == ScheduleType.NEVER) {
                                                // For NEVER, disable scheduling completely
                                                task.copy(
                                                    isScheduled = false,
                                                    scheduleType = ScheduleType.NEVER,
                                                    scheduleTime = null
                                                )
                                            } else {
                                                // For other types, enable scheduling with the selected date
                                                task.copy(
                                                    isScheduled = true,
                                                    scheduleType = scheduleType,
                                                    scheduleTime = date
                                                )
                                            }
                                            viewModel.updateItem(updatedTask)
                                            showScheduleDialog.value = null
                                        },
                                        onDismiss = {
                                            showScheduleDialog.value = null
                                        }
                                    )
                                }

                            }

                        }

                        mainState.isProcessing || mainState.isProcessingComplete -> {
                            // Processing state is now handled in a separate screen
                            // This will automatically navigate to the processing screen via onNavigateToProcessing
                            // in the onExecuteTasksClicked handler
                        }

                        else -> {}
                    }


                    // Add dialog replaced with navigation to add screen
                    if (mainState.isData) AnimatedVisibility(visible = mainState.exception != null) {
                        when (val exception = mainState.exception) {

                            is AppExceptions.MissingFieldException -> {
                                NotificationDialog(
                                    title = stringResource(id = R.string.input_error),
                                    message = exception.message,
                                    onDismiss = {
//                                        if (itemToAdd != null && !isAddDialogOpen.value) isAddDialogOpen.value = true
//                                        else if (itemToEdit != null && !isEditDialogOpen.value) isEditDialogOpen.value = true

                                        viewModel.dismissError()

                                    })
                            }

                            is AppExceptions.NoFileFoundException -> {
                                NotificationDialog(
                                    title = "Result",
                                    message = exception.message
                                        ?: return@AnimatedVisibility,
                                    onDismiss = {
                                        viewModel.dismissError()
                                    })
                            }

                            is AppExceptions.EmptyContentException -> {
                                NotificationDialog(
                                    title = "Message",
                                    message = exception.message
                                        ?: return@AnimatedVisibility,
                                    onDismiss = {
                                        viewModel.dismissError()
                                    })
                            }

                            is AppExceptions.PermissionExceptionForUri -> {
//                                PermissionRequestDialog(
//                                    uri = exception.errorMessage.substringBefore(" from")
//                                        ?.substringAfter("content://") ?: "", onAuthorize = {
//
//                                    }, onDismiss = {
//                                        viewModel.dismissError()
//                                    })
                            }

                            is AppExceptions.UnknownError -> {
                                NotificationDialog(
                                    title = "Oops.. an unknown error occurred.",
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
                AdBanner()
            }
        )

        // Show interstitial ad when needed
        AdInterstitial(
            show = shouldShowInterstitial,
            onAdClosed = {
                viewModel.onInterstitialAdClosed()
            }
        )


}
