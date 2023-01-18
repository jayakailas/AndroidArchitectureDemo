package com.avasoft.androiddemo.Pages.Room

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avasoft.androiddemo.R

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
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
                    var unread by remember { mutableStateOf(false) }

                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            if (it == DismissValue.DismissedToEnd) {
                                vm.replyMessage = message
                                focusRequester.requestFocus()
                            }
                            it != DismissValue.DismissedToEnd
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        modifier = Modifier.padding(vertical = 4.dp),
                        directions = setOf(DismissDirection.StartToEnd),
                        background = {
                            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
//                            val color by animateColorAsState(
//                                when (dismissState.targetValue) {
//                                    DismissValue.Default -> Color.LightGray
//                                    DismissValue.DismissedToEnd -> Color.Green
//                                    DismissValue.DismissedToStart -> Color.Red
//                                }
//                            )
                            val alignment = when (direction) {
                                DismissDirection.StartToEnd -> Alignment.CenterStart
                                DismissDirection.EndToStart -> Alignment.CenterEnd
                            }
                            val icon = when (direction) {
                                DismissDirection.StartToEnd -> Icons.Default.Done
                                DismissDirection.EndToStart -> Icons.Default.Delete
                            }
                            val scale by animateFloatAsState(
                                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                            )

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.reply),
                                    contentDescription = "Localized description",
                                    modifier = Modifier.scale(scale)
                                )
                            }
                        },
                        dismissContent = {
                            Column {
                                if(message.replyMessage != null) {
                                    ListItem (
                                        text = {
                                            Text(text = message.time.toDate().toString())
                                        },
                                        secondaryText = {
                                            Text(text = message.body)
                                        }
                                    )
                                }
                                Card(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .fillMaxWidth(0.75f)
                                        .combinedClickable(
                                            onClick = {
                                                vm.openMessageMenu = false
                                                vm.currentMessageIndex = -1
                                            },
                                            onDoubleClick = {

                                            },
                                            onLongClick = {
                                                vm.openMessageMenu = true
                                                vm.currentMessageIndex = index
                                            },
                                        ),
                                    backgroundColor = if(message.from == vm.email) Color.Blue else Color.DarkGray,
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(5.dp),
                                    elevation = animateDpAsState(
                                        if (dismissState.dismissDirection != null) 4.dp else 0.dp
                                    ).value
                                ) {
                                    ListItem (
                                        text = {
                                            Text(text = message.time.toDate().toString())
                                        },
                                        secondaryText = {
                                            Text(text = message.body)
                                        }
                                    )
                                }

                                AnimatedVisibility(
                                    visible = vm.openMessageMenu && vm.currentMessageIndex == index,
                                    enter = slideInVertically(),
                                    exit = fadeOut()
                                ) {
                                    Row {
                                        if(message.from == vm.email){
                                            Icon(
                                                painter = painterResource(id = R.drawable.delete),
                                                contentDescription = null,
                                                modifier = Modifier.clickable {
                                                    vm.openMessageMenu = false
                                                    vm.currentMessageIndex = -1
                                                    vm.deleteMessage(message.id)
                                                },
                                                tint = Color.Red
                                            )
                                        }

                                        Icon(
                                            painter = painterResource(id = R.drawable.reply),
                                            contentDescription = null,
                                            modifier = Modifier.clickable {
                                                vm.replyMessage = message
                                                focusRequester.requestFocus()
                                                vm.openMessageMenu = false
                                                vm.currentMessageIndex = -1
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = vm.replyMessage != null,
            enter = slideInVertically(),
            exit = slideOutVertically(targetOffsetY = { 1 }),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(backgroundColor = Color.LightGray) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ListItem (
                        text = {
                            Text(text = vm.replyMessage?.time?.toDate().toString())
                        },
                        secondaryText = {
                            Text(text = vm.replyMessage?.body?:"")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            vm.replyMessage = null
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel),
                            contentDescription = null
                        )
                    }
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
            },
            placeholder = {
                Text(text = "Enter message")
            }
        )
    }
}