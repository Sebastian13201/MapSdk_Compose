package com.compose.mapsdkcompose.models


import com.google.gson.annotations.SerializedName

data class GeocodedWaypointModel(
    @SerializedName("geocoder_status")
    val geocoderStatus: String? = ""
)