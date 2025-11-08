package com.mak7chek.carexpenses.ui.screens.map

import android.Manifest
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.mak7chek.carexpenses.R
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import com.google.maps.android.compose.rememberMarkerState
import com.mak7chek.carexpenses.util.bitmapDescriptorFromVector
import kotlinx.coroutines.launch



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val uiState by viewModel.uiState.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }
    // -----------------------------------------

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (locationPermissionState.status.isGranted) {
            MapWithControls(
                uiState = uiState,
                isTracking = isTracking,
                currentLocation = currentLocation,
                onVehicleSelected = viewModel::onVehicleSelected,
                onStartTrip = viewModel::onStartTrip,
                onStopTrip = viewModel::onStopTrip,
                modifier = Modifier.padding(padding)
            )
        } else {
            PermissionDeniedScreen(locationPermissionState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapWithControls(
    uiState: MapUiState,
    isTracking: Boolean,
    currentLocation: Location?,
    onVehicleSelected: (Long) -> Unit,
    onStartTrip: () -> Unit,
    onStopTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mapProperties by remember {
        mutableStateOf(MapProperties(isMyLocationEnabled = true))
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = false,
                zoomControlsEnabled = false
            )
        )
    }
    var isVehicleMenuExpanded by remember { mutableStateOf(false) }
    val cameraState: CameraPositionState = rememberCameraPositionState()
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val currentPositionMarkerState = rememberMarkerState(key = "current_location_marker")

    val density = LocalDensity.current
    val markerSizeDp = 40.dp
    val markerSizePx = with(density) { markerSizeDp.toPx().toInt() }

    val iconColor = MaterialTheme.colorScheme.primary.toArgb()
    val finishIconColor = MaterialTheme.colorScheme.tertiary.toArgb()

    LaunchedEffect(key1 = currentLocation) {
        if (currentLocation != null) {
            val newLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            currentPositionMarkerState.position = newLatLng
            if (isTracking) {
                routePoints = routePoints + newLatLng
            }
            cameraState.animate(
                update = CameraUpdateFactory.newLatLngZoom(newLatLng, 17f),
                durationMs = 1000
            )
        }
    }

    LaunchedEffect(key1 = isTracking) {
        if (isTracking) {
            routePoints = emptyList()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            uiSettings = mapUiSettings,
            cameraPositionState = cameraState
        ) {
            val currentBearing = currentLocation?.bearing ?: 0f

            if (currentLocation != null) {
                Marker(
                    state = currentPositionMarkerState,
                    title = "Ви тут",
                    rotation = currentBearing,
                    flat = true,
                    icon = bitmapDescriptorFromVector(
                        LocalContext.current,
                        R.drawable.ic_car,
                        tintColor = iconColor,
                        targetWidth = markerSizePx,
                        targetHeight = markerSizePx
                    )
                )
            }

            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 15f
                )
            }

            if (!isTracking && routePoints.isNotEmpty()) {
                routePoints.lastOrNull()?.let { endPoint ->
                    Marker(
                        // Виправлено помилку "Creating state without remember"
                        state = rememberMarkerState(key = "finish_flag", position = endPoint),
                        title = "Фініш",
                        icon = bitmapDescriptorFromVector(
                            LocalContext.current,
                            R.drawable.ic_finish_flag,
                            tintColor = finishIconColor,
                            targetWidth = markerSizePx,
                            targetHeight = markerSizePx
                        )
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(0.8f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val selectedVehicle = uiState.vehicles.find {
                    it.id == uiState.selectedVehicleId
                }
                OutlinedButton(
                    onClick = { isVehicleMenuExpanded = true },
                    enabled = !isTracking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedVehicle?.name ?: "Виберіть авто", maxLines = 1)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Вибрати")
                }

                DropdownMenu(
                    expanded = isVehicleMenuExpanded,
                    onDismissRequest = { isVehicleMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    uiState.vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text("${vehicle.name} (${vehicle.make})") },
                            onClick = {
                                onVehicleSelected(vehicle.id)
                                isVehicleMenuExpanded = false
                            }
                        )
                    }
                    if (uiState.vehicles.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Немає доступних авто", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            enabled = false,
                            onClick = {  }
                        )
                    }
                }
            }
        }


        Button(
            onClick = if (isTracking) onStopTrip else onStartTrip,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTracking) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            ),
            enabled = uiState.selectedVehicleId != null
        ) {
            Text(if (isTracking) "ЗАВЕРШИТИ ПОЇЗДКУ" else "ПОЧАТИ ПОЇЗДКУ",
                style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDeniedScreen(
    locationPermissionState: PermissionState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Нам потрібен дозвіл",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Щоб відстежувати ваші поїздки, нам потрібен доступ до вашої геолокації.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                locationPermissionState.launchPermissionRequest()
            }
        ) {
            Text(text = "Надати дозвіл")
        }
    }
}

