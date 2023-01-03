package com.avasoft.androiddemo.Pages.LocationScreen

import android.Manifest
import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.BOs.UserBO.calculateDistance
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Services.LocationService.ILocationService
import com.avasoft.androiddemo.Services.LocationService.ISystemLocationService
import com.avasoft.androiddemo.Services.LocationService.SystemLocationService
import com.avasoft.androiddemo.Services.LocationService.LocationService
import com.avasoft.androiddemo.Services.ServiceStatus
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class LocationVM(app : Application,private val userService: LocalUserService, private val locationService: ILocationService = LocationService(), private val systemLocationService: ISystemLocationService = SystemLocationService(app.applicationContext)): AndroidViewModel(app){

    @OptIn(ExperimentalPermissionsApi::class)
    var locationPermissionState: MultiplePermissionsState? = null

    var gpsLaunchRequest:  ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>? = null

    lateinit var user: UserBO

    lateinit var scope: CoroutineScope

    val sharedPreference = app.applicationContext.getSharedPreferences(GlobalConstants.USER_SHAREDPREFERENCE,0)

    var currentLat by mutableStateOf("")
    var currentLong by mutableStateOf("")
    var currentAddress by mutableStateOf("")

    var customLat by mutableStateOf("")
    var customLong by mutableStateOf("")
    var distance by mutableStateOf("")

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
                            getAddress(currentLat, currentLong)
                        }
                    }
                }
                isLoading.postValue(false)
            }
        }
        catch (ex: Exception){
            isLoading.postValue(false)
        }
    }

    fun setUserCustomLat(lat: String){
        customLat = when (lat.toDoubleOrNull()) {
            null -> customLat //old value
            else -> lat   //new value
        }
        user.customLat = when (lat.toDoubleOrNull()) {
            null -> user.customLat //old value
            else -> lat   //new value
        }
    }

    fun setUserCustomLong(long: String){
        customLong = when (long.toDoubleOrNull()) {
            null -> customLong //old value
            else -> long   //new value
        }
        user.customLong = when (long.toDoubleOrNull()) {
            null -> user.customLong //old value
            else -> long   //new value
        }
    }

    fun calculateDistanceClicked(){
        try{
            viewModelScope.launch(Dispatchers.IO) {
                if(customLat.isNotBlank() && customLong.isNotBlank()){
                    isLoading.postValue(true)
                    user.calculateDistance()
                    distance = user.distance.toString()

                    val result = userService.updateUserData(user)
                    if(result.status == ServiceStatus.Success){
                        isLoading.postValue(false)
                    }
                    else{
                        isLoading.postValue(false)
                    }
                }
            }
        }
        catch (ex:Exception){
            isLoading.postValue(false)
        }
    }

    private fun getAddress(lat: String, long: String){
        viewModelScope.launch {
            val result = locationService.getAddress(lat, long,getApplication<Application>().applicationContext)
            Log.d("API", result.toString())
            if(result.status == ServiceStatus.Success){
                currentAddress = result.content?.getAddressLine(0).toString()
            }
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
        viewModelScope.launch(Dispatchers.IO) {
            val result = systemLocationService.checkGPS()
            if(result.status == ServiceStatus.Success){
                    getCurrentLocation()
                }
                else if(result.status == ServiceStatus.ServerError && result.content != null){
                    scope.launch {
                        gpsLaunchRequest?.launch(IntentSenderRequest.Builder(result.content).build())
                    }
                }
                else {
//                    isFailurePopUp = true
                }
        }
    }
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun getCurrentLocation(){
        viewModelScope.launch(Dispatchers.IO) {
            val result = systemLocationService.currentLocation()
            if(result.status == ServiceStatus.Success){
                val location = result.content
                if(location != null){
                    currentLat = location.latitude.toString()
                    currentLong = location.longitude.toString()
                    user.currentLat = location.latitude.toString()
                    user.currentLong = location.longitude.toString()
                    getAddress(currentLat, currentLong)
                }
                else {
//                isFailurePopUp = true
                }
            }
            else {
//                isFailurePopUp = true
            }
        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun getLastKnownLocation(){
        viewModelScope.launch(Dispatchers.IO) {
            val result = systemLocationService.getLastKnownLocation()
            if(result.status == ServiceStatus.Success){
                val location = result.content
                if(location != null){
                    currentLat = location.latitude.toString()
                    currentLong = location.longitude.toString()
                    user.currentLat = location.latitude.toString()
                    user.currentLong = location.longitude.toString()
                    getAddress(currentLat, currentLong)
                }
                else {
//                isFailurePopUp = true
                }
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