package com.avasoft.androiddemo.BOs.GeoLocationBO

data class Geometry(
    val bounds: Bounds,
    val location: Location,
    val location_type: String,
    val viewport: Viewport
)