package com.avasoft.androiddemo.Services.LocationService

import com.avasoft.androiddemo.BOs.GeoLocationBO.GeoLocationBO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationSource {
    @GET("maps/api/geocode/json")
    suspend fun getAddress(
        @Query("latlng") latLong: String,
        @Query("key") apiKey: String = "AIzaSyAx8a93oxD-8Uk98YifA2GQCu6MiZxrTh4",
        @Query("sensor") sensor: Boolean = true
    ):Response<GeoLocationBO>
}