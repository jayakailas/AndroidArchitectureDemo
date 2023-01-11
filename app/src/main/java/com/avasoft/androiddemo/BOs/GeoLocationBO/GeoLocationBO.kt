package com.avasoft.androiddemo.BOs.GeoLocationBO

data class GeoLocationBO(
    val plus_code: PlusCode,
    val results: List<Result>,
    val status: String
)

data class PlusCode(
    val compound_code: String,
    val global_code: String
)

data class Result(
    val address_components: List<AddressComponent>,
    val formatted_address: String,
    val geometry: Geometry,
    val place_id: String,
    val plus_code: PlusCode,
    val types: List<String>
)

data class Geometry(
    val bounds: Bounds,
    val location: Location,
    val location_type: String,
    val viewport: Bounds
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class AddressComponent(
    val long_name: String,
    val short_name: String,
    val types: List<String>
)

data class Bounds(
    val northeast: Location,
    val southwest: Location
)