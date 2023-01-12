package com.avasoft.androiddemo.Pages.Room

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Pages.ChatList.Rooms
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class RoomVM(val roomId: String, private val app: Application): ViewModel() {

    var message by mutableStateOf("")
    var currentIndex by mutableStateOf(-1)
    var replyMessage by mutableStateOf<Message?>(null)
    var openMessageMenu by mutableStateOf(false)
    var messages = mutableStateListOf<Message>()
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)
    val email = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
    private val db = Firebase.firestore

    val dummyData = Message(
        id = "messageId",
        from = "email",
        roomId = "roomId",
        body = "message",
        type = mapOf("N" to "message"),
        time = Timestamp.now(),
        isDeleted = false
    )
    val dummyList = listOf(dummyData, dummyData, dummyData, dummyData, dummyData, dummyData, dummyData)

    init {
        db.collection("rooms")
            .document(roomId)
            .collection("messages")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("chatApp", "Chat room - Listen failed.", error)
                    return@addSnapshotListener
                }
                dummyList.forEach {
                    messages.add(it)
                }
//                for (dc in value!!.documentChanges) {
//                    when (dc.type) {
//                        DocumentChange.Type.ADDED -> {
//                            messages.add(Gson().fromJson<Message>(Gson().toJson(dc.document.data), Message::class.java))
//                        }
//                        DocumentChange.Type.MODIFIED -> Log.d("RealTimeDB", "Modified city: ${dc.document.data}")
//                        DocumentChange.Type.REMOVED -> {
//                            messages.remove(Gson().fromJson<Message>(Gson().toJson(dc.document.data), Message::class.java))
//                        }
//                    }
//                }
//                for (each in Gson().fromJson<Rooms>(Gson().toJson(value?.), Rooms::class.java).messages ?: listOf()){
//
//
//                    if(!messages.contains(each))
//                        messages.add(each)
//
////                    db.collection("messages")
////                        .document(each)
////                        .get()
////                        .addOnSuccessListener { document ->
////                            if (document != null) {
////                                Log.d("Message - DOC", document.data.toString())
////
////                                val messageResult = Gson().fromJson<Message>(Gson().toJson(document.data), Message::class.java)
////
////                            } else {
////                                Log.d("chatApp", "message - No such message")
////                            }
////                        }
////                        .addOnFailureListener { exception ->
////                            Log.d("chatApp", "message - get failed with ", exception)
////                        }
//                }
            }
    }

    fun sendMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            val messageId = UUID.randomUUID().toString()

            val messageBody = Message(
                id = messageId,
                from = email,
                roomId = roomId,
                body = message,
                type = mapOf("N" to message),
                time = Timestamp.now(),
                isDeleted = false
            )
//            Log.d("Message - body",messageBody.toString())
//            db.collection("messages")
//                .document(messageId)
//                .set(messageBody)
//                .addOnSuccessListener {
//                    Log.d("chatApp", "message - sent")
                    db.collection("rooms")
                        .document(roomId)
                        .collection("messages")
                        .document(messageId)
                        .set(messageBody)
                        .addOnSuccessListener {
                            message = ""
                            Log.d("chatApp", "message - linked to room")
                        }
                        .addOnFailureListener {
                            Log.d("chatApp", "message - not linked to room")
                        }
//                }
//                .addOnFailureListener {
//                    Log.d("chatApp", "message - not sent")
//                }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("rooms")
                .document(roomId)
                .collection("messages")
                .document(messageId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(app.applicationContext, "Deleted", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Log.d("chatApp", "message not deleted")
                }
        }
    }
}

data class Message(
    val id: String,
    val from: String,
    val roomId: String,
    val body: String,
    val type: Map<String, String>,
    val time: Timestamp,
    val isDeleted: Boolean
)

class RoomVMFactory(val roomId: String,val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RoomVM(roomId, app) as T
    }
}