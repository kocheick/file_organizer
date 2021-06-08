package com.example.fileorganizer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.fileorganizer.R
import com.example.fileorganizer.ui.theme.*


private val AppThemeLight = lightColors(
    primary = lavender_blush, primaryVariant = fiery_rose,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    secondary = raisin_black, secondaryVariant = jet
)

private val AppThemeDark = darkColors(
    primary = Color(R.color.design_default_color_primary_dark),
    secondary = Color(R.color.design_default_color_secondary),
    onSecondary = Color(R.color.design_default_color_on_secondary),
    surface = Color(R.color.design_default_color_surface)
)

@Composable
fun AppTheme(
    darkThee: Boolean = false
    //isSystemInDarkTheme()
    ,
    content: @Composable () -> Unit
) {
    MaterialTheme(colors = if (darkThee) AppThemeDark else AppThemeLight, content = content)
}