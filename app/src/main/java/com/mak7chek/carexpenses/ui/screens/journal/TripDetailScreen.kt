package com.mak7chek.carexpenses.ui.screens.journal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // 👈 ВАЖЛИВИЙ ІМПОРТ
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // 👈 ВАЖЛИВИЙ ІМПОРТ
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds // 👈 ВАЖЛИВИЙ ІМПОРТ
import com.google.maps.android.compose.* // 👈 ВАЖЛИВИЙ ІМПОРТ
import com.mak7chek.carexpenses.R // 👈 Переконайся, що імпортуєш свій R
import com.mak7chek.carexpenses.util.bitmapDescriptorFromVector
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    navController: NavHostController,
    viewModel: TripDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 1. Створюємо стан для "керування" камерою
    val cameraState = rememberCameraPositionState()
    val markerColor = MaterialTheme.colorScheme.secondary
    val markerColorInt = markerColor.toArgb()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Деталі поїздки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    // ... (Показуємо помилку)
                }
                uiState.trip != null -> {
                    val trip = uiState.trip!!

                    // 2. Конвертуємо точки
                    val routeLatLngs = remember(trip.id) { // 'remember' для оптимізації
                        trip.routePoints.map { LatLng(it.latitude, it.longitude) }
                    }


                    LaunchedEffect(key1 = routeLatLngs) {
                        if (routeLatLngs.isNotEmpty()) {
                            // Будуємо "коробку", в яку мають влізти всі точки
                            val boundsBuilder = LatLngBounds.builder()
                            routeLatLngs.forEach { boundsBuilder.include(it) }

                            // 4. "Камера, перемістись і покажи мені *всю* цю 'коробку'"
                            cameraState.animate(

                                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
                            )
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // 5. КАРТА (з підключеною камерою)
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f),
                            cameraPositionState = cameraState // 👈 ПІДКЛЮЧИЛИ КАМЕРУ
                        ) {
                            Polyline(
                                points = routeLatLngs,
                                color = MaterialTheme.colorScheme.primary,
                                width = 15f
                            )

                            routeLatLngs.firstOrNull()?.let { startPoint ->
                                Marker(
                                    state = MarkerState(position = startPoint),
                                    title = "Старт"
                                )
                            }
                            routeLatLngs.lastOrNull()?.let { endPoint ->
                                Marker(
                                    state = MarkerState(position = endPoint),
                                    title = "Фініш",
                                     icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.ic_finish_flag,markerColorInt)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Авто: ${trip.vehicleName}", style = MaterialTheme.typography.titleMedium)
                            Text("Дистанція: ${String.format(Locale.US, "%.1f", trip.totalDistanceKm)} км")
                            Text("Витрачено палива: ${String.format(Locale.US, "%.1f", trip.totalFuelConsumedL)} л")
                            // (Тут можна додати форматування 'startTime' та 'endTime')
                        }
                    }
                }
            }
        }
    }
}

