package com.shevapro.filesorter.ui.components.main

import androidx.compose.animation.Animatable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.shevapro.filesorter.R
import com.shevapro.filesorter.Utility.emptyInteractionSource

/**
 * Action buttons component for the main screen
 */
@Composable
fun ActionButtonsComponent(
    itemCount: Int,
    onAddNewTaskItem: () -> Unit,
    onExecuteTasksClicked: () -> Unit
) {
    val processBackgroundColor = remember { Animatable(initialValue = Color.LightGray) }

    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            processBackgroundColor.animateTo(Color(0xFF5C6BC0))
        } else {
            processBackgroundColor.animateTo(Color.LightGray)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 64.dp)
    ) {
        FloatingActionButton(
            onClick = { onAddNewTaskItem() },
            backgroundColor = colorResource(R.color.fiery_rose)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add New Item")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        FloatingActionButton(
            interactionSource = remember {
                if (itemCount > 0)
                    MutableInteractionSource()
                else emptyInteractionSource
            },
            modifier = Modifier.size(32.dp),
            onClick = {
                onExecuteTasksClicked()
            },
            backgroundColor = processBackgroundColor.value,
        ) {
            Icon(
                Icons.Filled.Done,
                contentDescription = "start sorting files",
                tint = Color.White
            )
        }
    }
}
