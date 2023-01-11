package com.avasoft.androiddemo.Pages.LocationConversionScreen

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Services.ServiceStatus
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.avasoft.androiddemo.Helpers.AppConstants.Unit
import com.avasoft.androiddemo.Helpers.Utilities.Convertors.DistanceConvertor

class LocationConversionVM(app: Application, private val userService: LocalUserService): ViewModel() {

    var distanceInKm by mutableStateOf("")
    var distanceToShow by mutableStateOf("")
    var failurePopUp by mutableStateOf(false)
    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)

    fun pageLoad(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userEmail = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
                if(userEmail.isNotBlank()) {
                    val result = userService.getUserByEmail(email = userEmail)
                    if(result.status == ServiceStatus.Success){
                        distanceInKm = result.content?.distance?:""
                        distanceToShow = result.content?.distance?:""
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
    }

    fun convert(unit: Unit) {
        try {
            if(distanceToShow.isNotBlank()){
                distanceToShow = DistanceConvertor.convert(distanceInKm.toDouble(), unit).toString()
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

class LocationConversionVMFactory(val app: Application, val userService: LocalUserService): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationConversionVM(app, userService) as T
    }
}