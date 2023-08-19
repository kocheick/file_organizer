package com.shevapro.filesorter.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.R
import com.shevapro.filesorter.model.TaskStats
import com.shevapro.filesorter.ui.theme.AppThemeLight

@Preview()
@Composable
fun LoadingScreen() {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(100.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App logo."
        )
    }

}

@Composable
fun ProgressScreen(currentMoveStats: TaskStats) {

    val progress by animateFloatAsState(targetValue = (currentMoveStats.numberOfFilesMoved.toDouble() / currentMoveStats.totalFiles.toDouble()).toFloat())
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(modifier = Modifier.fillMaxWidth()
          , contentAlignment = Alignment.Center) {

            CircularProgressIndicator(modifier = Modifier.size(100.dp), progress = progress)
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App logo."
            )
        }
        Spacer(Modifier.height(64.dp))
        Column(  Modifier.
            fillMaxWidth(0.9f)
//            .width(350.dp)
            .border(2.dp, AppThemeLight.primary, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp),
            ){
            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Current File :", fontSize = 16.sp)
                Text(
                    currentMoveStats.currentFileName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                     overflow = TextOverflow.Ellipsis, maxLines = 1
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Items moved:", fontSize = 16.sp)
                Text(
                    "${currentMoveStats.numberOfFilesMoved}/${currentMoveStats.totalFiles}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

    }

}