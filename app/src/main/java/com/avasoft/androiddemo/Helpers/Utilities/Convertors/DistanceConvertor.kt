package com.avasoft.androiddemo.Helpers.Utilities.Convertors

import android.util.Log
import com.avasoft.androiddemo.Helpers.AppConstants.Unit

object DistanceConvertor{
    // TODO - data type of distance should be Double
    fun convert(distance: Double, unit: Unit): Double{
        return try {
            when(unit){
                Unit.Kilometer -> distance
                Unit.Meter -> distance * Unit.Meter.inKM
                Unit.Miles -> distance * Unit.Miles.inKM
                Unit.Foot -> distance * Unit.Foot.inKM
                Unit.Yard -> distance * Unit.Yard.inKM
            }
        } catch (ex: Exception){
            Log.d("Exception occurred", ex.toString())
            0.0
        }
    }
}