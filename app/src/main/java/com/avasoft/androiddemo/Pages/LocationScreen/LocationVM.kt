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
import com.avasoft.androiddemo.Services.UserService.ILocalUserService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class LocationVM(app : Application,private val userService: ILocalUserService, private val locationService: ILocationService = LocationService(), private val systemLocationService: ISystemLocationService = SystemLocationService(app.applicationContext)): AndroidViewModel(app){

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
    var failurePopUp by mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userEmail = sharedPreference.getString(GlobalConstants.USER_EMAIL, "")?:""
                isLoading.postValue(true)
                delay(2000)
                if(userEmail.isNotBlank()) {
                    val result = userService.getUserByEmail(userEmail)
                    if(result.status == ServiceStatus.Success && result.content != null){
                        user = result.content
                        if(user.currentLat.isNullOrBlank() || user.currentLong.isNullOrBlank()){
                            /**
                             * TODO check whether we need to move this permission launch to a unified service with proper
                             * returning functions
                             */
                            launchLocationPermission()
                        }
                        else{
                            currentLat = user.currentLat.toString()
                            currentLong = user.currentLong.toString()
                            customLat = user.customLat.toString()
                            customLong = user.customLong.toString()
                            distance = user.distance.toString()
                            getAddress(currentLat, currentLong)
                        }
                    }
                    else{
                        failurePopUp = true
                    }
                }
            }
            catch (ex: Exception){
                Log.d("Exception occurred", ex.toString())
                isLoading.postValue(false)
            }
        }
    }

    fun setUserCustomLat(lat: String){
        try {
            if(lat.isEmpty()){
                customLat = lat
            }
            else{
                customLat = when (lat.toDoubleOrNull()) {
                    null -> customLat //old value
                    else -> lat   //new value
                }
                user.customLat = when (lat.toDoubleOrNull()) {
                    null -> user.customLat //old value
                    else -> lat   //new value
                }
            }
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }

    fun setUserCustomLong(long: String){
        try {
            if(long.isEmpty()){
                customLong = long
            }
            else{
                customLong = when (long.toDoubleOrNull()) {
                    null -> customLong //old value
                    else -> long   //new value
                }
                user.customLong = when (long.toDoubleOrNull()) {
                    null -> user.customLong //old value
                    else -> long   //new value
                }
            }
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }

    fun calculateDistanceClicked(){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                if(customLat.isNotBlank() && customLong.isNotBlank()){
                    isLoading.postValue(true)
                    user.calculateDistance()
                    distance = user.distance.toString()
                    userService.updateUserData(user)
                    isLoading.postValue(false)
                }
            }
            catch (ex:Exception){
                Log.d("Exception occurred", ex.toString())
                isLoading.postValue(false)
            }
        }
    }

    private fun getAddress(lat: String, long: String){
        viewModelScope.launch {
            try{
                isLoading.postValue(true)
                val result = locationService.getAddress(lat, long,getApplication<Application>().applicationContext)
                Log.d("API", result.toString())
                if(result.status == ServiceStatus.Success){
                    currentAddress = result.content?:""
                }
                isLoading.postValue(false)
            }
            catch (ex:Exception){
                Log.d("Exception occurred", ex.toString())
                isLoading.postValue(false)
            }
        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun locationPermissionCallback(it : Map<String,Boolean>){
        try {
            when {
                it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    checkGPS()
                }
                it[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    getLastKnownLocation()
                }
                else -> {
                    failurePopUp = true
                }
            }
        }
        catch (ex:Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun gpsLaunchCallback(it: ActivityResult){
        try {
            if(it.resultCode == Activity.RESULT_OK){
                getCurrentLocation()
            }
            else failurePopUp = true
        }
        catch (ex:Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun checkGPS(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
                    failurePopUp = true
                }
            }
            catch (ex:Exception){
                Log.d("Exception occurred", ex.toString())
                failurePopUp = true
            }
        }
    }
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun getCurrentLocation(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = systemLocationService.currentLocation()
                if(result.status == ServiceStatus.Success && (result.content != null)){
                    val location = result.content
                    currentLat = location.latitude.toString()
                    currentLong = location.longitude.toString()
                    user.currentLat = location.latitude.toString()
                    user.currentLong = location.longitude.toString()
                    getAddress(currentLat, currentLong)
                }
                else {
                    failurePopUp = true
                }
            }
            catch (ex:Exception){
                Log.d("Exception occurred", ex.toString())
                failurePopUp = true
            }
        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    fun getLastKnownLocation(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = systemLocationService.getLastKnownLocation()
                if(result.status == ServiceStatus.Success && (result.content != null)){
                    val location = result.content
                    currentLat = location.latitude.toString()
                    currentLong = location.longitude.toString()
                    user.currentLat = location.latitude.toString()
                    user.currentLong = location.longitude.toString()
                    getAddress(currentLat, currentLong)
                }
                else {
                    failurePopUp = true
                }
            }
            catch (ex:Exception){
                Log.d("Exception occurred", ex.toString())
                failurePopUp = true
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun launchLocationPermission() {
        scope.launch {
            try {
                locationPermissionState?.launchMultiplePermissionRequest()
            }
            catch (ex:Exception){
                Log.d("Exception occurred", ex.toString())
            }
        }
    }

    fun closePopUp(){
        try {
            failurePopUp = false
        }
        catch (ex:Exception){
            Log.d("Exception occurred", ex.toString())
        }
    }
}

class LocationVMFactory(private val app: Application, val userService: ILocalUserService): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationVM(app, userService) as T
    }
}