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
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ChatListVM(app: Application): ViewModel() {
    val db = Firebase.firestore
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)
    val email = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
    var message by mutableStateOf("")
    var roomId by mutableStateOf("")
    val uuid = UUID.randomUUID().toString()

    var touchPointRooms = mutableStateListOf<TouchpointRoom>()

//    private val data1 = TouchpointRoom(
//        roomId = uuid,
//        receiverId = "a15@a.com",
//        email = "a15@a.com"
//    )
//
//    private val touchpoint = Touchpoints(
//        userId = email,
//        rooms = listOf(data1)
//    )
//
//    private val room = Rooms(
//        roomId = uuid,
//        messageIds = emptyList()
//    )

    init {
        Log.d("email", email)
        db.collection("touchpoints")
            .document(email)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("chatApp", "Touch points - Listen failed.", error)
                    return@addSnapshotListener
                }
                Log.d("Touch","$value")
                for (each in Gson().fromJson<Touchpoints>(Gson().toJson(value?.data), Touchpoints::class.java).rooms ?: listOf()){
                    touchPointRooms.add(each)
                }
            }
    }


    /**
     * Create room - test
     */
//    fun createRoom() {
//        viewModelScope.launch(Dispatchers.IO) {
//            db.collection("rooms")
//                .document(uuid)
//                .set(room)
//                .addOnSuccessListener {
//                    Log.d("chatApp", "room - Success")
//                    db.collection("touchpoints")
//                        .document(email)
//                        .set(touchpoint)
//                        .addOnSuccessListener {
//                            Log.d("chatApp", "touchpoint - Success")
//                        }
//                        .addOnFailureListener {
//                            Log.d("chatApp", "touchpoint - Failure")
//                        }
//                }
//                .addOnFailureListener {
//                    Log.d("chatApp", "room - Failure")
//                }
//
//            navigateToRoom = true
//        }
//    }
}

data class Rooms(
    val roomId: String,
    val messageIds: List<String>
)

data class Touchpoints(
    val userId: String,
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