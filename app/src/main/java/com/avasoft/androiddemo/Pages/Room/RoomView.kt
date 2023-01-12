package com.avasoft.androiddemo.Pages.Room

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avasoft.androiddemo.R

@Composable
fun RoomView(vm: RoomVM) {

    var message by remember {
        mutableStateOf("")
    }

    Column() {
        LazyColumn{
            items(vm.messages){ message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    backgroundColor = if(message.from == vm.email) Color.Green else Color.DarkGray,
                    contentColor = Color.White
                ) {
                    Column() {
                        Text(text = message.time)
                        Text(text = message.body)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextField(
            value = message,
            onValueChange = {
                message = it
            },
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    tint = MaterialTheme.colors.onBackground,
                    contentDescription = "",
                    modifier = Modifier
                        .clickable {
                            vm.sendMessage(message)
                        }
                )
            }
        )
    }
}