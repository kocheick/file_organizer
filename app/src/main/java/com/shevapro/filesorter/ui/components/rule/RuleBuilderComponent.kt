package com.shevapro.filesorter.ui.components.rule

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.FolderPicker
import com.shevapro.filesorter.R
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.model.ConditionType
import com.shevapro.filesorter.model.LogicalOperator
import com.shevapro.filesorter.model.RuleCondition
import com.shevapro.filesorter.model.UIRule

/**
 * Component for building and editing rules
 */
@Composable
fun RuleBuilderComponent(
    rule: UIRule,
    onRuleNameChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onLogicalOperatorChange: (LogicalOperator) -> Unit,
    onAddCondition: (RuleCondition) -> Unit,
    onUpdateCondition: (Int, RuleCondition) -> Unit,
    onRemoveCondition: (Int) -> Unit,
    onSaveRule: () -> Unit,
    onCancel: () -> Unit
) {
    var showAddConditionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Rule name
        OutlinedTextField(
            value = rule.name,
            onValueChange = onRuleNameChange,
            label = { Text("Rule Name") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colors.secondaryVariant,
                focusedBorderColor = MaterialTheme.colors.primaryVariant,
                unfocusedBorderColor = MaterialTheme.colors.secondaryVariant.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colors.primaryVariant,
                cursorColor = MaterialTheme.colors.primaryVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // Logical operator selection
        Text(
            text = "Combine conditions with:",
            color = MaterialTheme.colors.secondaryVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = rule.logicalOperator == LogicalOperator.AND,
                        onClick = { onLogicalOperatorChange(LogicalOperator.AND) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colors.primaryVariant,
                            unselectedColor = MaterialTheme.colors.secondaryVariant.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "AND",
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { onLogicalOperatorChange(LogicalOperator.AND) }
                            .padding(start = 4.dp, end = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = rule.logicalOperator == LogicalOperator.OR,
                        onClick = { onLogicalOperatorChange(LogicalOperator.OR) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colors.primaryVariant,
                            unselectedColor = MaterialTheme.colors.secondaryVariant.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "OR",
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onLogicalOperatorChange(LogicalOperator.OR) }
                            .padding(start = 4.dp)
                    )
                }

        }
        
        // Conditions list
        Text(
            text = "Conditions",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (rule.conditions.isEmpty()) {
            Text(
                text = "No conditions added yet. Add a condition to get started.",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                itemsIndexed(rule.conditions) { index, condition ->
                    ConditionItem(
                        condition = condition,
                        onUpdateCondition = { onUpdateCondition(index, it) },
                        onRemoveCondition = { onRemoveCondition(index) }
                    )
                }
            }
        }
        
        // Add condition button
        Button(
            onClick = { showAddConditionDialog = true },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.fiery_rose).copy(alpha = 0.8f),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Condition",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Add Condition")
        }
        
        // Destination folder selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(IntrinsicSize.Min)
                .clickable {
                    FolderPicker.showFolderPicker(
                        context = context,
                        initialPath = rule.destination.takeIf { it.isNotBlank() },
                        onFolderSelected = { uri ->
                            onDestinationChange(uri.toString())
                        }
                    )
                }, // Make Row height determined by tallest child intrinsic height
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = Utility.formatUriToUIString(rule.destination),
                onValueChange = { /* Will be handled by folder picker */ },
                readOnly = true,
                label = { Text("Destination Folder") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    focusedBorderColor = MaterialTheme.colors.primaryVariant,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colors.primaryVariant,
                    cursorColor = MaterialTheme.colors.primaryVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)

                // Let the TextField determine its intrinsic height
            )

            // Use a Button instead of IconButton for easier size control matching TextField
            // Or constrain the IconButton's height
            Button( // Changed to Button for potentially easier height matching
                onClick = {
                    FolderPicker.showFolderPicker(
                        context = context,
                        initialPath = rule.destination.takeIf { it.isNotBlank() },
                        onFolderSelected = { uri ->
                            onDestinationChange(uri.toString())
                        }
                    )
                },
                shape = RoundedCornerShape(4.dp), // Apply shape to Button
                contentPadding = PaddingValues(0.dp), // Remove default Button padding
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primaryVariant
                ),
                modifier = Modifier
                    .fillMaxHeight() // Make Button fill the height of the Row (determined by TextField)
                    .aspectRatio(1f) // Make it square to resemble an IconButton
                    // Removed explicit padding inside modifier, using contentPadding = 0.dp instead
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Select Folder",
                    tint = Color.White
                )
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            // Cancel button with better contrast and matching height
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.fiery_rose),
                    backgroundColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(end = 8.dp)
            ) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.button
                )
            }

            // Save button with better styling
            TextButton(
                onClick = onSaveRule,
                enabled = rule.name.isNotBlank() && rule.destination.isNotBlank() && rule.conditions.isNotEmpty(),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White, backgroundColor = colorResource(id = R.color.fiery_rose),
                  disabledContentColor = Color.White)
            ) {
                Text("Save Rule", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
    }
    
    // Add condition dialog
    if (showAddConditionDialog) {
        AddConditionDialog(
            onAddCondition = {
                onAddCondition(it)
                showAddConditionDialog = false
            },
            onDismiss = { showAddConditionDialog = false }
        )
    }
}

@Composable
fun ConditionItem(
    condition: RuleCondition,
    onUpdateCondition: (RuleCondition) -> Unit,
    onRemoveCondition: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEditDialog = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = condition.type.name.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "${condition.operator}: ${condition.value}",
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            
            IconButton(onClick = onRemoveCondition) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Condition",
                    tint = colorResource(id = R.color.fiery_rose)
                )
            }
        }
    }
    
    if (showEditDialog) {
        EditConditionDialog(
            condition = condition,
            onUpdateCondition = {
                onUpdateCondition(it)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun AddConditionDialog(
    onAddCondition: (RuleCondition) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(ConditionType.FILE_TYPE) }
    var value by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("equals") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Condition",
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Condition type dropdown
                Text(
                    text = "Condition Type",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                ConditionTypeDropdown(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Operator dropdown
                Text(
                    text = "Operator",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                OperatorDropdown(
                    selectedOperator = operator,
                    onOperatorSelected = { operator = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Value input
                Text(
                    text = "Value",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        focusedBorderColor = MaterialTheme.colors.primaryVariant,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colors.primaryVariant,
                        cursorColor = MaterialTheme.colors.primaryVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (value.isNotBlank()) {
                        onAddCondition(
                            RuleCondition(
                                type = selectedType,
                                value = value,
                                operator = operator
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primaryVariant,
                    contentColor = Color.White
                ),
                enabled = value.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.fiery_rose)
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun EditConditionDialog(
    condition: RuleCondition,
    onUpdateCondition: (RuleCondition) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(condition.type) }
    var value by remember { mutableStateOf(condition.value) }
    var operator by remember { mutableStateOf(condition.operator) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Condition",
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Condition type dropdown
                Text(
                    text = "Condition Type",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                ConditionTypeDropdown(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Operator dropdown
                Text(
                    text = "Operator",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                OperatorDropdown(
                    selectedOperator = operator,
                    onOperatorSelected = { operator = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Value input
                Text(
                    text = "Value",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        focusedBorderColor = MaterialTheme.colors.primaryVariant,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colors.primaryVariant,
                        cursorColor = MaterialTheme.colors.primaryVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (value.isNotBlank()) {
                        onUpdateCondition(
                            RuleCondition(
                                type = selectedType,
                                value = value,
                                operator = operator
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primaryVariant,
                    contentColor = Color.White
                ),
                enabled = value.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.fiery_rose)
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun ConditionTypeDropdown(
    selectedType: ConditionType,
    onTypeSelected: (ConditionType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .clickable { expanded = true }
            .padding(16.dp)
    ) {
        Text(
            text = selectedType.name.replace("_", " "),
            color = MaterialTheme.colors.onSurface
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            ConditionType.values().forEach { type ->
                DropdownMenuItem(
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                ) {
                    Text(
                        text = type.name.replace("_", " "),
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun OperatorDropdown(
    selectedOperator: String,
    onOperatorSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val operators = listOf("equals", "contains", "starts_with", "ends_with", "greater_than", "less_than")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .clickable { expanded = true }
            .padding(16.dp)
    ) {
        Text(
            text = selectedOperator.replace("_", " "),
            color = MaterialTheme.colors.onSurface
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            operators.forEach { op ->
                DropdownMenuItem(
                    onClick = {
                        onOperatorSelected(op)
                        expanded = false
                    }
                ) {
                    Text(
                        text = op.replace("_", " "),
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    }
}