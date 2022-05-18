package com.example.fileorganizer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.*


@Composable
fun TaskItem(
    task: TaskOrder,
    onTaskClick: (TaskOrder) -> Unit = {},
    onTaskEditClick: (TaskOrder) -> Unit = {}
) {

    val fileType = task.type.uppercase()
    val sourceFolder = task.from
    val destinationFolder = task.to

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
                    onTaskClick(task)
                    println("$task clicked")
                }
            ).background(
                    colorResource(id = R.color.fiery_rose).copy(alpha = 0.03F),
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
                    .padding(start = 16.dp)
            ) {

                FolderPath("from", sourceFolder)
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
                onTaskEditClick(task)
                println("Edit button clicked for $task")
            }
        )
    }

}

@Composable
private fun FolderPath(source: String, destination: String) {

    val formattedDestination = if (destination.length > 16) {

        val first = destination.substringBefore("/")
        val last =
            if (destination.contains(":") && destination.substringAfterLast(":").length > 2) {
                destination.substringAfterLast(":").substringAfterLast("/")
            } else destination.substringBeforeLast("/")
        first + last
    } else destination.replaceFirst("/tree","Root")


    Row(horizontalArrangement = Arrangement.SpaceBetween) {

        Text(source, color = Color.Gray, modifier = Modifier
            .width(40.dp))

        Text(" : ", color = colorResource(R.color.jet),
            modifier = Modifier.padding(start = 2.dp, end = 2.dp)
        )
        Text("/$formattedDestination/", color = colorResource(R.color.jet), maxLines = 2)
    }


}