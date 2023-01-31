package com.avasoft.androiddemo.Pages.Room

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.BOs.ChatUserBO
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Pages.ChatList.TouchpointRoom
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
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
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    var attachmentUrl: Uri = Uri.EMPTY
    var attatchmentName = ""
    var bitmap by mutableStateOf<Bitmap?>(null)
    val FIVE_MEGABYTE: Long = (1024 * 1024) * 5
    var sentImage by mutableStateOf(false)
    var imgCaptured by mutableStateOf(false)
    var isRecipientOnline by mutableStateOf(false)
    var recipientLastOnline by mutableStateOf("")
    private val firestoreDb = Firebase.firestore

    init {
        try {
            firestoreDb.collection("users")
                .document(recipientEmail)
                .addSnapshotListener { value, error ->
                    if(error != null) {
                        Log.d("chatApp", "Chat room - Listen failed.", error)
                        return@addSnapshotListener
                    }

                    val data = Gson().fromJson<ChatUserBO>(Gson().toJson(value?.data), ChatUserBO::class.java)

                    isRecipientOnline = data.online
                }

            firestoreDb.collection("rooms")
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
                                    attachment = if(data.type.containsKey("A")) data.type["A"] as Map<String, Any> else null,
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
                                    attachment = if(data.type.containsKey("A")) data.type["A"] as Map<String, Any> else null,
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
                                    attachment = if(data.type.containsKey("A")) data.type["A"] as Map<String, Any> else null,
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
                }
                else if(attachmentUrl != Uri.EMPTY){
                    messageBody = Message(
                        id = messageId,
                        from = email,
                        roomId = roomId,
                        body = message,
                        type = mapOf("A" to (attatchmentName to attachmentUrl)),
                        time = Timestamp.now(),
                        isDeleted = false
                    )
                }
                else {
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

                        message = ""
                        replyMessage = null
                        attachmentUrl = Uri.EMPTY
                        attatchmentName = ""
                        imgCaptured = false
                    }
                    .addOnFailureListener {
                        Log.d("chatApp", "message - not linked to room")
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

    /**
     * Send image using putBytes() method
     */
    fun sendImage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imgRef = storageRef.child("files/${bitmap?.config?.name}_${bitmap?.generationId}.jpg")
                attatchmentName = "${bitmap?.config?.name}_${bitmap?.generationId}.jpg"
                val baos = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = imgRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    Log.d("chatApp", "sendAttachment() - attachment not uploaded")
                }.addOnSuccessListener { taskSnapshot ->
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    Log.d("chatApp", "sendAttachment() - attachment uploaded , contentType - ${taskSnapshot.metadata?.contentType}")
                }

                val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imgRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("chatApp", "sendAttachment() - URL - ${task.result}")
                        attachmentUrl = task.result
                        sendMessage()
                    } else {
                        Log.d("chatApp", "sendAttachment() - URL - failed")
                    }
                }
            }
            catch (ex: Exception){
                Log.d("ChatException", ex.message?:"Empty")
            }
        }
    }

    /**
     * Send attachments using putFile() function
     */
    fun sendAttachment(uri: Uri){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val attachmentRef = storageRef.child("files/${DocumentFile.fromSingleUri(app.applicationContext, uri)?.name?:""}")
                attatchmentName = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name?:""

                val uploadTask = attachmentRef.putFile(uri)
                uploadTask.addOnFailureListener {
                    Log.d("chatApp", "sendAttachment() - attachment not uploaded")
                }.addOnSuccessListener { taskSnapshot ->
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    Log.d("chatApp", "sendAttachment() - attachment uploaded , contentType - ${taskSnapshot.metadata?.contentType}")
                }

                val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    attachmentRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("chatApp", "sendAttachment() - URL - ${task.result}")
                        attachmentUrl = task.result
                        sendMessage()
                    } else {
                        Log.d("chatApp", "sendAttachment() - URL - failed")
                    }
                }
            }
            catch (ex: Exception){
                Log.d("ChatException", ex.message?:"Empty")
            }
        }
    }

    /**
     * download file using getFile() function
     */
    fun downloadFile(fileName: String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val attachmentRef = storageRef.child("files/$fileName")

                attachmentRef
                    .getFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName))
                    .addOnSuccessListener {
                        Log.d("chatApp", "downloadFile() - $it")
                    }
                    .addOnFailureListener{
                        Log.d("chatApp", "downloadFile() failed - $it")
                    }
            }
            catch (ex: Exception){
                Log.d("ChatException", ex.message?:"Empty")
            }
        }
    }

    /**
     * Get file from storage using getbytes() function
     * to show the sent image in the UI
     */
    fun showSentImage(fileName: String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(
                    fileName.contains("jpg") ||
                    fileName.contains("jpeg") ||
                    fileName.contains("png")
                ){
                    val fileRef = storageRef.child("files/${fileName}")

                    fileRef.getBytes(FIVE_MEGABYTE)
                        .addOnSuccessListener {
                            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                            sentImage = true
                        }
                        .addOnFailureListener {
                            Log.d("chatApp", "showSentImage() - getBytes failed")
                        }
                }
            }
            catch (ex: Exception){
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
    val attachment: Map<String, Any>?,
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