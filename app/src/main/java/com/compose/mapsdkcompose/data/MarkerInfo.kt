package com.compose.mapsdkcompose.data

import com.google.android.gms.maps.model.LatLng

data class MarkerInfo(
    val position: LatLng,
    val title: String,
    val iconResId: Int
)