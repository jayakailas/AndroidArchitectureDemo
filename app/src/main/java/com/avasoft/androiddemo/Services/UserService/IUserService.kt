package com.avasoft.androiddemo.Services.UserService

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.avasoft.androiddemo.BOs.UserBO.UserBO

@Dao
interface IUserService {

    @Query("SELECT * FROM user_table WHERE email = :email")
    fun getUserData(email: String): UserBO

    @Insert
    suspend fun createUser(data: UserBO): Long

    @Update
    fun updateUserData(data: UserBO)

    @Query("SELECT * FROM user_table WHERE email = :email & password = :password")
    fun validateUser(email: String, password: String): UserBO
}