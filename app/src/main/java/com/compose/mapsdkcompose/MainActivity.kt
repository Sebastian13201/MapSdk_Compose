package com.compose.mapsdkcompose

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
import com.compose.mapsdkcompose.api.RetrofitClient
import com.compose.mapsdkcompose.ui.theme.MapSdkComposeTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "GOOGLE_MAPS_API_KEY")
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
}

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }
    val suggestions = remember { mutableStateOf<List<Pair<String, LatLng>>>(emptyList()) }
    val selectedPosition = remember { mutableStateOf(LatLng(33.7772544, -84.5545472)) } // Initial position
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedPosition.value, 15f)
    }

    val route = remember { mutableStateOf<List<LatLng>>(emptyList()) }

    // Call to fetch places when search query changes
    SearchPlaces(searchQuery.value) { newSuggestions ->
        suggestions.value = newSuggestions
    }

    // Fetch route when the destination is selected
    LaunchedEffect(selectedPosition.value) {
        // Fetch directions from origin to selected position
        route.value = fetchRoute(
            "YOUR_GOOGLE_MAPS_API_KEY", // Use your API Key
            selectedPosition.value,
            LatLng(33.7488, -84.3880) // Set your destination here
        )
    }

    Column(modifier = modifier) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { query -> searchQuery.value = query },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Search") },
            singleLine = true
        )

        // Display Suggestions
        LazyColumn {
            items(suggestions.value) { suggestion ->
                Text(
                    text = suggestion.first,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            // Update camera position and selected marker
                            selectedPosition.value = suggestion.second
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedPosition.value, 15f)
                        }
                )
            }
        }

        // Google Map
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Marker for selected position
                Marker(
                    state = MarkerState(position = selectedPosition.value),
                    title = "Selected Location",
                    snippet = "Marker at selected location"
                )

                // Draw the route polyline if available
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

@Composable
fun SearchPlaces(
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
                        LatLng(0.0, 0.0) // Replace with the actual location if available
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

fun decodePolyline(encoded: String): List<LatLng> {
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

suspend fun fetchRoute(apiKey: String, origin: LatLng, destination: LatLng): List<LatLng> {
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