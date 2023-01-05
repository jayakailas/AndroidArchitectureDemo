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
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapVM(app: Application, private val repository: LocalUserService): ViewModel() {

    var loadingState by mutableStateOf(false)
    var failurePopUp by mutableStateOf(false)
    var email by mutableStateOf("")
    var currentLat by mutableStateOf(0.0)
    var currentLng by mutableStateOf(0.0)
    var customLat by mutableStateOf(0.0)
    var customLng by mutableStateOf(0.0)
    var coordinatesList : MutableList<LatLng> = mutableListOf()
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)

    fun pageLoad(){
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val userEmail = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
                if(userEmail.isNotBlank()) {
                    val result = repository.getUserByEmail(email = userEmail)
                    currentLat = result.content?.currentLat?.toDouble()?:0.0
                    currentLng = result.content?.currentLong?.toDouble()?:0.0
                    customLat = result.content?.customLat?.toDouble()?:0.0
                    customLng = result.content?.customLong?.toDouble()?:0.0

                    coordinatesList = mutableListOf(
                        LatLng(currentLat, currentLng),
                        LatLng(customLat, customLng)
                    )
                }
                else{
                    failurePopUp = true
                }
            }
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            failurePopUp = true
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

class MapVMFactory(val app: Application, val repository: LocalUserService): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapVM(app, repository) as T
    }
}
