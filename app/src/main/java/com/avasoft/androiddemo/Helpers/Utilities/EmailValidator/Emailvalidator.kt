package com.avasoft.androiddemo.Helpers.Utilities.EmailValidator

import android.util.Patterns

object EmailValidator{
    fun isValidEmail(email:String):Boolean{
        Patterns.PHONE
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }
}