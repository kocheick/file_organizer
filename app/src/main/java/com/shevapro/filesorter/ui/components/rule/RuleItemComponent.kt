package com.shevapro.filesorter.ui.components.rule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevapro.filesorter.R
import com.shevapro.filesorter.model.UIRule

/**
 * Component for displaying a single rule item in the rule management screen.
 * 
 * @param rule The rule to display
 * @param onEditRule Callback when the edit button is clicked
 * @param onDeleteRule Callback when the delete button is clicked
 */
@Composable
fun RuleItem(
    rule: UIRule,
    onEditRule: () -> Unit,
    onDeleteRule: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Rule header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        // Add content description for accessibility
                        modifier = Modifier.semantics {
                            contentDescription = "Rule name: ${rule.name}"
                        }
                    )
                    Text(
                        text = "Destination: ${(rule.destination)}",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        // Add content description for accessibility
                        modifier = Modifier.semantics {
                            contentDescription = "Destination folder: ${rule.destination}"
                        }
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Conditions summary
            Text(
                text = "Conditions (${rule.conditions.size}):",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            rule.conditions.take(3).forEach { condition ->
                Text(
                    text = "• ${condition.type.name.replace("_", " ")}: ${condition.operator} ${condition.value}",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }
            
            if (rule.conditions.size > 3) {
                Text(
                    text = "• ... and ${rule.conditions.size - 3} more",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEditRule,
                    modifier = Modifier.semantics {
                        contentDescription = "Edit rule ${rule.name}"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Rule",
                        tint = MaterialTheme.colors.secondary
                    )
                }
                
                IconButton(
                    onClick = onDeleteRule,
                    modifier = Modifier.semantics {
                        contentDescription = "Delete rule ${rule.name}"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Rule",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}