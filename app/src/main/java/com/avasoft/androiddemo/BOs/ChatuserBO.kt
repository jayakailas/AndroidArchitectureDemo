package com.avasoft.androiddemo.BOs

data class ChatUserBO(
    var email: String,
    var userId: String,
    val online: Boolean,
    val lastOnline: Map<String, String>
//    var meta: Map<String,Any>
)
