package com.avasoft.androiddemo.Pages.ChatList

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.BOs.ChatUserBO
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Pages.Room.Message
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.options
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatListVM(val app: Application): ViewModel() {
    private val db = Firebase.firestore
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)
    val email = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
    var recipient by mutableStateOf("")
    var touchPointRooms = mutableStateListOf<TouchpointRoom>()
    var selectedChat by mutableStateOf("")
    var blocked by mutableStateOf(false)
    private val firebaseDb = Firebase.database
    private val userRef = firebaseDb.getReference("users")
    private val connectivityManager = app.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {

        // Since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        val myConnectionsRef = firebaseDb.getReference("users/${email.split(".")[0]}/connections")

        // Stores the timestamp of my last disconnect (the last time I was seen online)
        val lastOnlineRef = firebaseDb.getReference("/users/${email.split(".")[0]}/lastOnline")

        val connectedRef = firebaseDb.getReference(".info/connected")
        val uuid = UUID.randomUUID().toString()
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue<Boolean>() ?: false
                Log.d("jk", "onDataChange - $connected")

                if (connected) {
                    val con = myConnectionsRef.child(uuid)

                    // When this device disconnects, remove it
                    con.onDisconnect().removeValue()

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP)

                    // Add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    con.setValue(java.lang.Boolean.TRUE)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Reatime", "Listener was cancelled at .info/connected")
            }
        })

//        val isOnline = isOnline(isOnline = false, lastOnline = ServerValue.TIMESTAMP)
//        userRef.onDisconnect().setValue(isOnline)
//        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
//
//            // When network is available
//            override fun onAvailable(network: Network) {
//                Log.d("jk", "Success")
//                val con = myConnectionsRef.child(uuid)
//
//                // When this device disconnects, remove it
//                con.onDisconnect().removeValue()
//
//                // When I disconnect, update the last time I was seen online
//                lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP)
//
//                // Add this device to my connections list
//                // this value could contain info about the device or a timestamp too
//                con.setValue(java.lang.Boolean.TRUE)
////                val data = isOnline(isOnline = true, lastOnline = ServerValue.TIMESTAMP)
////                userRef
////                    .child(email.split(".")[0])
////                    .setValue(data)
////                    .addOnSuccessListener {
////                        Log.d("realtime", "Success")
////                    }
////                    .addOnFailureListener {
////                        Log.d("realtime", "Failure ${it.message}")
////                    }
//            }
//
//            // when network connection is lost
//            override fun onLost(network: Network) {
//                firebaseDb.goOffline()
//            }
//
//            // When network is not available
//            override fun onUnavailable() {
//                firebaseDb.goOffline()
//            }
//        })

        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            catch (ex: Exception){
                Log.d("Exception occurred", ex.toString())
            }
        }
    }


    /**
     * Create room
     */
    fun createRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            catch (ex: Exception){
                Log.d("Exception occurred", ex.toString())
            }
        }
    }

    /**
     * Creates Touch point for the specified email
     */
    private fun createTouchPoint(email: String, recipient: String, roomId: String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            catch (ex: Exception){
                Log.d("Exception occurred", ex.toString())
            }
        }
    }

    /**
     * Creates touch point rooms collection for the specified email
     */
    private fun createTouchPointRoomsCollection(roomId: String, email:String, recipient:String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            catch (ex: Exception){
                Log.d("Exception occurred", ex.toString())
            }
        }
    }

    /**
     * To block or un block the user i.e., update the blocked field in touch point room
     * param: isBlock - block the user if true, un block otherwise
     */
    fun blockOrUnblockUser(isBlock: Boolean){

        viewModelScope.launch(Dispatchers.IO) {
            try {
                db
                    .collection("touchpoints")
                    .document(email)
                    .collection("touchpointrooms")
                    .document(selectedChat)
                    .update("blocked", isBlock)
                    .addOnSuccessListener {
                        Toast.makeText(app.applicationContext, "${if(isBlock) "blocked" else "unblocked"} user: $selectedChat", Toast.LENGTH_LONG).show()
                        blocked = false
                        selectedChat = ""
                    }
                    .addOnFailureListener {
                        Log.d("chatApp", "blockUser() - block user failed")
                    }
            }
            catch (ex: Exception){
                Log.d("Exception occurred", ex.toString())
            }
        }
    }

    /**
     * To convert date object into string of format - dd/MM/yyy
     */
    fun toSimpleString(date: Date) : String {
        return try {
            val format = SimpleDateFormat("dd/MM/yyy")
            format.format(date)
        } catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            ""
        }
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

data class isOnline(
    val isOnline : Boolean,
    val lastOnline : Map<String, String>
)

class ChatListVMFactory(val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatListVM(app) as T
    }
}