package com.example.fileorganizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fileorganizer.Utility.MATCH_2_CHARS_AFTER_SIGN


@Composable
fun TaskItem(
    task: TaskOrder,
    onTaskClick: () -> Unit = {},
    onTaskEditClick: () -> Unit = {}
) {

    val fileType = task.extension.uppercase()
    val sourceFolder = task.source.substringAfterLast(":").replace("/"," > ")
    val destinationFolder = task.destination.substringAfterLast(":").replace("/"," > ")



    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            , horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically

    ) {

        Row(
            modifier = Modifier.clickable(
                indication = rememberRipple(),
                interactionSource = MutableInteractionSource(),
                onClick = {
                    onTaskClick()
                    println("$task clicked")
                }
            ).background(
                    colorResource(id = R.color.fiery_rose).copy(alpha = 0.04F),
                    shape = RoundedCornerShape(200.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // type/extension layout
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .width(8.dp)
                    .background(
                        shape = CircleShape,
                        color = colorResource(R.color.middle_blue_green).copy(0.4f)
                    )
            ) {
                Text(
                    fileType,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold
                )

            }

            // source & dest. folders info layout
            Column(
                modifier = Modifier
                    .width(250.dp)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {

                FolderPath("from", sourceFolder )
                FolderPath("to", destinationFolder)
            }
        }

        //Edit button
        TextButton(
            content = { Text(stringResource(R.string.edit)) },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(R.color.lavender_blush),
                contentColor = colorResource(R.color.jet)
            ), modifier = Modifier.padding(end = 8.dp),
            onClick = {
                onTaskEditClick()
                println("Edit button clicked for $task")
            }
        )
    }

}

@Composable
private fun FolderPath(source: String, destination: String) {

//    val formattedDestination = if (destination.length > 16) {
//
//        val first = destination.substringBefore("/")
//        val last =
//            if (destination.contains(":") && destination.substringAfterLast(":").length > 2) {
//                destination.substringAfterLast(":").substringAfterLast("/")
//            } else destination.substringBeforeLast("/")
//        first + last
//    } else destination.replaceFirst("/tree","Root")


    Row(horizontalArrangement = Arrangement.SpaceBetween) {

        Text(source, color = Color.Gray, modifier = Modifier
            .width(40.dp), maxLines = 2)

        Text(" : ", color = colorResource(R.color.jet),
            modifier = Modifier.padding(start = 2.dp, end = 2.dp)
        )
        Text(destination, color = colorResource(R.color.jet), maxLines = 2)
    }


}