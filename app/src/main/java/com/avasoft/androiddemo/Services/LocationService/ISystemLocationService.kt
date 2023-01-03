package com.avasoft.androiddemo.Services.LocationService

import android.app.PendingIntent
import android.location.Location
import com.avasoft.androiddemo.Services.ServiceResult

interface ISystemLocationService {

    suspend fun checkGPS(): ServiceResult<PendingIntent>

    suspend fun getLastKnownLocation(): ServiceResult<Location>

    suspend fun currentLocation(): ServiceResult<Location>
}