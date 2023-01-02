package com.avasoft.androiddemo.Services.UserService

import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Services.ServiceResult
import com.avasoft.androiddemo.Services.ServiceStatus

class LocalUserService(private val userDao: IUserService) {

    suspend fun createUser(data: UserBO): Long {
        return userDao.createUser(
            data = data
        )
    }

    suspend fun checkUserAlreadyExists(email: String): ServiceResult<UserBO> {
        val data = userDao.getUserData(email = email)

        return ServiceResult(
            status = ServiceStatus.Success,
            message = null,
            content = data
        )
    }

    suspend fun updateUserData(data: UserBO) {
        userDao.updateUserData(data = data)
    }

    suspend fun validateUser(email: String, password: String): ServiceResult<Boolean>{
        val user = userDao.validateUser(email, password)
        if(user != null){
            return ServiceResult(ServiceStatus.Success, null, true)
        }
        return ServiceResult(ServiceStatus.NotFound, "User Not Found", null)
    }
}