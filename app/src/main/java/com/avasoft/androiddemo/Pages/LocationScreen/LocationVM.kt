package com.avasoft.androiddemo.Pages.LocationScreen

import android.Manifest
import android.app.Activity
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Services.LocationService.LocationManager
import com.avasoft.androiddemo.Services.ServiceStatus
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationVM(app : Application, userService: LocalUserService, private val locationManager: LocationManager = LocationManager(app.applicationContext)): AndroidViewModel(app){

    @OptIn(ExperimentalPermissionsApi::class)
    var locationPermissionState: MultiplePermissionsState? = null

    var gpsLaunchRequest:  ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>? = null

    lateinit var user: UserBO

    lateinit var scope: CoroutineScope

    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)

    var currentLat by mutableStateOf("")
    var currentLong by mutableStateOf("")
    var customLat by mutableStateOf("")
    var customLong by mutableStateOf("")

    var isLoading = MutableLiveData<Boolean>(false)

    init {
        try {
            val userEmail = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
            viewModelScope.launch(Dispatchers.IO) {
                isLoading.postValue(true)
                delay(5000)
                if(userEmail.isNotBlank()) {
                    val result = userService.getUserByEmail(userEmail)
                    if(result.status == ServiceStatus.Success){
                        user = result.content!!
                        if(user.currentLat.isNullOrBlank() || user.currentLong.isNullOrBlank()){
                            launchLocationPermission()
                        }
                        else{
                            currentLat = user.currentLat.toString()
                            currentLong = user.currentLong.toString()
                        }
                    }
                }
                isLoading.postValue(false)
            }
        }
        catch (ex: Exception){

        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun locationPermissionCallback(it : Map<String,Boolean>){
        when {
            it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                checkGPS()
            }
            it[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                getLastKnownLocation()
            }
            else -> {
//                isFailurePopUp = true
            }
        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun gpsLaunchCallback(it: ActivityResult){
        if(it.resultCode == Activity.RESULT_OK){
            getCurrentLocation()
        }
//        else isFailurePopUp = true
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun checkGPS(){
        viewModelScope.launch {

            locationManager.checkGPS{ isSuccess, gpsIntent ->
                if(isSuccess){
                    getCurrentLocation()
                }
                else if(!isSuccess && gpsIntent != null){
                    scope.launch {
                        gpsLaunchRequest?.launch(IntentSenderRequest.Builder(gpsIntent).build())
                    }
                }
                else {
//                    isFailurePopUp = true
                }
            }
        }
    }
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun getCurrentLocation(){
        locationManager.currentLocation { location ->
            if(location != null){
                currentLat = location.latitude.toString()
                currentLong = location.longitude.toString()
            }
            else {
//                isFailurePopUp = true
            }
        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun getLastKnownLocation(){
        locationManager.getLastKnownLocation { location ->
            if(location != null){
                currentLat = location.latitude.toString()
                currentLong = location.longitude.toString()
            }
            else {
//                isFailurePopUp = true
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun launchLocationPermission() {
        scope.launch {
            locationPermissionState?.launchMultiplePermissionRequest()
        }
    }
}

class LocationVMFactory(private val app: Application, val userService: LocalUserService): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationVM(app, userService) as T
    }
}