package com.compose.mapsdkcompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.compose.mapsdkcompose.ui.theme.MapSdkComposeTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.maps_api_key))
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
    val startPosition = LatLng(33.7772544, -84.5545472)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPosition, 15f)
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


        // Google Map
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = startPosition),
                    title = "Starting Point",
                    snippet = "Marker in Atlanta"
                )
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