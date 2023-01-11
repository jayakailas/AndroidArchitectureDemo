package com.avasoft.androiddemo.Pages.ChatList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatListView(NavigateToRoom: (String) -> Unit,vm: ChatListVM) {

    if(vm.navigateToRoom && vm.roomId.isNotBlank()) {
        NavigateToRoom(vm.roomId)
    }
    LazyColumn(
        modifier = Modifier
            .padding(20.dp)
    ){
        items(vm.touchPointRooms){
            Text(
                text = it.email,
                modifier = Modifier
                    .clickable {
                        
                    }
            )
            Divider(thickness = 0.5.dp)
        }
    }
}