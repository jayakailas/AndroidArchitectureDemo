package com.avasoft.androiddemo.Pages.SignUpScreen

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.BOs.ChatUserBO
import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Helpers.Utilities.EmailValidator.EmailValidator
import com.avasoft.androiddemo.Services.DemoDatabase
import com.avasoft.androiddemo.Services.ServiceStatus
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

    private val database = Firebase.firestore

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
            Log.d("Exception occurred", ex.toString())
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
            Log.d("Exception occurred", ex.toString())
        }
    }

    fun setEmailAddress(email: String){
        try {
            this.email = email
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }

    fun setPassWord(password: String){
        try {
            this.password = password
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }

    fun setIsEmailError(email: String){
        try {
            isEmailError = email.isBlank() || !EmailValidator.isValidEmail(email)
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }

    fun setIsPasswordError(password: String){
        try {
            isPasswordError = password.isBlank()
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }

    fun changePasswordVisibility(){
        try {
            passwordVisibility = !passwordVisibility
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
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
                Log.d("Exception occurred", ex.toString())
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

                            /**
                             * Firestore
                             */

                            database.collection("users").document(email)
                                .set(ChatUserBO(email, email, mapOf("status" to "O")))
                                .addOnSuccessListener {
                                    onSuccess(true)
                                }
                                .addOnFailureListener {
                                    onSuccess(false)
                                    isLoading = false
                                    failurePopUp = true
                                }
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
                Log.d("Exception occurred", ex.toString())
            }
        }
    }

    fun closePopUp(){
        try {
            failurePopUp = false
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }
}

class SignUpVMFactory(private val app: Application): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignUpVM(app) as T
    }
}