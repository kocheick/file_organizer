package com.shevapro.filesorter.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.ui.screens.MainScreen
import com.shevapro.filesorter.ui.screens.ProcessingScreen
import com.shevapro.filesorter.ui.screens.RuleManagementScreen
import com.shevapro.filesorter.ui.screens.TaskEditorScreen
import com.shevapro.filesorter.ui.theme.AppTheme
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import com.shevapro.filesorter.ui.viewmodel.RuleViewModel

object NavigationRoutes {
    const val MAIN = "main"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task"
    const val RULE_MANAGEMENT = "rule_management"
    const val PROCESSING = "processing"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel,
    ruleViewModel: RuleViewModel
) {
    val itemToAdd by viewModel.itemToAdd.collectAsState(initial = null)
    val itemToEdit by viewModel.itemToEdit.collectAsState(initial = null)
    val foundExtensions by viewModel.foundExtensions.collectAsState(initial = emptyList())
    val presetRules by ruleViewModel.presetRules.collectAsState()
    val allRules by ruleViewModel.allRules.collectAsState()
    val currentRule by ruleViewModel.currentRule.collectAsState()

    // Animation duration
    val animDuration = 300

    AppTheme {
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.MAIN
        ) {
            composable(
                route = NavigationRoutes.MAIN,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(animDuration)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(animDuration)
                    )
                }
            ) {
                MainScreen(
                    viewModel = viewModel,
                    onNavigateToAddTask = {
                        if (itemToAdd == null) viewModel.onUpdateItemToAdd(UITaskRecord.EMPTY_OBJECT)
                        navController.navigate(NavigationRoutes.ADD_TASK)
                    },
                    onNavigateToEditTask = { task ->
                        viewModel.onUpdateItemToEdit(task)
                        navController.navigate(NavigationRoutes.EDIT_TASK)
                    },
                    onNavigateToRuleManagement = {
                        ruleViewModel.resetCurrentRule()
                        navController.navigate(NavigationRoutes.RULE_MANAGEMENT)
                    },
                    onNavigateToProcessing = {
                        navController.navigate(NavigationRoutes.PROCESSING)
                    }
                )
            }

            composable(
                route = NavigationRoutes.ADD_TASK,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(animDuration)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(animDuration)
                    )
                }
            ) {
                TaskEditorScreen(
                    item = itemToAdd ?: UITaskRecord.EMPTY_OBJECT,
                    isEditMode = false,
                    presetRules = presetRules,
                    onSaveTask = { extension, source, destination ->
                        viewModel.addNewItemWith(extension, source, destination)
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    },
                    onSaveUpdates = { viewModel.onUpdateItemToAdd(it) },
                    foundExtensions = foundExtensions,
                    onSourceSelected = { viewModel.getExtensionsForNewSource(it) },
                    onSaveAsRule = { name, destination, conditions, operator ->
                        // Save the current task as a rule
                        ruleViewModel.resetCurrentRule()
                        ruleViewModel.updateRuleName(name)
                        ruleViewModel.updateRuleDestination(destination)
                        ruleViewModel.updateRuleLogicalOperator(operator)
                        conditions.forEach { ruleViewModel.addCondition(it) }
                        ruleViewModel.saveRule()
                    }
                )
            }

            composable(
                route = NavigationRoutes.EDIT_TASK,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(animDuration)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(animDuration)
                    )
                }
            ) {
                TaskEditorScreen(
                    item = itemToEdit ?: UITaskRecord.EMPTY_OBJECT,
                    isEditMode = true,
                    presetRules = presetRules,
                    onSaveTask = { extension, source, destination ->
                        val updatedItem = (itemToEdit ?: UITaskRecord.EMPTY_OBJECT).copy(
                            extension = extension,
                            source = source,
                            destination = destination
                        )
                        viewModel.updateItem(updatedItem)
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    },
                    onSaveUpdates = { viewModel.onUpdateItemToEdit(it) },
                    foundExtensions = foundExtensions,
                    onSourceSelected = { viewModel.getExtensionsForNewSource(it) },
                    onSaveAsRule = { name, destination, conditions, operator ->
                        // Save the current task as a rule
                        ruleViewModel.resetCurrentRule()
                        ruleViewModel.updateRuleName(name)
                        ruleViewModel.updateRuleDestination(destination)
                        ruleViewModel.updateRuleLogicalOperator(operator)
                        conditions.forEach { ruleViewModel.addCondition(it) }
                        ruleViewModel.saveRule()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = NavigationRoutes.RULE_MANAGEMENT,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(animDuration)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(animDuration)
                    )
                }
            ) {
                RuleManagementScreen(
                    allRules = allRules,
                    currentRule = currentRule,
                    onRuleNameChange = { ruleViewModel.updateRuleName(it) },
                    onDestinationChange = { ruleViewModel.updateRuleDestination(it) },
                    onLogicalOperatorChange = { ruleViewModel.updateRuleLogicalOperator(it) },
                    onAddCondition = { ruleViewModel.addCondition(it) },
                    onUpdateCondition = { index, condition -> ruleViewModel.updateCondition(index, condition) },
                    onRemoveCondition = { ruleViewModel.removeCondition(it) },
                    onSaveRule = {
                        ruleViewModel.saveRule()
                        navController.popBackStack()
                    },
                    onEditRule = { ruleViewModel.setCurrentRule(it) },
                    onDeleteRule = { ruleViewModel.deleteRule(it) },
                    onNavigateBack = { navController.popBackStack() },
                    onInitiateAddRule = { ruleViewModel.resetCurrentRule() }
                )
            }

            composable(
                route = NavigationRoutes.PROCESSING,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(animDuration)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(animDuration)
                    )
                }
            ) {
                val state by viewModel.mainState.collectAsState()
                ProcessingScreen(
                    mainState = state,
                    onDone = {
                        viewModel.onProcessingCompleteAcknowledged()
                        navController.popBackStack(NavigationRoutes.MAIN, false)
                    },

                )
            }
        }
    }
}
