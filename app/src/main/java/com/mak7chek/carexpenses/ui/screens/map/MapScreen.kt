package com.mak7chek.carexpenses.ui.screens.map

import android.Manifest
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.maps.android.compose.GoogleMap // 👈 Твій імпорт карти
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.mak7chek.carexpenses.ui.screens.map.MapUiState
import com.mak7chek.carexpenses.ui.screens.map.MapViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // --- 1. "СЛУХАЄМО" ВСІ НАШІ СТАНИ ---
    val uiState by viewModel.uiState.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    if (locationPermissionState.status.isGranted) {
        MapWithControls(
            uiState = uiState,
            isTracking = isTracking,
            currentLocation = currentLocation,
            onVehicleSelected = viewModel::onVehicleSelected,
            onStartTrip = viewModel::onStartTrip,
            onStopTrip = viewModel::onStopTrip
        )
    } else {
        PermissionDeniedScreen(locationPermissionState)
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
    onStopTrip: () -> Unit
) {
    var mapProperties by remember {
        mutableStateOf(MapProperties(isMyLocationEnabled = true))
    }
    var isVehicleMenuExpanded by remember { mutableStateOf(false) }
    val cameraState: CameraPositionState = rememberCameraPositionState()
    val routePoints = remember { mutableStateListOf<LatLng>() }

    LaunchedEffect(key1 = currentLocation) {
        if (currentLocation != null && isTracking) {
            val newLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            routePoints.add(newLatLng)

            cameraState.animate(
                update = CameraUpdateFactory.newLatLngZoom(newLatLng, 17f),
                durationMs = 1000
            )
        }
    }

    LaunchedEffect(key1 = isTracking) {
        if (!isTracking) {
            routePoints.clear()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            cameraPositionState = cameraState
        ) {
            if (isTracking && routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 15f
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
        {
            val selectedVehicle = uiState.vehicles.find {
                it.id == uiState.selectedVehicleId
            }
            OutlinedButton(
                onClick = { isVehicleMenuExpanded = true },
                enabled = !isTracking
            ) {
                Text(selectedVehicle?.name ?: "Виберіть авто")
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Вибрати")
            }


            DropdownMenu(
                expanded = isVehicleMenuExpanded,
                onDismissRequest = { isVehicleMenuExpanded = false }

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
            }
        }

        Button(
            onClick = if (isTracking) onStopTrip else onStartTrip,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTracking) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            ),
            enabled = uiState.selectedVehicleId != null
        ) {
            Text(if (isTracking) "ЗАВЕРШИТИ ПОЇЗДКУ" else "ПОЧАТИ ПОЇЗДКУ")
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
                // 3. При натисканні - показуємо системний діалог запиту дозволу
                locationPermissionState.launchPermissionRequest()
            }
        ) {
            Text(text = "Надати дозвіл")
        }
    }
}