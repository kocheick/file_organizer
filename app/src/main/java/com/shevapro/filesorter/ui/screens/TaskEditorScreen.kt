package com.shevapro.filesorter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shevapro.filesorter.R
import com.shevapro.filesorter.model.ConditionType
import com.shevapro.filesorter.model.LogicalOperator
import com.shevapro.filesorter.model.RuleCondition
import com.shevapro.filesorter.model.UIRule
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.ui.components.form.TaskFormEditor
import com.shevapro.filesorter.ui.components.rule.PresetRulesComponent
import com.shevapro.filesorter.ui.components.rule.SaveRuleDialog
import com.shevapro.filesorter.ui.theme.AppTheme

/**
 * Screen for adding or editing a task with unified interface
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TaskEditorScreen(
    item: UITaskRecord,
    isEditMode: Boolean = false,
    presetRules: List<UIRule> = emptyList(),
    onSaveTask: (String, String, String) -> Unit,
    onCancel: () -> Unit,
    onSaveUpdates: (UITaskRecord?) -> Unit,
    foundExtensions: List<String> = emptyList(),
    onSourceSelected: (String) -> Unit,
    onSaveAsRule: ((String, String, List<RuleCondition>, LogicalOperator) -> Unit)? = null
) {
    var extension = item.extension
    var source = item.source
    var destination = item.destination
    var showSaveRuleDialog by remember { mutableStateOf(false) }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = stringResource(
                                if (isEditMode) R.string.edit else R.string.add_item
                            ).uppercase()
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            onSaveUpdates(
                                item.copy(
                                    extension = extension, 
                                    source = source, 
                                    destination = destination, 
                                    id = item.id
                                )
                            )
                            onCancel()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Save as Rule button - only show if the callback is provided
                        if (onSaveAsRule != null) {
                            IconButton(
                                onClick = { showSaveRuleDialog = true },
                                enabled = extension.isNotBlank() && destination.isNotBlank()
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save as Rule")
                            }
                        }

                        // Save button
                        IconButton(onClick = {
                            onSaveTask(extension, source, destination)
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Display preset rules for both add and edit modes
                if (presetRules.isNotEmpty()) {
                    PresetRulesComponent(
                        presetRules = presetRules,
                        onRuleSelected = { rule ->
                            // Apply the selected rule's destination
                            destination = rule.destination
                            // Extract extension from the first condition if it's a FILE_TYPE
                            rule.conditions.find { it.type == ConditionType.FILE_TYPE }?.let {
                                extension = it.value
                            }
                            // Update UI state to reflect changes
                            onSaveUpdates(
                                item.copy(
                                    extension = extension,
                                    source = source,
                                    destination = destination,
                                    id = item.id
                                )
                            )
                        }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // Unified task form
                TaskFormEditor(
                    taskToBeEdited = item,
                    onDestinationUriChange = {
                        destination = it
                        onSaveUpdates(
                            item.copy(
                                extension = extension, 
                                source = source, 
                                destination = it, 
                                id = item.id
                            )
                        )
                    },
                    onSourceUriChange = {
                        source = it
                        onSourceSelected(it)
                        onSaveUpdates(
                            item.copy(
                                extension = extension, 
                                source = it, 
                                destination = destination, 
                                id = item.id
                            )
                        )
                    },
                    onTypeChange = {
                        extension = it
                        onSaveUpdates(
                            item.copy(
                                extension = it, 
                                source = source, 
                                destination = destination, 
                                id = item.id
                            )
                        )
                    },
                    extensionLabelText = stringResource(
                        id = R.string.enter_file_extension_or_type_with,
                        extension.uppercase()
                    ),
                    typesFromSelectedSource = foundExtensions
                )
            }
        }
    }

    // Save Rule Dialog - only show if the callback is provided
    if (showSaveRuleDialog && onSaveAsRule != null) {
        SaveRuleDialog(
            extension = extension,
            destination = destination,
            onSaveRule = { ruleName, logicalOperator ->
                // Create a condition from the current extension
                val condition = RuleCondition(
                    type = ConditionType.FILE_TYPE,
                    value = extension,
                    operator = "equals"
                )

                onSaveAsRule(ruleName, destination, listOf(condition), logicalOperator)
                showSaveRuleDialog = false
            },
            onDismiss = { showSaveRuleDialog = false }
        )
    }
}
