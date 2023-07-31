package com.shevapro.filesorter.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.shevapro.filesorter.R


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