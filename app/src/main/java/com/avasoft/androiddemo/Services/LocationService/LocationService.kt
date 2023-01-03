package com.avasoft.androiddemo.Services.LocationService

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.avasoft.androiddemo.Services.APIHelper
import com.avasoft.androiddemo.Services.ServiceResult
import com.avasoft.androiddemo.Services.ServiceStatus
import java.util.*

class LocationService(private val locationSource: LocationSource = APIHelper.locationService): ILocationService {
    override suspend fun getAddress(lat: String, long: String, context: Context): ServiceResult<Address> {
//        val result = locationSource.getAddress("$lat,$Long")
        val geocoder = Geocoder(context, Locale.getDefault())
        val result = geocoder.getFromLocation(lat.toDouble(), long.toDouble(),1)
        if(result != null && result.size > 0){
            return ServiceResult(ServiceStatus.Success, null, result[0])
        }
        return ServiceResult(ServiceStatus.NoContent, null, null)
    }

}