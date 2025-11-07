package com.mak7chek.carexpenses.ui.screens.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mak7chek.carexpenses.data.local.entities.TripEntity
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel()
    // TODO: Додати 'navController: NavHostController'
) {
    val uiState by viewModel.uiState.collectAsState()
    val trips = uiState.trips

    val pullToRefreshState = rememberPullToRefreshState()
    val currentError = uiState.errorMessage
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = viewModel::refreshTrips,
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
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {if (currentError != null) {
                    Text(
                        text = currentError,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                }
            }

            trips.isEmpty() -> { // Спрощена логіка
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Журнал поїздок порожній.\nПерейдіть на вкладку 'Карта', щоб почати нову поїздку.",
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
                    items(trips) { trip ->
                        TripItem(
                            trip = trip,
                            onClick = {
                                // TODO: navController.navigate(Routes.tripDetail(trip.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripItem(trip: TripEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Поїздка #${trip.id}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (trip.totalDistanceKm != null) {
                Text(
                    text = "Дистанція: ${String.format(Locale.US, "%.1f", trip.totalDistanceKm)} км",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}