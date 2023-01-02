package com.avasoft.androiddemo.Pages.SignUpScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SignUpViewModel: ViewModel(){
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isEmailError by mutableStateOf(false)
    var isEmailExist by mutableStateOf(false)
    var isPasswordError by mutableStateOf(false)
    var passwordVisibility by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

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

    fun createClicked(){

    }
}