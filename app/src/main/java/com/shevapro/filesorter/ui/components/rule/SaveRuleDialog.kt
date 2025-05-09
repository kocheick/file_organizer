package com.shevapro.filesorter.ui.components.rule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevapro.filesorter.model.LogicalOperator

@Composable
fun SaveRuleDialog(
    extension: String,
    destination: String,
    onSaveRule: (String, LogicalOperator) -> Unit,
    onDismiss: () -> Unit
) {
    var ruleName by remember { mutableStateOf("") }
    var logicalOperator by remember { mutableStateOf(LogicalOperator.AND) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save as Rule") },
        text = {
            Column {
                Text(
                    text = "Create a rule based on the current task:",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Rule name input
                OutlinedTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    label = { Text("Rule Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedLabelColor = MaterialTheme.colors.primaryVariant,focusedBorderColor = MaterialTheme.colors.primaryVariant, textColor = MaterialTheme.colors.onSurface)
                )

                // Display current extension and destination
                Text(
                    text = "File Type: $extension",
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Destination: $destination",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Logical operator selection
                Text(
                    text = "Logical Operator for Multiple Conditions:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = logicalOperator == LogicalOperator.AND,
                        onClick = { logicalOperator = LogicalOperator.AND }
                        ,colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colors.primaryVariant,
                            unselectedColor = MaterialTheme.colors.secondaryVariant
                        )
                    )
                    Text(
                        text = "AND",
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    RadioButton(
                        selected = logicalOperator == LogicalOperator.OR,
                        onClick = { logicalOperator = LogicalOperator.OR }
                        ,colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colors.primaryVariant,
                            unselectedColor = MaterialTheme.colors.secondaryVariant
                        )
                    )
                    Text(text = "OR")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ruleName.isNotBlank()) {
                        onSaveRule(ruleName, logicalOperator)
                    }
                },
                enabled = ruleName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant,
                    contentColor = Color.White
                )
            ) {
                Text("Save Rule", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colors.primaryVariant,backgroundColor = Color.Transparent)) {
                Text("Cancel")
            }
        }
    )
}