package com.example.fileorganizer.ui.components

import android.widget.Toast
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ErrorMessage(message:String){
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun ErrorScreen(message: String){
    Text(text = message, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
}