package com.avasoft.androiddemo.Helpers.Utilities.EmailValidator

import android.util.Patterns

object EmailValidator{
    fun isValidEmail(email:String):Boolean{
        return try {
            Patterns.PHONE
            Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        } catch (ex: Exception){
            false
        }
    }
}