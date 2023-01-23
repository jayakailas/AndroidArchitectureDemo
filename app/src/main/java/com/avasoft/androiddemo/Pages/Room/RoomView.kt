package com.avasoft.androiddemo.Pages.Room

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.avasoft.androiddemo.R
import com.avasoft.androiddemo.ui.theme.Purple500
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun RoomView(vm: RoomVM) {
    val listState = rememberLazyListState()
    val focusRequester = FocusRequester()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = vm.messages.size) {
        if(vm.messages.size > 0) {
            listState.scrollToItem(vm.messages.lastIndex)
        }
    }

    val context = LocalContext.current

    /**
     * launcher to open the device camera and it's callback to handle the captured image
     */
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        if(it != null){
            vm.bitmap = it
            vm.camPopUp = false
            vm.imgCaptured = true
        }
    }

    /**
     * Create launcher to launch the gallery
     */
    val galleryLauncher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->

        if(uri != null){
            // Convert imageUri to bitmap

            val inputStream =  context.contentResolver.openInputStream(uri ?: Uri.EMPTY)
            // convert input stream to byteArray
            val byteArray = inputStream?.readBytes()
            // byteArray to bitmap
            vm.bitmap = BitmapFactory.decodeByteArray(byteArray, 0 ,byteArray?.size?:0)
            vm.camPopUp = false
            vm.imgCaptured = true
        }
    }

    Box() {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Purple500)
            ){
                Text(
                    text = vm.recipientEmail,
                    modifier = Modifier
                        .align(Alignment.Center),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState
            ){
                itemsIndexed(vm.messages){ index, message ->

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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = if(message.from == vm.email) Alignment.End else Alignment.Start
                            ) {
                                if(message.replyMessage != null) {
                                    ListItem (
                                        text = {
                                            Text(text = message.replyMessage.body)
                                        },
                                        secondaryText = {
                                            Text(text = message.replyMessage.time.toDate().toString())
                                        },
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .fillMaxWidth(0.75f)
                                            .clickable {
                                                val index = vm.messages.indexOf(
                                                    vm.messages.first { chatMessage ->
                                                        chatMessage.id == message.replyMessage.id
                                                    }
                                                )

                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(index)
                                                }
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
                                    backgroundColor = if(message.from == vm.email) Purple500 else Color.DarkGray,
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(5.dp),
                                    elevation = animateDpAsState(
                                        if (dismissState.dismissDirection != null) 4.dp else 0.dp
                                    ).value
                                ) {
                                    ListItem (
                                        text = {
                                            Column {
                                                if(message.type.keys.contains("A")){
                                                    val innerMap = message.type["A"] as Map<String, Any>
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White)
                                                            .clickable {
                                                                if(message.from != vm.email){
                                                                    vm.downloadImage(innerMap["first"].toString())
                                                                }
                                                                else{
                                                                    vm.showSentImage(innerMap["first"].toString())
                                                                }
                                                            },
                                                        Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = innerMap["first"].toString(),
                                                            color = Color.DarkGray,
                                                        )
                                                        if(message.from != vm.email){
                                                            Icon(
                                                                painter = painterResource(id = R.drawable.ic_outline_download_24),
                                                                contentDescription = null,
                                                                tint = Color.Unspecified
                                                            )
                                                        }
                                                    }
                                                    Divider(thickness = 1.dp)
                                                }
                                                Text(text = message.body)
                                            }
                                        },
                                        secondaryText = {
                                            Text(text = message.time.toDate().toString())
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
                    Row(
                        modifier = Modifier,
                        Arrangement.SpaceBetween
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_attach_file_24),
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .clickable {

                                }
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_outline_photo_camera_24),
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .clickable {
                                    vm.camPopUp = true
                                }
                        )

                        Icon(
                            imageVector = Icons.Outlined.Send,
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .clickable {
                                    if (vm.message.isNotBlank()) {
                                        vm.sendMessage()
                                    }
                                }
                        )
                    }
                },
                placeholder = {
                    Text(text = "Enter message")
                }
            )
        }

        /**
         *
         * Image view
         */
        if(vm.imgCaptured) {

            Popup(
                onDismissRequest = {
                    vm.imgCaptured = false
                },
                properties = PopupProperties(dismissOnBackPress = true)
            ) {
                BackHandler(enabled = true) {
                    vm.imgCaptured = false
                }
                Surface(
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88323F4B))
                    ){
                        Image(
                            bitmap = vm.bitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.cancel),
                            contentDescription = "Cancel icon",
                            modifier = Modifier
                                .padding(20.dp)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    vm.imgCaptured = false
                                },
                            tint = Color.Unspecified
                        )
                        TextField(
                            value = vm.message,
                            onValueChange = {
                                vm.message = it
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .focusRequester(focusRequester),
                            trailingIcon = {
                                Row(
                                    modifier = Modifier,
                                    Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Send,
                                        tint = MaterialTheme.colors.onBackground,
                                        contentDescription = "",
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                            .clickable {
                                                vm.sendAttachment()
                                            }
                                    )
                                }
                            },
                            placeholder = {
                                Text(text = "Enter message")
                            }
                        )
                    }
                }
            }
        }

        /**
         * Image pop up
         * Options - Take Photo / Upload From Device
         */
        if(vm.camPopUp){
            Popup(
                onDismissRequest = {
                    vm.camPopUp = false
                },
                properties = PopupProperties(dismissOnBackPress = true)
            ) {
                BackHandler(enabled = true) {
                    vm.camPopUp = false
                }

                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88323F4B))){
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.Center),
                        shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp, end = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Button(
                                onClick = {
                                    cameraLauncher.launch(null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp)
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Take Photo",
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Upload From Device",
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        /**
         * Sent image viewer
         */
        if(vm.sentImage){
            Popup(
                onDismissRequest = {
                    vm.sentImage = false
                },
                properties = PopupProperties(dismissOnBackPress = true)
            ) {
                BackHandler(enabled = true) {
                    vm.sentImage = false
                }

                Surface(
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88323F4B))
                    ){
                        Image(
                            bitmap = vm.bitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.cancel),
                            contentDescription = "Cancel icon",
                            modifier = Modifier
                                .padding(20.dp)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    vm.sentImage = false
                                },
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}