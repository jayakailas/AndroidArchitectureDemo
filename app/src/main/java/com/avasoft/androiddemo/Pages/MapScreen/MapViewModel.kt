package com.avasoft.androiddemo.Pages.MapScreen

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapVM(app: Application, repository: LocalUserService): ViewModel() {

    var loadingState by mutableStateOf(false)
    var email by mutableStateOf("")
    var currentLat by mutableStateOf("")
    var currentLng by mutableStateOf("")
    var customLat by mutableStateOf("")
    var customLng by mutableStateOf("")
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)

    init {
        val userEmail = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
        Log.d("userEmail", userEmail)
        viewModelScope.launch(Dispatchers.IO) {
            if(userEmail.isNotBlank()) {
                loadingState = true
                val result = repository.checkUserAlreadyExists(email = userEmail)
                currentLat = result.content?.currentLat?:""
                currentLng = result.content?.currentLong?:""
                customLat = result.content?.customLat?:""
                customLng = result.content?.customLong?:""
                loadingState = false
            }
        }
    }


}

class MapVMFactory(val app: Application, val repository: LocalUserService): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapVM(app, repository) as T
    }
}
