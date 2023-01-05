package com.avasoft.androiddemo.Services.LocationService

import android.app.PendingIntent
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import com.avasoft.androiddemo.Services.ServiceResult
import com.avasoft.androiddemo.Services.ServiceStatus
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 *This is the manager used to access all the location related operations
 */
class SystemLocationService(context: Context): ISystemLocationService{
    private val context = context

    /**
     * TODO This is the method used to check and launch the GPS launch request menu for the user
     * onResult(Boolean, PendingIntent) - >
     * Boolean -> Result of the checkGPS function
     * PendingIntent (nullable) -> Incase the exception from the system returns a pending intent to launch a request
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun checkGPS(): ServiceResult<PendingIntent>
    {
        return suspendCancellableCoroutine<ServiceResult<PendingIntent>> { continuation ->
            try {
                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 2000).build()
                val locationSettingsRequest =
                    LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                        .setAlwaysShow(true).build()

                // This will trigger to check whether the GPS is turned on or not
                val req = LocationServices.getSettingsClient(context)
                    .checkLocationSettings(locationSettingsRequest)

                /**
                 * The below success and failure listeners will be called after check whether GPS is turned or not.
                 * Success -> GPS is turned on
                 * Failure -> GPS is not turned on
                 */
                req.addOnSuccessListener {
                    /**
                     * If the Success callback is hit, it means the GPS is turned on.
                     */
                    if(it.locationSettingsStates?.isLocationUsable == true){
                        continuation.resume(ServiceResult(ServiceStatus.Success,null,null),null)
                    }
                    else{
                        continuation.resume(ServiceResult(ServiceStatus.ServerError,null,null),null)
                    }
                }
                req.addOnFailureListener { ex ->
                    /**
                     * If the failure callback is hit, it means that the GPS is not turned on.
                     * This means that we have to launch the 'turn GPS on' request to ask the user for GPS turn on.
                     */
                    if (ex is ResolvableApiException) {
                        val pendingIntent = ex.resolution
                        /**
                         * The pendingintent from the exception variable given by the system is used to launch the request.
                         */
                        continuation.resume(ServiceResult(ServiceStatus.ServerError,null,pendingIntent),null)
                    } else {
                        /**
                         * We cannot handle other exceptions [So the checkGPS function has failed]
                         */
                        continuation.resume(ServiceResult(ServiceStatus.ServerError,null,null),null)
                    }
                }
            } catch (ex: Exception) {
                /**
                 * Both the result is false, and pending intent is null -> means that the checkGPS function has entirely failed
                 * Means that we have to intimate the user that 'Some error has occurred'.
                 */
                Log.d("Exception occurred", ex.toString())
                continuation.resume(ServiceResult(ServiceStatus.ServerError,null,null),null)
            }
        }
    }


    /**
     * TODO Used to get the last known location from the fused location provider [Google play services]
     */
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    override suspend fun getLastKnownLocation(): ServiceResult<Location>{
        return suspendCancellableCoroutine<ServiceResult<Location>> { continuation ->
            try {
                val locationClient = LocationServices.getFusedLocationProviderClient(context)
                val lastLocation = locationClient.lastLocation
                lastLocation.addOnCompleteListener {
                    /**
                     * Most of the time the last known location will be available.
                     * But for extraordinary situations when we do not get the value we have to add a null check at the block where the callback is defined
                     */
                    continuation.resume(ServiceResult(ServiceStatus.Success,null,it.result),null)
                }
            }
            catch (ex:Exception){
                Log.d("Exception occurred", ex.toString())
                continuation.resume(ServiceResult(ServiceStatus.ServerError,null,null),null)
            }
        }
    }

    /**
     * TODO Used to get the current Location from the fused location provider [google play services]
     * This should be called only after the GPS is turned on. Or else it will provide null value most of the time
     */
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"])
    override suspend fun currentLocation(): ServiceResult<Location>{
        return suspendCancellableCoroutine<ServiceResult<Location>> { continuation ->
            try {
                val locationReqBuilder = CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).build()
                val locationClient = LocationServices.getFusedLocationProviderClient(context)
                val currentLocationFix = locationClient.getCurrentLocation(locationReqBuilder, null)

                currentLocationFix.addOnCompleteListener {
                    /**
                     * Even after turning on the GPS sometimes it might proved a null as a result.
                     * So for safety make sure to add a null check at the callback result block and handle the result
                     */
                    continuation.resume(ServiceResult(ServiceStatus.Success,null,it.result),null)
                }
            }
            catch (ex:Exception){
                Log.d("Exception occurred", ex.toString())
                continuation.resume(ServiceResult(ServiceStatus.Success,null,null),null)
            }
        }
    }

}