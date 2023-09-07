package com.shevapro.filesorter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.ui.theme.AppThemeLight

@Composable
fun Stats(appStatistic: AppStatistic) {
    var expanded by rememberSaveable{ mutableStateOf(false) }
    val config = LocalConfiguration.current

    val widthFraction by remember{ mutableFloatStateOf(if (config.screenWidthDp.dp < 600.dp) 1.0f else 0.6f) }

    Column(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .padding(12.dp)
            .background(AppThemeLight.primary, RoundedCornerShape(12.dp))
            .border(1.dp, AppThemeLight.secondary, RoundedCornerShape(12.dp))
//            .animateContentSize()
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(colors = ButtonDefaults.textButtonColors(contentColor = Color.DarkGray),onClick = {                       expanded = !expanded
        }){ Text(modifier = Modifier.padding(vertical = 8.dp), text = "Quick Stats") }

        AnimatedVisibility (expanded) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
//            columns = GridCells.Fixed(3),
                Modifier
                    .wrapContentSize()
                    .padding(8.dp)
//                .border(1.dp, Color.Red, RoundedCornerShape(12.dp))
                ,
                verticalArrangement = Arrangement.Center
            ) {

                item(appStatistic.totalFilesMoved) {

                    Column(
                        Modifier
                            .size(120.dp)
                            .padding(4.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(text = "Files Moved", color = Color.DarkGray)
                        Text(
                            text = appStatistic.totalFilesMoved.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                }
                item(appStatistic.mostUsed.toString()) {
                    Column(
                        Modifier
                            .size(120.dp)
                            .padding(4.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
//                    Text(
//                        text = "Top ",
//                        modifier = Modifier.align(Alignment.CenterHorizontally),
//                        color = Color.DarkGray
//                    )
                        Column(
//                            Modifier.padding(horizontal = 2.dp),

                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(text = "Top " + "Source", fontSize = 10.sp)
                            Text(
//                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                modifier = Modifier
                                    .border(1.dp,Color.LightGray, RoundedCornerShape(16.dp))
                                    .padding(3.dp),
                                text = appStatistic.mostUsed.topSourceFolder,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(text = "Top " + "Destination", fontSize = 10.sp)
                            Text(
//                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                modifier = Modifier
                                    .border(1.dp,Color.LightGray, RoundedCornerShape(16.dp))
                                    .padding(3.dp),
                                text = appStatistic.mostUsed.topDestinationFolder,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(4.dp))

                            Text(text = "Top " + "File Type", fontSize = 10.sp)
                            Text(modifier= Modifier
                                .border(1.dp,Color.LightGray, RoundedCornerShape(16.dp))
                                .padding(3.dp)
                                ,
                                text = appStatistic.mostUsed.topMovedFileByType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                overflow = TextOverflow.Clip,
                                maxLines = 1
                            )
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
                        }

                    }
                }
                item(appStatistic.toString()) {
                    Column(
                        Modifier
                            .size(120.dp)
                            .padding(4.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(text = "Time saved", color = Color.DarkGray)
                        Text(
                            text = "${appStatistic.timeSavedInMinutes} min",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                }
            }
        }
    }
}