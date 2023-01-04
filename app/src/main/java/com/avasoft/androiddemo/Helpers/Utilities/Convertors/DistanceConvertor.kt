package com.avasoft.androiddemo.Helpers.Utilities.Convertors

import com.avasoft.androiddemo.Helpers.AppConstants.Unit

// TODO ask whether we need ServiceResult for Convertors too
object DistanceConvertor{
    fun convert(distance: String, unit: Unit): String{
        return try {
            when(unit){
                Unit.Kilometer -> distance
                Unit.Meter -> "${distance.toDouble() * Unit.Meter.inKM}"
                Unit.Miles -> "${distance.toDouble() * Unit.Miles.inKM}"
                Unit.Foot -> "${distance.toDouble() * Unit.Foot.inKM}"
                Unit.Yard -> "${distance.toDouble() * Unit.Yard.inKM}"
            }
        } catch (ex: Exception){
            ""
        }
    }
}