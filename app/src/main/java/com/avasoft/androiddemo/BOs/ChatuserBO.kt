package com.avasoft.androiddemo.BOs

data class ChatUserBO(
    var email: String,
    var userId: String,
    var meta: Map<String,String>
)
