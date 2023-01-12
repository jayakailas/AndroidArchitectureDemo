package com.avasoft.androiddemo.Pages.ChatList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatListView(NavigateToRoom: (String) -> Unit,vm: ChatListVM) {

    LazyColumn(
        modifier = Modifier
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
}