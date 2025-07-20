package com.example.gym_buddy.gymsnearme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun LocalGymsMap() {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    // Google headquarters
    val defaultLocation = LatLng(37.4221, -122.0853)
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val nearbyGymMarkers = remember { mutableStateListOf<GymInfo>() } // Store GymInfo
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
    }
    val fusedLocationClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        false
                    ) -> {
                getCurrentLocation(context, fusedLocationClient) { latLng ->
                    currentLocation = latLng
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(latLng, 12f) // Zoom closer
                    // Launch a coroutine to fetch gyms
                    coroutineScope.launch {
                        val gyms = findNearbyGyms(placesClient, latLng)
                        nearbyGymMarkers.clear()
                        nearbyGymMarkers.addAll(gyms)
                    }
                }
            }

            else -> {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        // Initial location permission check
        requestLocationAndFetchGyms(
            context = context,
            fusedLocationClient = fusedLocationClient,
            placesClient = placesClient,
            locationPermissionLauncher = locationPermissionLauncher,
            coroutineScope = coroutineScope,
            onLocationUpdate = { latLng -> currentLocation = latLng }, // Lambda to update state
            cameraPositionState = cameraPositionState,
            nearbyGymMarkers = nearbyGymMarkers
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            currentLocation?.let {
                // Generate a BitmapDescriptor for a orange marker
                val orangeMarkerIcon: BitmapDescriptor by remember(it) {
                    mutableStateOf(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                }

                Marker(
                    state = MarkerState(position = it),
                    title = "My Location",
                    icon = orangeMarkerIcon
                )
            }
            // Display markers for nearby gyms
            nearbyGymMarkers.forEach { gym ->
                val starRating = gym.rating?.let { rating ->
                    "★ " + "%.1f".format(rating)
                } ?: "Rating: N/A"

                Marker(
                    state = MarkerState(position = gym.latLng),
                    title = gym.name ?: "Gym",
                    snippet = starRating
                )
            }
        }
        // Refresh button
        ElevatedButton(
            onClick = {
                requestLocationAndFetchGyms(
                    context = context,
                    fusedLocationClient = fusedLocationClient,
                    placesClient = placesClient,
                    locationPermissionLauncher = locationPermissionLauncher,
                    coroutineScope = coroutineScope,
                    onLocationUpdate = { latLng ->
                        currentLocation = latLng
                    }, // Lambda to update state
                    cameraPositionState = cameraPositionState,
                    nearbyGymMarkers = nearbyGymMarkers
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 0.dp, top = 5.dp, end = 0.dp, bottom = 0.dp)
        ) {
            Text("Update My Location")
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// SuppressMissingPermission is safe because hasLocationPermission is checked before calling
@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (LatLng) -> Unit
) {
    // Check if location is enabled on the device
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) &&
        !locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    ) {
        Toast.makeText(context, "Please enable location services", Toast.LENGTH_LONG).show()
        // Optionally, guide user to settings:
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
        return
    }


    // Use CancellationToken for a one-time current location request
    val cancellationTokenSource = CancellationTokenSource()

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationFetched(LatLng(location.latitude, location.longitude))
        } else {
            Toast.makeText(
                context,
                "Could not get location. Trying last known location.",
                Toast.LENGTH_SHORT
            ).show()
            // Fallback to last known location if current location is null
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                if (lastLocation != null) {
                    onLocationFetched(LatLng(lastLocation.latitude, lastLocation.longitude))
                } else {
                    Toast.makeText(
                        context,
                        "Could not get last known location.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error getting last known location: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }.addOnFailureListener { exception ->
        Toast.makeText(
            context,
            "Error getting current location: ${exception.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}


private fun requestLocationAndFetchGyms(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    placesClient: PlacesClient,
    locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    coroutineScope: CoroutineScope,
    onLocationUpdate: (LatLng) -> Unit, // Callback for current location state
    cameraPositionState: CameraPositionState,
    nearbyGymMarkers: SnapshotStateList<GymInfo>
) {
    if (hasLocationPermission(context)) {
        getCurrentLocation(context, fusedLocationClient) { latLng ->
            onLocationUpdate(latLng) // Update the currentLocation state in the composable
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 12f)
            coroutineScope.launch {
                // Ensure placesClient is initialized before this point
                val gyms = findNearbyGyms(
                    placesClient,
                    latLng
                ) // Removed context, as PlacesClient is now passed
                nearbyGymMarkers.clear()
                nearbyGymMarkers.addAll(gyms)
            }
        }
    } else {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}


// Data class to hold simplified gym information for the UI
data class GymInfo(
    val id: String,
    val name: String?,
    val latLng: LatLng,
    val rating: Double? = null
)

@SuppressLint("MissingPermission") // Ensure permissions are checked before calling functions that need them
suspend fun findNearbyGyms(
    placesClient: PlacesClient, // Pass the PlacesClient
    center: LatLng,
    radiusMeters: Double = 5000.0 // Radius in meters (5km)
): List<GymInfo> {
    val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.RATING
    )

    // Define the search area as a circle around the user's location. Radius in meters.
    val searchBounds = CircularBounds.newInstance(center, radiusMeters)

    // Construct the SearchNearbyRequest.
    //    - Set the included primary type to "gym".
    //    - Set location restriction for more relevant results.
    val searchNearbyRequest = SearchNearbyRequest.builder(searchBounds, placeFields)
        .setIncludedPrimaryTypes(listOf("gym"))
        .setMaxResultCount(10)
        .build()

    val gymResults = mutableListOf<GymInfo>()

    try {
        val response = placesClient.searchNearby(searchNearbyRequest).await()

        for (place in response.places) {
            // Get the values using the getter methods
            val placeId = place.id
            val placeLatLng = place.location
            val placeName = place.displayName
            val placeRating = place.rating

            // Ensure essential fields are not null before creating GymInfo
            if (placeId != null && placeLatLng != null) {
                gymResults.add(
                    GymInfo(
                        id = placeId,
                        name = placeName,
                        latLng = placeLatLng,
                        rating = placeRating
                    )
                )

            }
        }
    } catch (e: Exception) {
        // Handle exceptions, e.g., from network errors or API issues
        Log.e("NearbyGyms", "Error finding nearby gyms: ${e.message}", e)
    }

    return gymResults
}