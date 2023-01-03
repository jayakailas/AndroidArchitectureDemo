package com.avasoft.androiddemo.Pages.LocationConversionScreen

import android.app.Application
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
import kotlinx.coroutines.withContext
import com.avasoft.androiddemo.Helpers.AppConstants.Unit

class LocationConversionVM(app: Application, private val userService: LocalUserService): ViewModel() {

    var distanceInKm by mutableStateOf("")
    var distanceToShow by mutableStateOf("")

    var failurePopUp by mutableStateOf(false)

    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)

    fun pageLoad(){
        val userEmail = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
                failurePopUp = true
            }
        }
    }

    fun convert(unit: Unit) {
        try {
            if(distanceToShow.isNotBlank()){
                distanceToShow = when(unit){
                    Unit.Kilometer -> distanceInKm
                    Unit.Meter -> "${distanceInKm.toDouble() * Unit.Meter.inKM}"
                    Unit.Miles -> "${distanceInKm.toDouble() * Unit.Miles.inKM}"
                    Unit.Foot -> "${distanceInKm.toDouble() * Unit.Foot.inKM}"
                    Unit.Yard -> "${distanceInKm.toDouble() * Unit.Yard.inKM}"
                }
            }
        }
        catch (ex: Exception){
            failurePopUp = true
        }
    }

    fun closePopUp(){
        failurePopUp = false
    }
}

class LocationConversionVMFactory(val app: Application, val userService: LocalUserService): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationConversionVM(app, userService) as T
    }
}