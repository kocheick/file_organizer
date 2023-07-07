package com.example.fileorganizer.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun ErrorMessage(message: String){
    Text(text = message, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
}