package com.mak7chek.carexpenses.ui.screens.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.mak7chek.carexpenses.R
import com.mak7chek.carexpenses.data.dto.TripDetailResponse
import com.mak7chek.carexpenses.util.bitmapDescriptorFromVector
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    navController: NavHostController,
    viewModel: TripDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraState = rememberCameraPositionState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.trip != null -> {
                    TripDetailContent(
                        trip = uiState.trip!!,
                        cameraState = cameraState,
                        onNotesChange = viewModel::onNotesChange,
                        onSaveNotes = viewModel::saveNotes
                    )
                }
            }
        }
    }
}

@Composable
fun TripDetailContent(
    trip: TripDetailResponse,
    cameraState: CameraPositionState,
    onNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit
) {
    val context = LocalContext.current
    val markerColor = MaterialTheme.colorScheme.secondary.toArgb()
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("uk")) }

    val routeLatLngs = remember(trip.id) {
        trip.routePoints.map { LatLng(it.latitude, it.longitude) }
    }

    LaunchedEffect(key1 = routeLatLngs) {
        if (routeLatLngs.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            routeLatLngs.forEach { boundsBuilder.include(it) }
            cameraState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. КАРТА ---
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            cameraPositionState = cameraState
        ) {
            Polyline(
                points = routeLatLngs,
                color = MaterialTheme.colorScheme.primary,
                width = 15f
            )
            routeLatLngs.firstOrNull()?.let { startPoint ->
                Marker(state = MarkerState(position = startPoint), title = "Старт")
            }
            routeLatLngs.lastOrNull()?.let { endPoint ->
                Marker(
                    state = MarkerState(position = endPoint),
                    title = "Фініш",
                    icon = bitmapDescriptorFromVector(context, R.drawable.ic_finish_flag, markerColor)
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FinancialSummary(trip)

            NotesEditor(
                notes = trip.notes ?: "",
                onNotesChange = onNotesChange,
                onSave = onSaveNotes
            )

            TripInfo(trip = trip, formatter = formatter)
        }
    }
}

/**
 * Картка з фінансовим звітом
 */
@Composable
fun FinancialSummary(trip: TripDetailResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Вартість поїздки",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${String.format(Locale.US, "%.2f", trip.totalCost)} грн",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn("Дистанція", "${String.format(Locale.US, "%.1f", trip.totalDistanceKm)} км")
                InfoColumn("Витрачено", "${String.format(Locale.US, "%.1f", trip.totalFuelConsumedL)} л")
                InfoColumn("Ціна палива", "${String.format(Locale.US, "%.2f", trip.pricePerLiter)} грн/л")
            }
        }
    }
}

/**
 *  Редактор нотаток
 */
@Composable
fun NotesEditor(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Нотатки до поїздки") },
        placeholder = { Text("Напр.: Поїздка на дачу") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = onSave) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Зберегти нотатку"
                )
            }
        }
    )
}

/**
 * Новий Composable: Додаткова інформація
 */
@Composable
fun TripInfo(trip: TripDetailResponse, formatter: DateTimeFormatter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Деталі", style = MaterialTheme.typography.titleMedium)

            InfoRow("Авто:", trip.vehicleName)
            InfoRow("Паливо:", trip.fuelType)
            InfoRow("Витрата (авто):", "${trip.avgConsumption} л/100 км")

            Divider(Modifier.padding(vertical = 4.dp))

            val startTime = try { LocalDateTime.parse(trip.startTime).format(formatter) } catch (e: Exception) { "N/A" }
            val endTime = try { trip.endTime?.let { LocalDateTime.parse(it).format(formatter) } ?: "В дорозі" } catch (e: Exception) { "N/A" }

            InfoRow("Старт:", startTime)
            InfoRow("Фініш:", endTime)
        }
    }
}

// Допоміжні Composable для UI
@Composable
fun InfoColumn(title: String, value: String) {
    Column {
        Text(title, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}