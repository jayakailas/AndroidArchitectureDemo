package com.avasoft.androiddemo.BOs.UserBO

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserBO (
    @PrimaryKey
    val email: String,

    val password: String,
    var currentLat: String?,
    var currentLong: String?,
    var customLat: String?,
    var customLong: String?,
    var distance: String?
)