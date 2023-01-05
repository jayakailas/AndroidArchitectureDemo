package com.avasoft.androiddemo.Services.UserService

import android.util.Log
import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Services.ServiceResult
import com.avasoft.androiddemo.Services.ServiceStatus

class LocalUserService(private val userDao: UserDao): ILocalUserService {

    override suspend fun createUser(data: UserBO): ServiceResult<UserBO> {
        try {
            val response = userDao.createUser(
                data = data
            )
            if(!response.equals(0)){
                return ServiceResult(ServiceStatus.Created, null, null)
            }
            return ServiceResult(ServiceStatus.ServerError, null, null)
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            return ServiceResult(ServiceStatus.ServerError, null, null)
        }
    }

    override suspend fun checkUserAlreadyExists(email: String): ServiceResult<Boolean> {
        try {
            val data = userDao.getUserData(email = email)

            if(data != null){
                return ServiceResult(ServiceStatus.Success, null, true)
            }
            return ServiceResult(ServiceStatus.NotFound, null, false)
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            return ServiceResult(ServiceStatus.ServerError, null, null)
        }
    }

    override suspend fun updateUserData(data: UserBO): ServiceResult<Int> {
        try {
            val data = userDao.updateUserData(data = data)

            if(data != 0){
                return ServiceResult(ServiceStatus.Success, null, data)
            }
            return ServiceResult(ServiceStatus.ServerError, null, null)
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            return ServiceResult(ServiceStatus.ServerError, null, null)
        }
    }

    override suspend fun validateUser(email: String, password: String): ServiceResult<Boolean>{
        try {
            val user = userDao.validateUser(email, password)
            if(user != null){
                return ServiceResult(ServiceStatus.Success, null, true)
            }
            return ServiceResult(ServiceStatus.NotFound, "User Not Found", null)
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            return ServiceResult(ServiceStatus.ServerError, null, null)
        }
    }

    override suspend fun getUserByEmail(email: String): ServiceResult<UserBO>{
        try {
            val data = userDao.getUserData(email = email)
            if(data != null){
                return ServiceResult(ServiceStatus.Success, null, data)
            }
            return ServiceResult(ServiceStatus.NotFound, null, null)
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            return ServiceResult(ServiceStatus.ServerError, null, null)
        }
    }
}