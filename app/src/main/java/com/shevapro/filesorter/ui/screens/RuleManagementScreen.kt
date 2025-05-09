package com.shevapro.filesorter.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shevapro.filesorter.model.UIRule
import com.shevapro.filesorter.R
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.ui.components.ads.AdBanner
import com.shevapro.filesorter.ui.components.common.NumberPickerColumn
import com.shevapro.filesorter.ui.components.dialog.RuleEditDialog
import com.shevapro.filesorter.ui.components.rule.RuleBuilderComponent
import com.shevapro.filesorter.ui.components.rule.RuleList
import com.shevapro.filesorter.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Screen for managing rules
 */
@Composable
fun RuleManagementScreen(
    allRules: List<UIRule>,
    currentRule: UIRule,
    onRuleNameChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onLogicalOperatorChange: (com.shevapro.filesorter.model.LogicalOperator) -> Unit,
    onAddCondition: (com.shevapro.filesorter.model.RuleCondition) -> Unit,
    onUpdateCondition: (Int, com.shevapro.filesorter.model.RuleCondition) -> Unit,
    onRemoveCondition: (Int) -> Unit,
    onSaveRule: () -> Unit,
    onEditRule: (UIRule) -> Unit,
    onDeleteRule: (UIRule) -> Unit,
    onNavigateBack: () -> Unit,
    onInitiateAddRule: () -> Unit
) {
    var showAddEditDialog by remember { mutableStateOf(false) }

    // For showing snackbar messages
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Rule Management") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            onInitiateAddRule()
                            showAddEditDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Rule")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                AdBanner()
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Use the extracted RuleList component
                RuleList(
                    rules = allRules,
                    onEditRule = { rule ->
                        onEditRule(rule)
                        showAddEditDialog = true
                    },
                    onDeleteRule = onDeleteRule,
                    onCreateRule = {
                        onInitiateAddRule()
                        showAddEditDialog = true
                    },
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState
                )

                // Use the extracted RuleEditDialog component
                RuleEditDialog(
                    isVisible = showAddEditDialog,
                    rule = currentRule,
                    onRuleNameChange = onRuleNameChange,
                    onDestinationChange = onDestinationChange,
                    onLogicalOperatorChange = onLogicalOperatorChange,
                    onAddCondition = onAddCondition,
                    onUpdateCondition = onUpdateCondition,
                    onRemoveCondition = onRemoveCondition,
                    onSaveRule = onSaveRule,
                    onDismiss = { showAddEditDialog = false }
                )
            }
        }

}