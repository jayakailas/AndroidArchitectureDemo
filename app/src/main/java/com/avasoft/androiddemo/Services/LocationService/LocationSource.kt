package com.avasoft.androiddemo.Services.LocationService

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationSource {
    @GET("maps/api/geocode/json")
    suspend fun getAddress(
        @Query("latlng") latLong: String,
        @Query("key") apiKey: String = "AIzaSyCum9I4-7SwOHyoTxQp75nY3PZiOCIhVrE"
    ):Response<Any>
}