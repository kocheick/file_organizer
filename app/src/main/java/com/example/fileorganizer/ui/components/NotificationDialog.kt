package com.example.fileorganizer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fileorganizer.R


@Composable
fun NotificationDialog(title:String, message: String, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = {Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)}
        ,buttons = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = {
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = colorResource(R.color.fiery_rose),
                    backgroundColor = Color.LightGray.copy(0.0f)
                )
            ) {
                Text(stringResource(id = R.string.ok))
            }}
    }, text = {
        Text(text = message)
    })
}