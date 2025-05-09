package com.shevapro.filesorter.ui.components.main

import androidx.compose.foundation.Image
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shevapro.filesorter.R

/**
 * Header component for the main screen
 */
@Composable
fun HeaderComponent() {
    TopAppBar(title = {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App logo"
        )
    }, backgroundColor = colorResource(R.color.lavender_blush), elevation = 2.dp)
}

@Preview
@Composable
private fun HeaderComponentPreview() {
    HeaderComponent()
}
