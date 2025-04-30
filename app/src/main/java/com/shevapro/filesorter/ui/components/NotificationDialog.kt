package com.shevapro.filesorter.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.R
import com.shevapro.filesorter.Utility

/**
 * Type of permission action to take
 */
enum class PermissionAction {
    REQUEST_STORAGE_PERMISSION,
    REQUEST_ALL_FILES_ACCESS,
    OPEN_SETTINGS
}

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

/**
 * A dialog that explains the need for storage permissions and guides the user to the settings.
 * This is specifically designed for Android 29+ (Q) where the storage permission model changed.
 */
@Composable
fun StoragePermissionDialog(
    onDismiss: () -> Unit,
    onPermissionAction: ((PermissionAction) -> Unit)? = null
) {
    val context = LocalContext.current
    val needsAllFilesAccess = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { 
            Text(
                "Storage Permission Required", 
                fontSize = 20.sp, 
                fontWeight = FontWeight.SemiBold
            ) 
        },
        text = {
            Column {
                Text(
                    "This app needs access to your storage to organize files. " +
                    "On Android 10 and above, you need to grant permission from the settings."
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (needsAllFilesAccess) {
                    Text(
                        "Please click 'All Files Access' and enable 'Allow management of all files'.",
                        fontWeight = FontWeight.Medium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Text(
                        "Please click 'Open Settings', select 'Permissions', and enable 'Storage'.",
                        fontWeight = FontWeight.Medium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                } else {
                    Text(
                        "Please grant the storage permission to allow the app to organize your files.",
                        fontWeight = FontWeight.Medium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Without this permission, the app won't be able to organize your files.",
                    color = colorResource(id = R.color.fiery_rose)
                )
            }
        },
        buttons = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // For Android 11+, we need two separate buttons for the different permission types
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    TextButton(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(48.dp),
                        onClick = {
                            onPermissionAction?.invoke(PermissionAction.REQUEST_STORAGE_PERMISSION) ?: onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            backgroundColor = colorResource(R.color.fiery_rose).copy(0.85f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Request Permissions",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    TextButton(
                        modifier = Modifier
                            .height(48.dp),
                        onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            onPermissionAction?.invoke(PermissionAction.REQUEST_ALL_FILES_ACCESS)
                                ?: context.startActivity(intent)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            backgroundColor = colorResource(R.color.fiery_rose).copy(0.85f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "All Files Access",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                } else {
                    TextButton(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(48.dp),
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = colorResource(id = R.color.dark_gray_text),
                            backgroundColor = Color.LightGray.copy(0.4f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    TextButton(
                        modifier = Modifier
                            .height(48.dp),
                        onClick = {
                            onPermissionAction?.invoke(PermissionAction.OPEN_SETTINGS)
                                ?: Utility.openStoragePermissionSettings(context)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            backgroundColor = colorResource(R.color.fiery_rose).copy(0.85f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Open Settings",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    )
}