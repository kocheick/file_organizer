package com.shevapro.filesorter.ui.components.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.R

/**
 * Component displayed when there are no tasks
 */
@Composable
fun EmptyContentComponent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            modifier = Modifier.size(620.dp),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App logo.",
            alpha = 0.02F,
            contentScale = ContentScale.Fit
        )

        Text(text = stringResource(R.string.no_item_created), fontSize = 24.sp)
    }
}