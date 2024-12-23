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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.compose.mapsdkcompose.ui.theme.MapSdkComposeTheme
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "YOUR_GOOGLE_PLACES_API_KEY")
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
    val singapore = LatLng(1.35, 103.87)  // Default location

    // State variables
    val searchQuery = remember { mutableStateOf("") }
    val suggestions = remember { mutableStateOf(emptyList<Pair<String, LatLng>>()) }
    val selectedLocation = remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Box
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { query ->
                searchQuery.value = query
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Search") },
            singleLine = true
        )

        // Launch search effect when query changes
        LaunchedEffect(searchQuery.value) {
            if (searchQuery.value.isNotBlank()) {
                val placesClient = Places.createClient(context)

                try {
                    // Get place predictions
                    val predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setQuery(searchQuery.value)
                        .build()

                    val predictionsResponse = placesClient.findAutocompletePredictions(predictionsRequest).await()

                    // For each prediction, fetch the place details
                    val newSuggestions = predictionsResponse.autocompletePredictions.map { prediction ->
                        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
                        val fetchPlaceRequest = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

                        try {
                            val placeResponse = placesClient.fetchPlace(fetchPlaceRequest).await()
                            val place = placeResponse.place
                            Pair(
                                prediction.getPrimaryText(null).toString(),
                                place.latLng ?: singapore // Fallback to default location
                            )
                        } catch (e: ApiException) {
                            Log.e("Places", "Error fetching place details: ${e.statusCode}, ${e.message}", e)
                            null
                        }
                    }.filterNotNull()

                    suggestions.value = newSuggestions
                } catch (e: ApiException) {
                    Log.e("API_ERROR", "Error fetching predictions: ${e.statusCode}, ${e.message}", e)
                    suggestions.value = emptyList()
                }
            } else {
                suggestions.value = emptyList()
            }
        }

        // Suggestions List
        LazyColumn {
            items(suggestions.value) { suggestion ->
                Text(
                    text = suggestion.first,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedLocation.value = suggestion.second
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(suggestion.second, 15f)
                            searchQuery.value = suggestion.first
                            suggestions.value = emptyList()
                        }
                        .padding(8.dp)
                )
            }
        }

        // Map
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                // Show marker for selected location
                selectedLocation.value?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = searchQuery.value
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