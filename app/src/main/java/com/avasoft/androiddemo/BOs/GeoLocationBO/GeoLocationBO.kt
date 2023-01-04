package com.avasoft.androiddemo.BOs.GeoLocationBO

data class GeoLocationBO(
    val plus_code: PlusCode,
    val results: List<Result>,
    val status: String
)