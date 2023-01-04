package com.avasoft.androiddemo.Pages.LoginScreen

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Helpers.Utilities.EmailValidator.EmailValidator
import com.avasoft.androiddemo.Services.DemoDatabase
import com.avasoft.androiddemo.Services.ServiceStatus
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginVM(app: Application): AndroidViewModel(app) {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isEmailError by mutableStateOf(false)
    var isPasswordError by mutableStateOf(false)
    var passwordVisibility by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var failurePopUp by mutableStateOf(false)
    var userService: LocalUserService
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)
    var isLoadDone by mutableStateOf(false)

    init {
        val db = DemoDatabase.getInstance(app)
        val dao = db.userDao()
        userService = LocalUserService(dao)
    }

    fun onEmailFocusChange(isFocused: Boolean){
        try {
            if (isFocused) {
                isLoadDone = true
            } else {
                if (isLoadDone) {
                    setIsEmailError(email)
                }
            }
        } catch (ex: Exception) {
            // handle exception
        }
    }

    fun onPasswordFocusChange(isFocused: Boolean){
        try {
            if (isFocused) {
                isLoadDone = true
            } else {
                if (isLoadDone)
                    setIsPasswordError(password)
            }
        } catch (ex: Exception) {
            // handle exception
        }
    }

    fun setEmailAddress(email: String){
        try {
            this.email = email
        }
        catch (ex: Exception){

        }
    }

    fun setPassWord(password: String){
        try {
            this.password = password
        }
        catch (ex: Exception){

        }
    }

    fun setIsEmailError(email: String){
        try {
            isEmailError = email.isBlank() || !EmailValidator.isValidEmail(email)
        }
        catch (ex: Exception){

        }
    }

    fun setIsPasswordError(password: String){
        try {
            isPasswordError = password.isBlank()
        }
        catch (ex: Exception){

        }
    }

    fun changePasswordVisibility(){
        try {
            passwordVisibility = !passwordVisibility
        }
        catch (ex: Exception){

        }
    }

    fun loginClicked(onSuccess: (Boolean) -> Unit) {
            viewModelScope.launch(Dispatchers.IO) {
                try{
                    isEmailError = !EmailValidator.isValidEmail(email)
                    isPasswordError = password.isBlank()
                    if(!(isEmailError && isPasswordError)){
                        isLoading = true
                        val result = userService.validateUser(email, password)
                        if(result.status == ServiceStatus.Success){
                            isLoading = false
                            sharedPreference.edit().putString(GlobalConstants.USER_EMAIL, email).apply()
                            withContext(Dispatchers.Main) {
                                onSuccess(true)
                            }
                        }
                        else{
                            withContext(Dispatchers.Main) {
                                onSuccess(false)
                            }
                            isLoading = false
                            failurePopUp = true
                        }
                    }
                }
                catch (ex: Exception){
                    withContext(Dispatchers.Main) {
                        onSuccess(false)
                    }
                    isLoading = false
                    failurePopUp = true
                }
            }
    }

    fun closePopUp(){
        try {
            failurePopUp = false
        }
        catch (ex: Exception){

        }
    }
}

class LoginVMFactory(private val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginVM(app) as T
    }
}