@file:OptIn(ExperimentalPermissionsApi::class)

package com.shevapro.filesorter.ui.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shevapro.filesorter.R

/**
 * Reusable confirmation buttons component used in dialogs
 */
@Composable
fun ConfirmationButtons(
    actionTextLabel: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(0.3f))
            .padding(2.dp), horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = {
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = colorResource(R.color.jet),
                backgroundColor = Color.LightGray.copy(0.0f)
            )
        ) {
            Text(stringResource(id = R.string.cancel))
        }
        TextButton(
            modifier = Modifier.padding(horizontal = 4.dp),
            onClick = {
                onAction()
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black,
                backgroundColor = Color.DarkGray.copy(0.14f)
            )
        ) {
            Text(actionTextLabel)
        }
    }
}

/**
 * Reusable cancel and save buttons component used in dialogs
 */
@Composable
fun CancelAndSaveButtons(
    onDismiss: () -> Unit,
    onSaveUpdates: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(2.dp), horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = {
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = colorResource(R.color.jet),
                backgroundColor = Color.LightGray.copy(0.0f)
            )
        ) {
            Text(stringResource(R.string.cancel))
        }
        TextButton(
            onClick = {
                onSaveUpdates()
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black,
                backgroundColor = Color.DarkGray.copy(0.2f)
            )
        ) {
            Text(stringResource(R.string.update))
        }
    }
}