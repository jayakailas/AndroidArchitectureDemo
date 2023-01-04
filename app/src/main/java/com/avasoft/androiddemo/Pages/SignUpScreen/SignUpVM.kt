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
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
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
    var failurePopUp by mutableStateOf(false)
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
                    checkIfEmailAlreadyExist()
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

    fun checkIfEmailAlreadyExist(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                val result = userService.checkUserAlreadyExists(email)
                if(result.status == ServiceStatus.Success){
                    isLoading = false
                    isEmailExist = result.content?:false
                }
                else{
                    isLoading = false
                    isEmailExist = result.content?:false
                }
            }
            catch (ex: Exception){
                isEmailExist = false
                isLoading = false
            }
        }
    }

    fun createClicked(onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                checkIfEmailAlreadyExist()
                isEmailError = !EmailValidator.isValidEmail(email)
                isPasswordError = password.isBlank()
                if(!isEmailError && !isEmailExist && !isPasswordError){
                    isLoading = true
                    val result = userService.createUser(UserBO(email, password, null, null, null, null, null))
                    if(result.status == ServiceStatus.Created){
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

class SignUpVMFactory(private val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignUpVM(app) as T
    }
}