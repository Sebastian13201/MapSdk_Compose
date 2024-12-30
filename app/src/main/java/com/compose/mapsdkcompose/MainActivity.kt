package com.compose.mapsdkcompose

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.compose.mapsdkcompose.api.RetrofitClient
import com.compose.mapsdkcompose.data.MarkerInfo
import com.compose.mapsdkcompose.ui.theme.MapSdkComposeTheme
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyDzIo3sMmKaWWB20Usxmpl00oyL73ssCt0")
        }

        enableEdgeToEdge()
        setContent {
            MapSdkComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun MapScreen(modifier: Modifier = Modifier) {
        val context = LocalContext.current

        val searchQuery = remember { mutableStateOf("") }
        val suggestions = remember { mutableStateOf<List<Pair<String, LatLng>>>(emptyList()) }
        val selectedPosition = remember { mutableStateOf(LatLng(33.7772544, -84.5545472)) }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(selectedPosition.value, 15f)
        }

        val route = remember { mutableStateOf<List<LatLng>>(emptyList()) }
        val originPosition = remember { mutableStateOf(LatLng(33.7488, -84.3880)) }

        LaunchedEffect(selectedPosition.value) {
            if (selectedPosition.value != originPosition.value) {
                route.value = fetchRoute(
                    "AIzaSyDzIo3sMmKaWWB20Usxmpl00oyL73ssCt0",
                    originPosition.value,
                    selectedPosition.value
                )
            }
        }

        Scaffold(
            modifier = modifier
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = { query -> searchQuery.value = query },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search") },
                        singleLine = true
                    )

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(suggestions.value) { suggestion ->
                            Text(
                                text = suggestion.first,
                                color = if (selectedPosition.value == suggestion.second) Color.Blue else Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        selectedPosition.value = suggestion.second
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                            selectedPosition.value,
                                            15f
                                        )
                                    }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        val markers = listOf(
                            MarkerInfo(
                                LatLng(33.7782544, -84.5555472),
                                "Marker 1",
                                R.drawable.anya
                            ),
                            MarkerInfo(
                                LatLng(33.7792544, -84.5561472),
                                "Marker 2",
                                R.drawable.luffy
                            ),
                            MarkerInfo(
                                LatLng(33.7762544, -84.5532472),
                                "Marker 3",
                                R.drawable.clipart5643
                            ),
                            MarkerInfo(
                                LatLng(33.7772544, -84.5529472),
                                "Marker 4",
                                R.drawable.maximum_logo
                            )
                        )

                        markers.forEach { markerInfo ->
                            Marker(
                                state = MarkerState(position = markerInfo.position),
                                title = markerInfo.title,
                                icon = bitmapDescriptorFromVector(
                                    context,
                                    markerInfo.iconResId
                                )
                            )
                        }

                        if (route.value.isNotEmpty()) {
                            Polyline(
                                points = route.value,
                                color = Color.Blue,
                                width = 5f
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchPlaces(
        searchQuery: String,
        onSuggestionsFetched: (List<Pair<String, LatLng>>) -> Unit
    ) {
        val context = LocalContext.current
        val placesClient = remember { Places.createClient(context) }

        if (searchQuery.isNotBlank()) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(searchQuery)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val suggestions = response.autocompletePredictions.map { prediction ->
                        Pair(
                            prediction.getPrimaryText(null).toString(),
                            LatLng(33.7772544, -84.5545472)
                        )
                    }
                    onSuggestionsFetched(suggestions)
                }
                .addOnFailureListener { exception ->
                    Log.e("Places", "Error finding places", exception)
                    onSuggestionsFetched(emptyList())
                }
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

        val width = 80
        val height = 80

        vectorDrawable.setBounds(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dLng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    private suspend fun fetchRoute(apiKey: String, origin: LatLng, destination: LatLng): List<LatLng> {
        return try {
            val response = RetrofitClient.service.getDirections(
                "${origin.latitude},${origin.longitude}",
                "${destination.latitude},${destination.longitude}",
                apiKey
            )
            val polyline = response.routes?.firstOrNull()?.overviewPolyline?.points
            if (polyline != null) {
                decodePolyline(polyline)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("DirectionsScreen", "Error fetching directions: $e")
            emptyList()
        }
    }
}
