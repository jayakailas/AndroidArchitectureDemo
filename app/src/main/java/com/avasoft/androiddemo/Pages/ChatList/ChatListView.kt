package com.avasoft.androiddemo.Pages.ChatList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatListView(NavigateToRoom: (String) -> Unit,vm: ChatListVM) {

    if(vm.navigateToRoom && vm.roomId.isNotBlank()) {
        NavigateToRoom(vm.roomId)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
//        OutlinedTextField(
//            value = vm.message,
//            onValueChange = {
//                vm.message = it
//            },
//            label = {
//                Text(text = "Enter message")
//            }
//        )


        Button(
            onClick = { vm.createRoom() }
        ) {
            Text("Send Chat")
        }
    }
}