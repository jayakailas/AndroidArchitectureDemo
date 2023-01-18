package com.avasoft.androiddemo.Pages.ChatList

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avasoft.androiddemo.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListView(NavigateToRoom: (String, String) -> Unit,vm: ChatListVM) {

    Box() {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(56.dp)
            ){
                Text(
                    text = "Chats",
                    modifier = Modifier
                        .align(Alignment.Center),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if(vm.selectedChat.isNotBlank() && !vm.blocked){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outline_block_24),
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                vm.blockOrUnblockUser(true)
                            }
                    )
                }

                if(vm.selectedChat.isNotBlank() && vm.blocked){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_unblock),
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                vm.blockOrUnblockUser(false)
                            }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ){
                items(vm.touchPointRooms){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (vm.selectedChat == it.email) Color.Gray else Color.Unspecified)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (vm.selectedChat == it.email) {
                                            vm.selectedChat = ""
                                        } else if (vm.selectedChat.isNotBlank()) {
                                            vm.selectedChat = it.email
                                            vm.blocked = it.blocked
                                        } else if (!it.blocked)
                                            NavigateToRoom(it.roomId, it.receiverId)
                                    },
                                    onLongClick = {
                                        vm.selectedChat = it.email
                                        vm.blocked = it.blocked
                                    }
                                ),
                            Arrangement.SpaceBetween
                        ) {
                            Column() {
                                Text(
                                    text = it.email,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .padding(top = 20.dp, bottom = 10.dp)
                                )

                                Text(
                                    text = it.lastMessage,
                                    modifier = Modifier
                                        .padding(bottom = 20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = vm.toSimpleString(it.lastMessageTime.toDate()),
                                    modifier = Modifier
                                        .padding(top = 20.dp, bottom = 10.dp)
                                )

                                if(it.blocked){
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_outline_block_24),
                                        contentDescription = "",
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }

                        Divider(thickness = 0.5.dp)
                    }
                }
            }
        }
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(15.dp)
//                ,
//            horizontalAlignment = Alignment.End
//        ) {

            var animationState by remember {
                mutableStateOf(false)
            }

            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd),
            ) {

                androidx.compose.animation.AnimatedVisibility(
                    visible = animationState,
                    enter = slideInHorizontally(
                        initialOffsetX = { -(screenWidth.value.toInt() * 2) }
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -(screenWidth.value.toInt() * 2) }
                    ),
                    modifier = Modifier
                        .weight(1f)
                ) {
                    TextField(
                        value = vm.recipient,
                        onValueChange = {
                            vm.recipient = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                FloatingActionButton(
                    onClick ={},
                    modifier = Modifier
                        ,
                    backgroundColor = Color.Blue,
                    contentColor = Color.White
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_add_24),
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier
                            .clickable {
                                if(animationState){
                                    if(vm.recipient.isNotBlank())
                                        vm.createRoom()
                                }
                                animationState = !animationState
                                Log.d("animationState", "$animationState")
                            }
                    )
                }
            }
//        }
    }
}