package com.avasoft.androiddemo.Pages.ChatList

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.BOs.ChatUserBO
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Pages.Room.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatListVM(app: Application): ViewModel() {
    private val db = Firebase.firestore
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)
    val email = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
    var recipient by mutableStateOf("")

    var touchPointRooms = mutableStateListOf<TouchpointRoom>()

    var selectedChat by mutableStateOf("")
    var blocked by mutableStateOf(false)

    init {
        Log.d("email", email)

        val touchPointRoomsColRef = db.collection("touchpoints")
            .document(email)
            .collection("touchpointrooms")

        touchPointRoomsColRef
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("chatApp", "Touch points - Listen failed.", error)
                    return@addSnapshotListener
                }

                for (dc in value!!.documentChanges) {
                    Log.d("chatApp", "Touch points - Listen success ${dc.document.data}")

                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            touchPointRooms.add(Gson().fromJson<TouchpointRoom>(Gson().toJson(dc.document.data), TouchpointRoom::class.java))
                            touchPointRooms = touchPointRooms.sortedByDescending { it.lastMessageTime }.toMutableStateList()
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val touchPointModified = Gson().fromJson<TouchpointRoom>(Gson().toJson(dc.document.data), TouchpointRoom::class.java)
                            touchPointRooms.remove(touchPointRooms
                                .first {
                                    it.roomId == touchPointModified.roomId
                                }
                            )
                            touchPointRooms.add(touchPointModified)
                            touchPointRooms = touchPointRooms.sortedByDescending { it.lastMessageTime }.toMutableStateList()
                        }
                        DocumentChange.Type.REMOVED -> {
                            touchPointRooms.remove(Gson().fromJson<TouchpointRoom>(Gson().toJson(dc.document.data), TouchpointRoom::class.java))
                        }
                    }
                }

                Log.d("chatApp", "Touch points - last Listen success. ${touchPointRooms}")

            }
    }


    /**
     * Create room
     */
    fun createRoom() {
        viewModelScope.launch(Dispatchers.IO) {

            /**
             * Check if this user already exist
             * if only user exist, continue to next process
             */

            val usersDocRef = db
                .collection("users")
                .document(recipient)

            usersDocRef
                .get()
                .addOnSuccessListener {

                    val receiver = Gson().fromJson<ChatUserBO>(Gson().toJson(it.data), ChatUserBO::class.java)
                    if(receiver != null){

                        Log.d("chatApp", "createRoom() - Valid recipient: ${receiver.email}")
                        val roomId = UUID.randomUUID().toString()

                        /**
                         * TODO - transaction make the below steps to be executed sequentially
                         */

                        /**
                         * create touch point for sender
                         */
                        createTouchPoint(email,recipient,roomId)

                        /**
                         * create touch point for receiver
                         */
                        createTouchPoint(recipient,email, roomId)
                    }
                    else
                        Log.d("chatApp", "createRoom() - Invalid recipient")

                    /**
                     * Empty the recipient state which empties the recipient text field
                     */
                    recipient = ""
                }
                .addOnFailureListener {
                    Log.d("chatApp", "createRoom() - Fetch user failed")
                }
        }
    }

    /**
     * Creates Touch point for the specified email
     */
    private fun createTouchPoint(email: String, recipient: String, roomId: String){
        viewModelScope.launch(Dispatchers.IO) {

            val touchPointDocRef = db
                .collection("touchpoints")
                .document(email)

            val touchPointRoomDocRef = touchPointDocRef
                .collection("touchpointrooms")
                .document(recipient)

            /**
             * Check if the touch point is exist for the email
             * if exist - check if touch point room collection is exist for recipient
             * not exist - create room & touch point room collection
             */
            touchPointDocRef
                .get()
                .addOnSuccessListener { touchPoint ->
                    Log.d("chatApp", "createTouchPoint() - Touch point - ${touchPoint.data}")
                    if(touchPoint.data != null){
                        /**
                         * check if touch point room collection is exist for recipient
                         * if exist - do nothing
                         * not exist - create room & touch point room collection
                         */
                        touchPointRoomDocRef
                            .get()
                            .addOnSuccessListener { room ->
                                Log.d("EXIST",room.toString())
                                if(room.data != null){
                                    Log.d("chatApp", "createTouchPoint() - Room already exist")
                                }
                                else{
                                    createTouchPointRoomsCollection(
                                        roomId = roomId,
                                        email = email,
                                        recipient = recipient
                                    )
                                }
                            }
                            .addOnFailureListener {
                                Log.d("chatApp", "createTouchPoint() - Fetch touchPoint for recipient failed")
                            }
                    }
                    else{
                        createTouchPointRoomsCollection(
                            roomId = roomId,
                            email = email,
                            recipient = recipient
                        )
                    }
                }
                .addOnFailureListener {
                    Log.d("chatApp", "createRoom() - Touch point fetch failed")
                }
        }
    }

    /**
     * Creates touch point rooms collection for the specified email
     */
    private fun createTouchPointRoomsCollection(roomId: String, email:String, recipient:String){
        viewModelScope.launch(Dispatchers.IO) {
            val touchPointDocRef = db
                .collection("touchpoints")
                .document(email)

            val roomsDocRef = db
                .collection("rooms")
                .document(roomId)

            val touchPointRoomDocRef = touchPointDocRef
                .collection("touchpointrooms")
                .document(recipient)

            val roomToCreate = Rooms(roomId, listOf())

            roomsDocRef
                .set(roomToCreate)
                .addOnSuccessListener {
                    touchPointDocRef.set(Touchpoints(userId = email))
                        .addOnSuccessListener {
                            touchPointRoomDocRef
                                .set(
                                    TouchpointRoom(
                                        roomId = roomId,
                                        receiverId = recipient,
                                        email = recipient,
                                        lastMessage = "",
                                        lastMessageTime = Timestamp.now(),
                                        blocked = false
                                    )
                                )
                                .addOnSuccessListener {
                                    Log.d("chatApp", "createTouchPointRoomsCollection() - created touch point room")
                                }
                                .addOnFailureListener {
                                    Log.d("chatApp", "createTouchPointRoomsCollection() - create touch point room failed")
                                }
                        }
                        .addOnFailureListener {
                            Log.d("chatApp", "createTouchPointRoomsCollection() - create touch point with userId failed")
                        }
                }
                .addOnFailureListener {
                    Log.d("chatApp", "createTouchPointRoomsCollection() - create room failed")
                }
        }
    }

    fun blockOrUnblockUser(isBlock: Boolean){

        viewModelScope.launch(Dispatchers.IO) {
            db
                .collection("touchpoints")
                .document(email)
                .collection("touchpointrooms")
                .document(selectedChat)
                .update("blocked", isBlock)
                .addOnSuccessListener {
                    blocked = false
                    selectedChat = ""
                }
                .addOnFailureListener {
                    Log.d("chatApp", "blockUser() - block user failed")
                }
        }
    }

    fun toSimpleString(date: Date) : String {
        val format = SimpleDateFormat("dd/MM/yyy")
        return format.format(date)
    }
}

data class Rooms(
    val roomId: String,
    val messages: List<Message>
)

data class Touchpoints(
    val userId: String,
)

data class TouchpointRoom(
    val roomId: String,
    val receiverId: String,
    val email: String,
    val lastMessage: String,
    val lastMessageTime: Timestamp,
    val blocked: Boolean
)

class ChatListVMFactory(val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatListVM(app) as T
    }
}