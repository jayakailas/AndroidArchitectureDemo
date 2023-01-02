package com.avasoft.androiddemo.Pages.LoginScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.Services.DemoDatabase
import com.avasoft.androiddemo.Services.ServiceStatus
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginVM(app: Application): AndroidViewModel(app) {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isEmailError by mutableStateOf(false)
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

    fun loginClicked(onSuccess: (Boolean, String?) -> Unit) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                isLoading = true
                val result = userService.validateUser(email, password)
                if(result.status == ServiceStatus.Success){
                    isLoading = false
                    onSuccess(true, email)
                }
                else{
                    isLoading = false
                    onSuccess(false, null)
                }
            }
        }
        catch (ex: Exception){
            isLoading = false
            onSuccess(false, null)
        }
    }
}

class LoginVMFactory(private val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginVM(app) as T
    }
}