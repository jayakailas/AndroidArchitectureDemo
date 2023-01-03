package com.avasoft.androiddemo.Services

import com.avasoft.androiddemo.BuildConfig
import com.avasoft.androiddemo.Services.LocationService.LocationSource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object APIHelper{
    // Set up okHttpClient
    var okHttpClient = OkHttpClient.Builder().apply {

    // Sets the response cache to be used to read and write cached responses.
    this.cache(null)

    // Configure this client to retry or not when a connectivity problem is encountered.
    this.retryOnConnectionFailure(false)

    // Sets the default timeout for complete calls. A value of 0 means no timeout.
    this.callTimeout(40, TimeUnit.SECONDS)

    // Sets the default connect timeout for new connections. A value of 0 means no timeout.
    this.connectTimeout(40,TimeUnit.SECONDS)

        // Add interceptor to log request and response only in DEBUG MODE(for development purpose)
        if(BuildConfig.DEBUG){

            // OkHttp interceptor which logs request and response information.
            val logger = HttpLoggingInterceptor().apply {

                // Set Logging level - determines what has to logged from request & response.
                this.level = HttpLoggingInterceptor.Level.BODY
            }
            this.addInterceptor(logger)
        }
    }

    const val BASE_URL = "https://maps.googleapis.com/"

    // Create retrofit instance
    private val CLIENT: Retrofit by lazy {
        Retrofit
            .Builder()
            .client(okHttpClient.build())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val locationService: LocationSource by lazy {
        CLIENT.create(LocationSource::class.java)
    }
}