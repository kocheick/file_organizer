package com.shevapro.filesorter.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.ui.input.pointer.pointerInput

/**
 * A composable that displays a number picker with up/down arrows
 *
 * @param label The label to display above the picker
 * @param value The current value
 * @param range The range of allowed values
 * @param onValueChange Callback when the value changes
 * @param displayTransform Optional function to transform the display of the value
 * @param modifier Optional modifier for the component
 */
@Composable
fun NumberPickerColumn(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    displayTransform: ((Int) -> String) = { it.toString() },
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Card(
            modifier = Modifier
                .width(60.dp)
                .height(120.dp).scale(0.9f),
            elevation = 2.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Up arrow
                var upPressed by remember { mutableStateOf(false) }
                val upScale by animateFloatAsState(
                    targetValue = if (upPressed) 0.8f else 1f,
                    animationSpec = tween(durationMillis = 100),
                    label = "upScale"
                )

                IconButton(
                    onClick = {
                        val newValue = if (value >= range.last) range.first else value + 1
                        onValueChange(newValue)
                    },
                    modifier = Modifier
                        .semantics {
                            contentDescription = "Increase $label"
                        }
                        .scale(upScale)
                        .pressWithScale { upPressed = it }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase"
                    )
                }

                // Current value
                AnimatedNumberDisplay(
                    value = value,
                    displayTransform = displayTransform
                )

                // Down arrow
                var downPressed by remember { mutableStateOf(false) }
                val downScale by animateFloatAsState(
                    targetValue = if (downPressed) 0.8f else 1f,
                    animationSpec = tween(durationMillis = 100),
                    label = "downScale"
                )

                IconButton(
                    onClick = {
                        val newValue = if (value <= range.first) range.last else value - 1
                        onValueChange(newValue)
                    },
                    modifier = Modifier
                        .semantics {
                            contentDescription = "Decrease $label"
                        }
                        .scale(downScale)
                        .pressWithScale { downPressed = it }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedNumberDisplay(
    value: Int,
    displayTransform: (Int) -> String
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            // Determine if the value is increasing or decreasing
            val direction = if (targetState > initialState) 1 else -1

            // Create a slide in/out animation based on the direction
            (slideInVertically { height -> direction * height } + fadeIn()) with
                    (slideOutVertically { height -> -direction * height } + fadeOut()) using
                    SizeTransform(clip = false)
        },
        label = "NumberAnimation"
    ) { targetValue ->
        Text(
            text = displayTransform(targetValue),
            style = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onSurface),
            fontWeight = FontWeight.Bold
        )
    }
}

// Extension function to add a press animation to Modifier
private fun Modifier.pressWithScale(onStateChange: (Boolean) -> Unit) = this
    .offsetScale(
        pressed = { onStateChange(true) },
        released = { onStateChange(false) }
    )

// Extension function to handle press events
private fun Modifier.offsetScale(
    pressed: () -> Unit,
    released: () -> Unit
) = this
    .pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                when {
                    event.type == androidx.compose.ui.input.pointer.PointerEventType.Press -> pressed()
                    event.type == androidx.compose.ui.input.pointer.PointerEventType.Release -> released()
                }
            }
        }
    }