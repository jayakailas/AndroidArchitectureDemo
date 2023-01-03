package com.avasoft.androiddemo.Helpers.AppConstants

enum class Unit(val inKM: Double){
    Kilometer(1.0),
    Meter(1000.0),
    Miles(0.621371),
    Foot(3280.84),
    Yard(1093.61)
}

val unitsList = listOf<Unit>(
    Unit.Kilometer,
    Unit.Meter,
    Unit.Miles,
    Unit.Foot,
    Unit.Yard
)