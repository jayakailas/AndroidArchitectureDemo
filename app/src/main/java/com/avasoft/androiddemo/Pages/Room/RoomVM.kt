package com.avasoft.androiddemo.Pages.Room

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Pages.ChatList.Rooms
import com.avasoft.androiddemo.Pages.ChatList.Touchpoints
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class RoomVM(val roomId: String, app: Application): ViewModel() {

    var messages = mutableStateListOf<Message>()
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)
    val email = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
    private val db = Firebase.firestore

    init {
        db.collection("rooms")
            .document(roomId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("chatApp", "Chat room - Listen failed.", error)
                    return@addSnapshotListener
                }
                for (each in Gson().fromJson<Rooms>(Gson().toJson(value?.data), Rooms::class.java).messageIds ?: listOf()){
                    db.collection("messages")
                        .document(each)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                Log.d("Message - DOC", document.data.toString())
                                messages.add(Gson().fromJson<Message>(Gson().toJson(document.data), Message::class.java))
                            } else {
                                Log.d("chatApp", "message - No such message")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("chatApp", "message - get failed with ", exception)
                        }
                }
            }
    }

    fun sendMessage(message: String) {
        val messageId = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            val messageBody = Message(email, roomId, message, mapOf("N" to message), LocalDateTime.now(ZoneOffset.UTC).toString(), false)
            Log.d("Message - body",messageBody.toString())
            db.collection("messages")
                .document(messageId)
                .set(messageBody)
                .addOnSuccessListener {
                    Log.d("chatApp", "message - sent")
                    db.collection("rooms")
                        .document(roomId)
                        .update("messageIds", listOf(messageId))
                        .addOnSuccessListener {
                            Log.d("chatApp", "message - linked to room")
                        }
                        .addOnFailureListener {
                            Log.d("chatApp", "message - not linked to room")
                        }
                }
                .addOnFailureListener {
                    Log.d("chatApp", "message - not sent")
                }
        }
    }
}

data class Message(
    val from: String,
    val roomId: String,
    val body: String,
    val type: Map<String, String>,
    val time: String,
    val isDeleted: Boolean
)

class RoomVMFactory(val roomId: String,val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RoomVM(roomId, app) as T
    }
}