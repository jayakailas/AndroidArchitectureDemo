package com.avasoft.androiddemo.BOs.UserBO

import android.location.Location

fun UserBO.calculateDistance(){
    try {
        val kiloMeterInMeters = 0.001
        val result = FloatArray(2)
        Location.distanceBetween(
            this.currentLat!!.toDouble(),
            this.currentLong!!.toDouble(),
            this.customLat!!.toDouble(),
            this.customLong!!.toDouble(),
            result
        )
        this.distance = (result[0] * kiloMeterInMeters).toString()
    }
    catch (ex: Exception){
        // TODO Have to explore the folder structure to create user-defined excpetions
        throw Exception("An error occurred")
    }
}