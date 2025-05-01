package com.shevapro.filesorter.ui

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.shevapro.filesorter.App
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.service.RuleExecutionService
import com.shevapro.filesorter.model.ScheduleType
import com.shevapro.filesorter.ui.screens.MainScreen
import com.shevapro.filesorter.ui.screens.RuleManagementScreen
import com.shevapro.filesorter.ui.screens.TaskEditorScreen
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import com.shevapro.filesorter.ui.viewmodel.RuleViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        App.vm
    }

    val ruleViewModel: RuleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the rule execution service
        RuleExecutionService.startService(this)

        setContent {
            supportActionBar?.hide()

            AppNavigation(viewModel, ruleViewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the service when the app is destroyed
        RuleExecutionService.stopService(this)
    }
}

enum class Screen {
    Main, AddTask, EditTask, RuleManagement
}

@Composable
fun AppNavigation(viewModel: MainViewModel, ruleViewModel: RuleViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.Main) }
    val itemToAdd by viewModel.itemToAdd.collectAsState()
    val itemToEdit by viewModel.itemToEdit.collectAsState()
    val foundExtensions by viewModel.foundExtensions.collectAsState()

    val presetRules by ruleViewModel.presetRules.collectAsState()

    // Add animation for screen transitions
    androidx.compose.animation.AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            // Simple fade animation
            fadeIn(animationSpec = tween(300)).togetherWith(fadeOut(animationSpec = tween(300)))
        }
    ) { screen ->
        when (screen) {
            Screen.Main -> {
                MainScreen(
                    viewModel = viewModel,
                    onNavigateToAddTask = { 
                        currentScreen = Screen.AddTask
                        if (itemToAdd == null) viewModel.onUpdateItemToAdd(UITaskRecord.EMPTY_OBJECT)
                    },
                    onNavigateToEditTask = { task ->
                        currentScreen = Screen.EditTask
                        viewModel.onUpdateItemToEdit(task)
                    },
                    onNavigateToRuleManagement = {
                        currentScreen = Screen.RuleManagement
                        ruleViewModel.resetCurrentRule()
                    }
                )
            }
            Screen.AddTask -> {
                // Use EnhancedAddTaskScreen instead of EnhancedAddTaskDialog
                TaskEditorScreen(
                    item = itemToAdd ?: UITaskRecord.EMPTY_OBJECT,
                    isEditMode = false,
                    presetRules = presetRules,
                    onSaveTask = { extension, source, destination ->
                        viewModel.addNewItemWith(extension, source, destination)
                        currentScreen = Screen.Main
                    },
                    onCancel = {
                        currentScreen = Screen.Main
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
            Screen.EditTask -> {
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
                        currentScreen = Screen.Main
                    },
                    onCancel = {
                        currentScreen = Screen.Main
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
                        currentScreen = Screen.Main
                    }
                )
            }
            Screen.RuleManagement -> {
                val allRules by ruleViewModel.allRules.collectAsState()
                val currentRule by ruleViewModel.currentRule.collectAsState()

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
                        currentScreen = Screen.Main
                    },
                    onEditRule = { ruleViewModel.setCurrentRule(it) },
                    onDeleteRule = { ruleViewModel.deleteRule(it) },
                    onToggleRuleActive = { rule ->
                        // Set current rule first to ensure we're modifying the correct rule
                        ruleViewModel.setCurrentRule(rule)
                        // Toggle active state
                        ruleViewModel.toggleRuleActive()
                        // Save changes to database
                        ruleViewModel.saveRule()
                    },
                    onToggleRuleScheduled = { rule ->
                        // Set current rule first to ensure we're modifying the correct rule
                        ruleViewModel.setCurrentRule(rule)
                        // Toggle scheduled state
                        ruleViewModel.toggleRuleScheduled()
                        // Save changes to database immediately
                        ruleViewModel.saveRule()
                    },
                    onUpdateSchedule = { rule, scheduleType, date ->
                        ruleViewModel.setCurrentRule(rule)
                        if (!rule.isScheduled) {
                            ruleViewModel.toggleRuleScheduled()
                        }
                        ruleViewModel.updateSchedule(scheduleType, date)
                        ruleViewModel.saveRule()
                    },
                    onNavigateBack = { currentScreen = Screen.Main },
                    onInitiateAddRule = { ruleViewModel.resetCurrentRule() }
                )
            }
        }
    }
}

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
