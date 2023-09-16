package com.shevapro.filesorter.ui.components

import android.Manifest
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.R


@Composable
fun NotificationDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },

        buttons = {

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp), horizontalArrangement = Arrangement.End) {
                TextButton(modifier = Modifier.fillMaxHeight(),
                    onClick = {
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(id = R.color.raisin_black),
                        backgroundColor = Color.DarkGray.copy(0.2f)
                    )
                ) {
                    Text(stringResource(id = R.string.ok))
                }
            }
        }, text = {
            Text(text = message)
        })
}

@Composable
fun PermissionRequestDialog(uri:String,onAuthorize:(Uri)->Unit, onDismiss: () -> Unit={}){
    val path:String? = "content://$uri"
    val parsedUri = Uri.parse(path)


    val sourcePath = remember { mutableStateOf(path) }

    
    val sourceDirectoryPickerLauncher = pickDirectory(pickedUri = {
        sourcePath.value = it
    })

    val _permissions = listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) _permissions.plus(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            .plus(Manifest.permission.MANAGE_EXTERNAL_STORAGE).toList() else _permissions.toList()
    }
    val srcLauncher = permissionLauncher(sourceDirectoryPickerLauncher, sourcePath, permissions)


    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text("Access Permission Needed", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
        buttons = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {


                TextButton(
                    onClick = { onDismiss()
                    }
                ) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                       srcLauncher.launch(permissions.toTypedArray())
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(R.color.fiery_rose),
                        backgroundColor = Color.LightGray.copy(0.0f)
                    )
                ) {
                    Text("Authorize")
                }
            }
        }, text = {
            Text("Permission is needed for following uri $parsedUri")
        })
}