package com.shevapro.filesorter.ui.components.rule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shevapro.filesorter.model.UIRule

/**
 * Component for displaying and applying preset rules
 * with dropdown menu to save space
 */
@Composable
fun PresetRulesComponent(
    presetRules: List<UIRule>,
    onRuleSelected: (UIRule) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedRule by remember { mutableStateOf<UIRule?>(null) }

    // Filter out duplicate rules based on name
    val uniqueRules = remember(presetRules) {
        presetRules.distinctBy { it.name }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // Header with expand/collapse button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.primaryVariant.copy(alpha = 0.1f))
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Preset Rules",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.secondaryVariant // Ensure good contrast
                )

                if (uniqueRules.isNotEmpty()) {
                    Text(
                        text = " (${uniqueRules.size})",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primaryVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Expand/collapse icon with indicator text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isExpanded) "Hide" else "Show",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primaryVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colors.primaryVariant
                )
            }
        }

        // Content - Dropdown style preset rules
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            if (uniqueRules.isEmpty()) {
                Text(
                    text = "No preset rules available",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Select a rule to apply:",
                        style = MaterialTheme.typography.caption,
                        color = Color.DarkGray, // Darker color for better contrast
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    uniqueRules.forEach { rule ->
                        val isSelected = selectedRule == rule

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRule = rule
                                    onRuleSelected(rule)
                                }
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colors.primaryVariant.copy(alpha = 0.2f)
                                    else
                                        Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = rule.name,
                                    style = MaterialTheme.typography.subtitle2,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colors.secondaryVariant, // Ensure good contrast
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${rule.conditions.size} condition(s) • ${rule.logicalOperator.name}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.secondaryVariant.copy(alpha = 0.7f), // Better contrast
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            TextButton(
                                onClick = {
                                selectedRule = rule
                                    onRuleSelected(rule)
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colors.primaryVariant
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Apply Rule",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp)
                                )
                                Text("Apply")
                            }
                        }

                        if (rule != uniqueRules.last()) {
                            Divider(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .alpha(0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetRuleCard(
    rule: UIRule,
    onRuleSelected: (UIRule) -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(4.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = rule.name,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Display the number of conditions
            Text(
                text = "${rule.conditions.size} condition(s)",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Display the logical operator
            Text(
                text = "Combined with: ${rule.logicalOperator.name}",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Display the destination
            Text(
                text = "To: ${rule.destination}",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Apply button
            Button(
                onClick = { onRuleSelected(rule) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Apply Rule",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Apply")
            }
        }
    }
}
