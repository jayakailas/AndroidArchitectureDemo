package com.avasoft.androiddemo.Services.UserService

import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Services.ServiceResult
import com.avasoft.androiddemo.Services.ServiceStatus

class LocalUserService(private val userDao: IUserService) {

    suspend fun createUser(data: UserBO): ServiceResult<UserBO> {
        val response = userDao.createUser(
            data = data
        )
        if(!response.equals(0)){
            return ServiceResult(ServiceStatus.Created, null, null)
        }
        return ServiceResult(ServiceStatus.ServerError, null, null)
    }

    suspend fun checkUserAlreadyExists(email: String): ServiceResult<Boolean> {
        val data = userDao.getUserData(email = email)

        if(data != null){
            return ServiceResult(ServiceStatus.Success, null, true)
        }
        return ServiceResult(ServiceStatus.NotFound, null, false)
    }

    suspend fun updateUserData(data: UserBO): ServiceResult<Int> {
        val data = userDao.updateUserData(data = data)

        if(data != 0){
            return ServiceResult(ServiceStatus.Success, null, data)
        }
        return ServiceResult(ServiceStatus.ServerError, null, null)
    }

    suspend fun validateUser(email: String, password: String): ServiceResult<Boolean>{
        val user = userDao.validateUser(email, password)
        if(user != null){
            return ServiceResult(ServiceStatus.Success, null, true)
        }
        return ServiceResult(ServiceStatus.NotFound, "User Not Found", null)
    }

    suspend fun getUserByEmail(email: String): ServiceResult<UserBO>{
        val data = userDao.getUserData(email = email)
        if(data != null){
            return ServiceResult(ServiceStatus.Success, null, data)
        }
        return ServiceResult(ServiceStatus.NotFound, null, null)
    }
}