package com.mak7chek.carexpenses.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mak7chek.carexpenses.data.local.entities.TripEntity
import com.mak7chek.carexpenses.ui.navigation.Routes
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.mak7chek.carexpenses.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private fun Long?.toLocalDate(): LocalDate? {
    return this?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val trips = uiState.trips

    val pullToRefreshState = rememberPullToRefreshState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            scope.launch {
                snackBarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // --- ПАНЕЛЬ ПОШУКУ ТА ФІЛЬТРІВ ---
            FilterControls(
                uiState = uiState,
                viewModel = viewModel,
                showFilters = showFilters,
                onToggleFilters = { showFilters = !showFilters }
            )

            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = viewModel::applyFilters,
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = uiState.isLoading,
                        state = pullToRefreshState,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            ) {
                when {
                    uiState.loadErrorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.loadErrorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    trips.isEmpty() && !uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotBlank() || uiState.selectedVehicleId != null)
                                    "Поїздок за вашими фільтрами не знайдено."
                                else
                                    "Журнал поїздок порожній.\nПерейдіть на вкладку 'Карта', щоб почати нову поїздку.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = trips,
                                key = { it.id }
                            ) { trip ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.onTripSwiped(trip)
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.error,
                                                    shape = CardDefaults.shape
                                                )
                                                .padding(horizontal = 16.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                        }
                                    }
                                ) {
                                    TripItem(
                                        trip = trip,
                                        onClick = {
                                            val route = Routes.tripDetailRoute(trip.id)
                                            navController.navigate(route)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterControls(
    uiState: JournalUiState,
    viewModel: JournalViewModel,
    showFilters: Boolean,
    onToggleFilters: () -> Unit
) {
    val datePickerStateFrom = rememberDatePickerState()
    var showFromDialog by remember { mutableStateOf(false) }

    val datePickerStateTo = rememberDatePickerState()
    var showToDialog by remember { mutableStateOf(false) }
    if (showFromDialog) {
        DatePickerDialog(
            onDismissRequest = { showFromDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateFromChange(datePickerStateFrom.selectedDateMillis.toLocalDate())
                    showFromDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onDateFromChange(null)
                    showFromDialog = false
                }) { Text("Очистити") }
            }
        ) {
            DatePicker(state = datePickerStateFrom)
        }
    }
    if (showToDialog) {
        DatePickerDialog(
            onDismissRequest = { showToDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateToChange(datePickerStateTo.selectedDateMillis.toLocalDate())
                    showToDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onDateToChange(null)
                    showToDialog = false
                }) { Text("Очистити") }
            }
        ) {
            DatePicker(state = datePickerStateTo)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = {
                viewModel.onSearchQueryChange(it)
                viewModel.applyFilters()
            },
            label = { Text("Пошук по нотатках...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                Row {
                    IconButton(onClick = viewModel::onExportClicked) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_download),
                            contentDescription = "Експорт в CSV",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onToggleFilters) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Фільтри",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        if (showFilters) {
            var vehicleMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = vehicleMenuExpanded,
                onExpandedChange = { vehicleMenuExpanded = !vehicleMenuExpanded }
            ) {
                val selectedText = uiState.vehicles.find { it.id == uiState.selectedVehicleId }?.name ?: "Всі автомобілі"
                OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Автомобіль") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = vehicleMenuExpanded,
                    onDismissRequest = { vehicleMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Всі автомобілі") },
                        onClick = {
                            viewModel.onVehicleSelected(null)
                            vehicleMenuExpanded = false
                        }
                    )
                    uiState.vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text(vehicle.name) },
                            onClick = {
                                viewModel.onVehicleSelected(vehicle.id)
                                vehicleMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.minDistance,
                    onValueChange = viewModel::onMinDistanceChange,
                    label = { Text("Від (км)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = uiState.maxDistance,
                    onValueChange = viewModel::onMaxDistanceChange,
                    label = { Text("До (км)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.dateFrom?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("З") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showFromDialog = true }) {
                            Icon(Icons.Default.DateRange, "Вибрати дату")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showFromDialog = true }
                )
                // Поле "Дата До"
                OutlinedTextField(
                    value = uiState.dateTo?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("До") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showToDialog = true }) {
                            Icon(Icons.Default.DateRange, "Вибрати дату")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showToDialog = true }
                )
            }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = viewModel::applyFilters,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Застосувати фільтри")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripItem(
    trip: TripEntity,
    onClick: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale("uk")) }
    val startTimeFormatted = remember(trip.startTime) {
        try {
            LocalDateTime.parse(trip.startTime).format(formatter)
        } catch (e: Exception) {
            trip.startTime
        }
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- 1. Назва ---
            Text(
                text = trip.notes?.takeIf { it.isNotBlank() } ?: trip.vehicleName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            // --- 2. Дата ---
            Text(
                text = startTimeFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- 3. Дистанція та Паливо ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Дистанція", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${String.format(Locale.US, "%.1f", trip.totalDistanceKm ?: 0.0)} км",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Витрачено", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${String.format(Locale.US, "%.1f", trip.totalFuelConsumedL ?: 0.0)} л",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}