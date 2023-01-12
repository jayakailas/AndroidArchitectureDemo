package com.avasoft.androiddemo.Pages.Room

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.avasoft.androiddemo.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomView(vm: RoomVM) {
    val listState = rememberLazyListState()
    val focusRequester = FocusRequester()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState
        ){
            itemsIndexed(vm.messages){ index, message ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = if(message.from == vm.email) Alignment.End else Alignment.Start
                ) {
                    Card(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth(0.75f)
                            .combinedClickable(
                                onClick = {

                                },
                                onDoubleClick = {

                                },
                                onLongClick = {
                                    vm.openMessageMenu = true
                                    vm.currentIndex = index
                                },
                            ),
                        backgroundColor = if(message.from == vm.email) Color.Blue else Color.DarkGray,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Column(
                            horizontalAlignment = if(message.from == vm.email) Alignment.End else Alignment.Start
                        ) {
                            Text(text = message.time.toDate().toString())
                            Text(text = message.body)
                        }
                    }

                    AnimatedVisibility(
                        visible = vm.openMessageMenu && vm.currentIndex == index,
                        enter = slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    vm.deleteMessage(message.id)
                                }
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.reply),
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    vm.replyMessage = message
                                    focusRequester.requestFocus()
                                }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = vm.replyMessage != null,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Card() {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = vm.replyMessage!!.time.toDate().toString())
                    Text(text = vm.replyMessage!!.body)
                }
            }
        }

        TextField(
            value = vm.message,
            onValueChange = {
                vm.message = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    tint = MaterialTheme.colors.onBackground,
                    contentDescription = "",
                    modifier = Modifier
                        .clickable {
                            if(vm.message.isNotBlank()) {
                                vm.sendMessage()
                            }
                        }
                )
            }
        )
    }
}