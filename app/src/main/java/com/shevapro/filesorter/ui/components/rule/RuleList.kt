package com.shevapro.filesorter.ui.components.rule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevapro.filesorter.model.UIRule
import kotlinx.coroutines.CoroutineScope

/**
 * A component that displays a list of rules or an empty state if there are no rules
 *
 * @param rules The list of rules to display
 * @param onEditRule Callback when a rule is edited
 * @param onDeleteRule Callback when a rule is deleted
 * @param onCreateRule Callback when a new rule is created
 * @param coroutineScope Coroutine scope for launching coroutines
 * @param snackbarHostState Snackbar host state for showing snackbars
 */
@Composable
fun RuleList(
    rules: List<UIRule>,
    onEditRule: (UIRule) -> Unit,
    onDeleteRule: (UIRule) -> Unit,
    onCreateRule: () -> Unit,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (rules.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No rules created yet",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create a rule to organize your files automatically",
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCreateRule
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Rule",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Create Rule")
                }
            }
        } else {
            // Rules list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(rules) { rule ->
                    RuleItem(
                        rule = rule,
                        onEditRule = { onEditRule(rule) },
                        onDeleteRule = { onDeleteRule(rule) }
                    )
                }
            }
        }
    }
}