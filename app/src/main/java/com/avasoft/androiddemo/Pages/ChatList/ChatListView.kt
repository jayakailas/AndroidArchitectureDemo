package com.avasoft.androiddemo.Pages.ChatList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.R

@Composable
fun ChatListView(NavigateToRoom: (String) -> Unit,vm: ChatListVM) {

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(20.dp)
        ){
            item{
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Chats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(vm.touchPointRooms){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            NavigateToRoom(it.roomId)
                        }
                ) {
                    Text(
                        text = it.email,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                    )
                    Divider(thickness = 0.5.dp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {

            TextField(
                value = vm.recipient,
                onValueChange = {
                    vm.recipient = it
                },
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_add_24),
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .clickable {
                                if(vm.recipient.isNotBlank())
                                    vm.createRoom()
                            }
                    )
                }
            )
        }
    }
}