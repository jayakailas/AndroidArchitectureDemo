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
import com.avasoft.androiddemo.Pages.ChatList.TouchpointRoom
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

class RoomVM(private val roomId: String, val recipientEmail: String, private val app: Application): ViewModel() {

    var message by mutableStateOf("")
    var currentMessageIndex by mutableStateOf(-1)
    var replyMessage by mutableStateOf<ChatMessage?>(null)
    var openMessageMenu by mutableStateOf(false)
    var messages = mutableStateListOf<ChatMessage>()
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)
    val email = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
    private val db = Firebase.firestore

    init {
        try {
            db.collection("rooms")
                .document(roomId)
                .collection("messages")
                .whereEqualTo("deleted", false)
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d("chatApp", "Chat room - Listen failed.", error)
                        return@addSnapshotListener
                    }

                    for (dc in value!!.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val data = Gson().fromJson<Message>(Gson().toJson(dc.document.data), Message::class.java)
                                Log.d("chatMessage", "added -> ${data.type}")
                                val chat = ChatMessage(
                                    id = data.id,
                                    replyMessage = if(data.type.containsKey("R")) Gson().fromJson<Message>(Gson().toJson(data.type["R"]), Message::class.java) else null,
                                    from = data.from,
                                    roomId = data.roomId,
                                    body = data.body,
                                    type = data.type,
                                    time = data.time,
                                    isDeleted = data.isDeleted,
                                )
                                messages.add(chat)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Log.d("RealTimeDB", "Modified city: ${dc.document.data}")
                                val data = Gson().fromJson<Message>(Gson().toJson(dc.document.data), Message::class.java)
                                Log.d("chatMessage", "modified -> ${data.type}")
                                val chat = ChatMessage(
                                    id = data.id,
                                    replyMessage = if(data.type.containsKey("R")) Gson().fromJson<Message>(Gson().toJson(data.type["R"]), Message::class.java) else null,
                                    from = data.from,
                                    roomId = data.roomId,
                                    body = data.body,
                                    type = data.type,
                                    time = data.time,
                                    isDeleted = data.isDeleted,
                                )
                                if(data.isDeleted) {
                                    messages.remove(chat)
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                val data = Gson().fromJson<Message>(Gson().toJson(dc.document.data), Message::class.java)
                                Log.d("chatMessage", "removed -> ${data.type}")
                                val chat = ChatMessage(
                                    id = data.id,
                                    replyMessage = if(data.type.containsKey("R")) Gson().fromJson<Message>(Gson().toJson(data.type["R"]), Message::class.java) else null,
                                    from = data.from,
                                    roomId = data.roomId,
                                    body = data.body,
                                    type = data.type,
                                    time = data.time,
                                    isDeleted = data.isDeleted,
                                )
                                messages.remove(chat)
                            }
                        }
                    }
                }
        } catch (ex: Exception) {
            Log.d("ChatException", ex.message?:"Empty")
        }
    }

    fun sendMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messageId = UUID.randomUUID().toString()
                val messageBody: Message

                if(replyMessage!= null) {
                    val mes = Message(
                        id = replyMessage?.id?:"",
                        from = replyMessage?.from?:"",
                        roomId = replyMessage?.roomId?:"",
                        body = replyMessage?.body?:"",
                        type = mapOf("N" to replyMessage?.body!!),
                        time = replyMessage?.time!!,
                        isDeleted = replyMessage?.isDeleted!!
                    )
                    messageBody = Message(
                        id = messageId,
                        from = email,
                        roomId = roomId,
                        body = message,
                        type = mapOf("R" to mes),
                        time = Timestamp.now(),
                        isDeleted = false
                    )
                } else {
                    messageBody = Message(
                        id = messageId,
                        from = email,
                        roomId = roomId,
                        body = message,
                        type = mapOf("N" to message),
                        time = Timestamp.now(),
                        isDeleted = false
                    )
                }

                val messageDocRef = db
                    .collection("rooms")
                    .document(roomId)
                    .collection("messages")
                    .document(messageId)

                messageDocRef
                    .set(messageBody)
                    .addOnSuccessListener {
                        Log.d("chatApp", "message - linked to room")

                        val timeStamp = Timestamp.now()
                        /**
                         * update last message and last message time in sender's touch point room
                         */
                        updateLastMessage(
                            email = email,
                            recipientEmail = recipientEmail,
                            message = message,
                            timeStamp = timeStamp
                        )

                        /**
                         * update last message and last message time in receiver's touch point room
                         */
                        updateLastMessage(
                            email = recipientEmail,
                            recipientEmail = email,
                            message = message,
                            timeStamp = timeStamp
                        )

                        /**
                         * Empties message text field
                         */
                        message = ""
                        replyMessage = null
                    }
                    .addOnFailureListener {
                        Log.d("chatApp", "message - not linked to room")
                        message = ""
                        replyMessage = null
                    }
            } catch (ex: Exception) {
                Log.d("ChatException", ex.message?:"Empty")
            }
        }
    }

    private fun updateLastMessage(email: String, recipientEmail: String, message: String, timeStamp: Timestamp){

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val touchPointRoomDocRef = db
                    .collection("touchpoints")
                    .document(email)
                    .collection("touchpointrooms")
                    .document(recipientEmail)

                touchPointRoomDocRef
                    .get()
                    .addOnSuccessListener {
                        if(!Gson().fromJson<TouchpointRoom>(Gson().toJson(it.data), TouchpointRoom::class.java).blocked){
                            touchPointRoomDocRef
                                .update(
                                    "lastMessage", message,
                                    "lastMessageTime",timeStamp
                                )
                                .addOnSuccessListener {
                                    Log.d("chatApp", "updateLastMessage() - last message updated")
                                }
                                .addOnFailureListener {
                                    Log.d("chatApp", "updateLastMessage() - last message not updated")
                                }
                        }
                    }
                    .addOnFailureListener {
                        Log.d("chatApp", "updateLastMessage() - get & check for blocked id failed")
                    }
            } catch (ex: Exception) {
                Log.d("ChatException", ex.message?:"Empty")
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val messagesColRef = db.collection("rooms")
                    .document(roomId)
                    .collection("messages")

                messagesColRef
                    .document(messageId)
                    .update("deleted", true)
                    .addOnSuccessListener {
                        Toast.makeText(app.applicationContext, "Deleted", Toast.LENGTH_LONG).show()
                        messagesColRef
                            .orderBy("time", Query.Direction.DESCENDING)
                            .whereEqualTo("deleted", false)
                            .limit(1)
                            .get()
                            .addOnSuccessListener {

                                Log.d("chatApp - DELETE", it.documents[0].data.toString())

                                var message = ""
                                var time = Timestamp.now()

                                if(it.documents[0].data != null){
                                    val lastMessage = Gson().fromJson<Message>(Gson().toJson(it.documents[0].data), Message::class.java)
                                    message = lastMessage.body
                                    time = lastMessage.time
                                }
                                /**
                                 * update last message and last message time in sender's touch point room
                                 */
                                updateLastMessage(
                                    email = email,
                                    recipientEmail = recipientEmail,
                                    message = message,
                                    timeStamp = time
                                )

                                /**
                                 * update last message and last message time in receiver's touch point room
                                 */
                                updateLastMessage(
                                    email = recipientEmail,
                                    recipientEmail = email,
                                    message = message,
                                    timeStamp = time
                                )
                            }
                            .addOnFailureListener {
                                Log.d("chatApp", "deleteMessage() - last message not updated ${it.message}")
                            }
                    }
                    .addOnFailureListener {
                        Log.d("chatApp", "deleteMessage() - message not deleted")
                    }
            } catch (ex: Exception) {
                Log.d("ChatException", ex.message?:"Empty")
            }
        }
    }
}

data class Message(
    val id: String,
    val from: String,
    val roomId: String,
    val body: String,
    val type: Map<String, Any>,
    val time: Timestamp,
    val isDeleted: Boolean
)

data class ChatMessage(
    val id: String,
    val replyMessage: Message?,
    val from: String,
    val roomId: String,
    val body: String,
    val type: Map<String, Any>,
    val time: Timestamp,
    val isDeleted: Boolean
)

class RoomVMFactory(val roomId: String, val recipientEmail: String, val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RoomVM(roomId, recipientEmail, app) as T
    }
}