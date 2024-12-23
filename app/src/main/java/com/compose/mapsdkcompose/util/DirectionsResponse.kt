package com.compose.mapsdkcompose.util

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes")
    val routes: List<Route>? = listOf(), // Ensure this matches the API response
    @SerializedName("status")
    val status: String? = ""
)

data class Route(
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolyline? = null
)

data class OverviewPolyline(
    @SerializedName("points")
    val points: String? = ""
)