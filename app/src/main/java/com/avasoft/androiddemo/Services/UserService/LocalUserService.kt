package com.avasoft.androiddemo.Services.UserService

import com.avasoft.androiddemo.BOs.UserBO.UserBO

class LocalUserService(private val userDao: IUserService) {

    suspend fun createUser(data: UserBO): Long {
        return userDao.createUser(
            data = data
        )
    }

    suspend fun checkUserAlreadyExists(email: String): UserBO {
        return userDao.getUserData(email = email)
    }

    suspend fun updateUserData(data: UserBO) {
        userDao.updateUserData(data = data)
    }
}