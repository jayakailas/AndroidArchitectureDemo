package com.avasoft.androiddemo.BOs.UserBO

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserBO (
    @PrimaryKey
    val email: String,

    val password: String,
    val currentLat: String?,
    val currentLong: String?,
    val customLat: String?,
    val customLong: String?
)