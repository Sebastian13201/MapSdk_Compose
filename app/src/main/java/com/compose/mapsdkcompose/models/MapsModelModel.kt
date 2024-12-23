package com.compose.mapsdkcompose.models


import com.google.gson.annotations.SerializedName

data class MapsModelModel(
    @SerializedName("geocoded_waypoints")
    val geocodedWaypoints: List<GeocodedWaypointModel?>? = listOf(),
    @SerializedName("routes")
    val routes: List<RouteModel?>? = listOf(),
    @SerializedName("status")
    val status: String? = ""
)

data class RouteModel(
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolylineModel?,
    @SerializedName("legs")
    val legs: List<LegModel?>? = listOf()
)

data class OverviewPolylineModel(
    @SerializedName("points")
    val points: String?
)

data class LegModel(
    @SerializedName("start_location")
    val startLocation: LatLngModel?,
    @SerializedName("end_location")
    val endLocation: LatLngModel?,
    @SerializedName("steps")
    val steps: List<StepModel?>? = listOf()
)

data class LatLngModel(
    @SerializedName("lat")
    val lat: Double?,
    @SerializedName("lng")
    val lng: Double?
)

data class StepModel(
    @SerializedName("html_instructions")
    val htmlInstructions: String?,
    @SerializedName("distance")
    val distance: DistanceModel?,
    @SerializedName("duration")
    val duration: DurationModel?,
    @SerializedName("end_location")
    val endLocation: LatLngModel?
)

data class DistanceModel(
    @SerializedName("text")
    val text: String?,
    @SerializedName("value")
    val value: Int?
)

data class DurationModel(
    @SerializedName("text")
    val text: String?,
    @SerializedName("value")
    val value: Int?
)