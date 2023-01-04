package com.avasoft.androiddemo.Services.LocationService

import android.content.Context
import android.location.Address
import com.avasoft.androiddemo.Services.ServiceResult

interface ILocationService {
    suspend fun getAddress(lat: String, long: String, context: Context): ServiceResult<String>
}