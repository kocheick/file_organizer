package com.shevapro.filesorter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.R
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.ui.theme.AppThemeLight

@Composable
fun Stats(appStatistic: AppStatistic) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val config = LocalConfiguration.current

    val widthFraction by remember { mutableFloatStateOf(if (config.screenWidthDp.dp < 600.dp) 0.9f else 0.5f) }

    Column(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .padding(12.dp)
            .background(AppThemeLight.primary, RoundedCornerShape(12.dp))
            .border(1.dp, AppThemeLight.secondary, RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState())
//            .animateContentSize()
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextButton(
            colors = ButtonDefaults.textButtonColors(contentColor = Color.DarkGray),
            onClick = {
                expanded = !expanded
            }) { Text(modifier = Modifier.padding(vertical = 8.dp), text = "Quick Stats") }

        AnimatedVisibility(expanded) {


//                    Text(
//                        text = "Top ",
//                        modifier = Modifier.align(Alignment.CenterHorizontally),
//                        color = Color.DarkGray
//                    )
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(4.dp)
                        .border(
                            1.dp,
                            Color.DarkGray.copy(0.4f),
                            RoundedCornerShape(8.dp)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = stringResource(R.string.files_moved),
                        color = Color.DarkGray
                    )
                    Text(
                        text = appStatistic.totalFilesMoved.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(4.dp)
                        .border(
                            1.dp,
                            Color.DarkGray.copy(0.4f),
                            RoundedCornerShape(8.dp)
                        )
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = stringResource(R.string.time_saved), color = Color.DarkGray)
                    Text(
                        text = "${appStatistic.timeSavedInMinutes} min",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(4.dp)
                        .border(1.dp, Color.DarkGray.copy(0.4f), RoundedCornerShape(8.dp)),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(widthFraction),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Top " + "Source", fontSize = 10.sp)
                        Text(
//                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    Color.LightGray,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(3.dp),
                            text = appStatistic.mostUsed.topSourceFolder,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(widthFraction),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Top " + "Destination", fontSize = 10.sp)
                        Text(
//                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    Color.LightGray,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(3.dp),
                            text = appStatistic.mostUsed.topDestinationFolder,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(widthFraction),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Top " + "File Type", fontSize = 10.sp)
                        Text(
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    Color.LightGray,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(3.dp),
                            text = appStatistic.mostUsed.topMovedFileByType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            overflow = TextOverflow.Clip,
                            maxLines = 1
                        )
                    }
//
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                               ,
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//
//                        }
                    Spacer(Modifier.height(4.dp))

                }





                Spacer(Modifier.height(4.dp))

            }


        }
    }
}