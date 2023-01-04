package com.avasoft.androiddemo.Services.UserService

import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Services.ServiceResult

interface ILocalUserService {

    suspend fun createUser(data: UserBO): ServiceResult<UserBO>

    suspend fun checkUserAlreadyExists(email: String): ServiceResult<Boolean>

    suspend fun updateUserData(data: UserBO): ServiceResult<Int>

    suspend fun validateUser(email: String, password: String): ServiceResult<Boolean>

    suspend fun getUserByEmail(email: String): ServiceResult<UserBO>
}