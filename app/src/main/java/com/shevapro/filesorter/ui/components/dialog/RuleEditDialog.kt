package com.shevapro.filesorter.ui.components.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.shevapro.filesorter.model.LogicalOperator
import com.shevapro.filesorter.model.RuleCondition
import com.shevapro.filesorter.model.UIRule
import com.shevapro.filesorter.ui.components.rule.RuleBuilderComponent

/**
 * A dialog for adding or editing a rule
 *
 * @param isVisible Whether the dialog is visible
 * @param rule The rule being edited
 * @param onRuleNameChange Callback when the rule name changes
 * @param onDestinationChange Callback when the destination changes
 * @param onLogicalOperatorChange Callback when the logical operator changes
 * @param onAddCondition Callback when a condition is added
 * @param onUpdateCondition Callback when a condition is updated
 * @param onRemoveCondition Callback when a condition is removed
 * @param onSaveRule Callback when the rule is saved
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun RuleEditDialog(
    isVisible: Boolean,
    rule: UIRule,
    onRuleNameChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onLogicalOperatorChange: (LogicalOperator) -> Unit,
    onAddCondition: (RuleCondition) -> Unit,
    onUpdateCondition: (Int, RuleCondition) -> Unit,
    onRemoveCondition: (Int) -> Unit,
    onSaveRule: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { 
                Text(
                    text = if (rule.id == 0) "Create Rule" else "Edit Rule",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                RuleBuilderComponent(
                    rule = rule,
                    onRuleNameChange = onRuleNameChange,
                    onDestinationChange = onDestinationChange,
                    onLogicalOperatorChange = onLogicalOperatorChange,
                    onAddCondition = onAddCondition,
                    onUpdateCondition = onUpdateCondition,
                    onRemoveCondition = onRemoveCondition,
                    onSaveRule = {
                        onSaveRule()
                        onDismiss()
                    },
                    onCancel = onDismiss
                )
            },
            buttons = {}
        )
    }
}