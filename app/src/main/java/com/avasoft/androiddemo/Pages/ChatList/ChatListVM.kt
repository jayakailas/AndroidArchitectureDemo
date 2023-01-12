package com.avasoft.androiddemo.Pages.ChatList

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.BOs.ChatUserBO
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Pages.Room.Message
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ChatListVM(app: Application): ViewModel() {
    val db = Firebase.firestore
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)
    val email = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
    var recipient by mutableStateOf("")
    var roomId by mutableStateOf("")

    var touchPointRooms = mutableStateListOf<TouchpointRoom>()

    init {
        Log.d("email", email)
        db.collection("touchpoints")
            .document(email)
            .collection("rooms")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("chatApp", "Touch points - Listen failed.", error)
                    return@addSnapshotListener
                }
                for (dc in value!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            touchPointRooms.add(Gson().fromJson<TouchpointRoom>(Gson().toJson(dc.document.data), TouchpointRoom::class.java))
                        }
                        DocumentChange.Type.MODIFIED -> Log.d("RealTimeDB", "Modified city: ${dc.document.data}")
                        DocumentChange.Type.REMOVED -> {
                            touchPointRooms.remove(Gson().fromJson<TouchpointRoom>(Gson().toJson(dc.document.data), TouchpointRoom::class.java))
                        }
                    }
                }
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

            db.collection("users")
                .document(recipient)
                .get()
                .addOnSuccessListener {

                    val receiver = Gson().fromJson<ChatUserBO>(Gson().toJson(it.data), ChatUserBO::class.java)
                    if(receiver != null){

                        Log.d("chatApp", "createRoom() - Valid recipient: ${receiver.email}")
                        val roomId = UUID.randomUUID().toString()
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
                }
                .addOnFailureListener {
                    Log.d("chatApp", "createRoom() - Fetch user failed")
                }
        }
    }

    private fun createTouchPoint(email: String, recipient: String, roomId: String){
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("touchpoints")
                .document(email)
                .get()
                .addOnSuccessListener { touchPoint ->
                    Log.d("chatApp", "createRoom() - Touch point - ${touchPoint.data}")
                    if(touchPoint.data != null){
                        db.collection("touchpoints")
                            .document(email)
                            .collection("rooms")
                            .document(recipient)
                            .get()
                            .addOnSuccessListener { room ->
                                Log.d("EXIST",room.toString())
                                if(room.data != null){
                                    Log.d("chatApp", "createRoom() - Room already exist")
                                }
                                else{
                                    val roomToCreate = Rooms(roomId, listOf())
                                    db.collection("rooms")
                                        .document(roomId)
                                        .set(roomToCreate)
                                        .addOnSuccessListener {
                                            db.collection("touchpoints")
                                                .document(email)
                                                .collection("rooms")
                                                .document(recipient)
                                                .set(roomToCreate)
                                                .addOnSuccessListener {  }
                                                .addOnFailureListener {  }
                                        }
                                        .addOnFailureListener { }
                                }
                            }
                            .addOnFailureListener {
                                Log.d("chatApp", "createRoom() - Fetch touchPoint failed")
                            }
                    }
                    else{
                        val roomToCreate = Rooms(roomId, listOf())
                        db.collection("rooms")
                            .document(roomId)
                            .set(roomToCreate)
                            .addOnSuccessListener {
                                db.collection("touchpoints")
                                    .document(email)
                                    .collection("rooms")
                                    .document(recipient)
                                    .set(TouchpointRoom(
                                        roomId = roomId,
                                        receiverId = recipient,
                                        email = recipient
                                    ))
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener {  }
                            }
                            .addOnFailureListener {  }
                    }
                }
                .addOnFailureListener {
                    Log.d("chatApp", "createRoom() - Touch point fetch failed")
                }
        }
    }
}

data class Rooms(
    val roomId: String,
    val messages: List<Message>
)

data class Touchpoints(
    val rooms: List<TouchpointRoom>
)

data class TouchpointRoom(
    val roomId: String,
    val receiverId: String,
    val email: String
)

class ChatListVMFactory(val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatListVM(app) as T
    }
}