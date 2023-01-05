package com.avasoft.androiddemo.Services.LocationService

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.avasoft.androiddemo.Services.APIHelper
import com.avasoft.androiddemo.Services.ServiceResult
import com.avasoft.androiddemo.Services.ServiceStatus
import java.util.*

class LocationService(private val locationSource: LocationSource = APIHelper.locationService): ILocationService {
    override suspend fun getAddress(lat: String, long: String, context: Context): ServiceResult<String> {
        try {
            val result = locationSource.getAddress("$lat,$long")
            if(result.isSuccessful){
                return ServiceResult(ServiceStatus.Success, null, result.body()?.results?.get(0)?.formatted_address?:"")
            }
            return ServiceResult(ServiceStatus.NoContent, null, null)
        }
        catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            return ServiceResult(ServiceStatus.ServerError, null, null)
        }
    }

}