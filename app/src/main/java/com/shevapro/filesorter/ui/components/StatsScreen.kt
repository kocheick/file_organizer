package com.shevapro.filesorter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.ui.theme.AppThemeLight

@Composable
fun Stats(appStatistic: AppStatistic) {
    Column(
        Modifier
            .wrapContentSize()
            .padding(12.dp)
            .background(AppThemeLight.primary, RoundedCornerShape(12.dp))
            .border(1.dp, AppThemeLight.secondary, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(modifier = Modifier.padding(vertical = 8.dp), text = "Quick Stats")

        LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), Modifier.padding(8.dp)) {

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
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Top",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.DarkGray
                    )
                    Column(
                        Modifier.padding(horizontal = 4.dp),

                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "- " + "Source", fontSize = 10.sp)
                            Text(
                                text = appStatistic.mostUsed.topSourceFolder,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium, overflow = TextOverflow.Ellipsis, maxLines = 1
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                   ,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "- " + "Destination", fontSize = 10.sp)
                            Text(
                                text = appStatistic.mostUsed.topDestinationFolder,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium, overflow = TextOverflow.Ellipsis, maxLines = 1
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                               ,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "- " + "File Type", fontSize = 10.sp)
                            Text(
                                text = appStatistic.MostMovedFileByType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium, overflow = TextOverflow.Clip, maxLines = 1
                            )
                        }
                    }

                }
            }
            item(appStatistic.timeSavedInMinutes) {
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