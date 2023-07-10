package com.example.fileorganizer

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fileorganizer.Utility.formatUriToUIString
import com.example.fileorganizer.model.UITaskRecord


@Composable
fun TaskItem(
    task: UITaskRecord,
    onClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
    onToggleState:()->Unit ={}
) {

    val nmbrOfChrs = task.extension.length

    val textSize =
        if (nmbrOfChrs < 3) 16.sp else if (nmbrOfChrs < 5) 14.sp else if (nmbrOfChrs < 8) 10.sp else 9.sp



    Row(
        modifier = Modifier
            .padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically

    ) {

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(40.dp))

                .clickable(
                    indication = rememberRipple(),
                    interactionSource = MutableInteractionSource(),
                    onClick = {
                        onClick()
                    }
                )

                .background(
                    colorResource(id = R.color.fiery_rose).copy(alpha = 0.04F),
                    shape = RoundedCornerShape(200.dp)
                )
            ,
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
                    task.extension,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize
                )

            }

            // source & dest. folders info layout
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(230.dp)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {

                FolderPath(stringResource(R.string.from), task.source)
                FolderPath(stringResource(R.string.to), task.destination)
            }
        }


        //selection state, check button
        
        Checkbox(checked  =task.isActive , onCheckedChange = { onToggleState() }, colors = CheckboxDefaults.colors(checkedColor = colorResource(
            id = R.color.fiery_rose
        )))

        //Delete button
        IconButton(onClick = {
            onRemoveClick()
        }) {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = stringResource(id = R.string.delete_item_with_id_and_extension,task.id,task.extension)
            )
        }
//
//        TextButton(
//            content = { Text(stringResource(R.string.edit)) },
//            colors = ButtonDefaults.buttonColors(
//                backgroundColor = colorResource(R.color.lavender_blush),
//                contentColor = colorResource(R.color.jet)
//            ), modifier = Modifier.padding(end = 8.dp),
//            onClick = {
//                onEditClick()
//            }
//        )
    }

}

@Composable
private fun FolderPath(source: String, path: String) {

//    val formattedDestination = if (destination.length > 16) {
//
//        val first = destination.substringBefore("/")
//        val last =
//            if (destination.contains(":") && destination.substringAfterLast(":").length > 2) {
//                destination.substringAfterLast(":").substringAfterLast("/")
//            } else destination.substringBeforeLast("/")
//        first + last
//    } else destination.replaceFirst("/tree","Root")

    val isRoot = Uri.parse(path).lastPathSegment.equals("primary:")
    val uiString = formatUriToUIString(path)
    val characterCount = uiString.length

    val formattedPath= if (isRoot)"Primary Root" else if (characterCount > 50) uiString.substringBefore(">") +">...>"+uiString.substringAfterLast(">") else uiString

    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

        Text(
            source, color = Color.Gray, modifier = Modifier
                .width(40.dp), maxLines = 2, fontSize = 12.sp
        )

        Text(
            " : ", color = colorResource(R.color.jet),
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Text(formattedPath, color = colorResource(R.color.jet), maxLines = 2, fontSize = 14.sp)
    }


}

@Composable
fun TaskListContent(
    tasksList: List<UITaskRecord>,
    onEditItmClicked: (UITaskRecord) -> Unit,
    onRemoveItem: (UITaskRecord) -> Unit,
    onToggleState: (UITaskRecord) -> Unit
) {


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.raisin_black).copy(0.00f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        items(tasksList) { task ->

            TaskItem(
                task = task,
                onClick = { onEditItmClicked(task) },
                onRemoveClick = { onRemoveItem(task) },
                onToggleState = { onToggleState(task) }
            )


        }


    }
}
