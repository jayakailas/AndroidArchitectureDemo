package com.avasoft.androiddemo.BOs.UserBO

import androidx.room.Entity

@Entity
data class UserBO (
val email: String,
val password: String,
val currentLat: String,
val currentLong: String,
val customLat: String,
val customLong: String
)