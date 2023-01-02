package com.avasoft.androiddemo.Pages.SignUpScreen

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Helpers.Utilities.EmailValidator.EmailValidator
import com.avasoft.androiddemo.Services.DemoDatabase
import com.avasoft.androiddemo.Services.ServiceStatus
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpVM(app: Application): AndroidViewModel(app){
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isEmailError by mutableStateOf(false)
    var isEmailExist by mutableStateOf(false)
    var isPasswordError by mutableStateOf(false)
    var passwordVisibility by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    var userService: LocalUserService

    init {
        val db = DemoDatabase.getInstance(app)
        val dao = db.userDao()
        userService = LocalUserService(dao)
    }

    fun setEmailAddress(email: String){
        this.email = email
    }

    fun setPassWord(password: String){
        this.password = password
    }

    fun setIsEmailError(isError: Boolean){
        isEmailError = isError
    }

    fun setIsPasswordError(isError: Boolean){
        isPasswordError = isError
    }

    fun changePasswordVisibility(){
        passwordVisibility = !passwordVisibility
    }

    fun checkIfEmailAlreadyExist(){
        // Check email API
    }

    fun createClicked(onSuccess: (Boolean) -> Unit) {
        try {
            isEmailError = !EmailValidator.isValidEmail(email)
            isPasswordError = password.isBlank()
            if(!isEmailError && !isEmailExist && !isPasswordError){
                viewModelScope.launch(Dispatchers.IO) {
                    isLoading = true
                    val result = userService.createUser(UserBO(email, password, null, null, null, null))
                    if(result.status == ServiceStatus.Success){
                        isLoading = false
                        withContext(Dispatchers.Main) {
                            onSuccess(true)
                        }
                    }
                    else{
                        onSuccess(false)
                        isLoading = false
                    }
                }
            }
        }
        catch (ex: Exception){
            onSuccess(false)
            isLoading = false
        }
    }
}

class SignUpVMFactory(private val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignUpVM(app) as T
    }
}