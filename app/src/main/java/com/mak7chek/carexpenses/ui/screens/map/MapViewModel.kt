package com.mak7chek.carexpenses.ui.screens.map

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.local.entities.VehicleEntity
import com.mak7chek.carexpenses.data.repository.TripRepository
import com.mak7chek.carexpenses.data.repository.VehicleRepository
import com.mak7chek.carexpenses.services.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow // ІМПОРТ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow // ІМПОРТ
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val vehicles: List<VehicleEntity> = emptyList(),
    val selectedVehicleId: Long? = null,
    val currentTripId: Long? = null,
    val loadErrorMessage: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val tripRepository: TripRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    val isTracking = TrackingService.isTracking
    val currentLocation = TrackingService.currentLocation

    private val vehiclesFlow = vehicleRepository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState = combine(_uiState, vehiclesFlow) { state, vehicles ->
        state.copy(
            vehicles = vehicles,
            selectedVehicleId = state.selectedVehicleId ?: vehicles.firstOrNull()?.id
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapUiState())

    fun onVehicleSelected(vehicleId: Long) {
        _uiState.update { it.copy(selectedVehicleId = vehicleId) }
    }

    fun onStartTrip() {
        val vehicleId = uiState.value.selectedVehicleId ?: return
        if (isTracking.value) return

        viewModelScope.launch {
            _uiState.update { it.copy(loadErrorMessage = null) }
            try {
                val newTrip = tripRepository.startTrip(vehicleId)

                if (newTrip != null) {
                    startTrackingService(newTrip.id)
                    _uiState.update { it.copy(currentTripId = newTrip.id) }
                } else {
                    _userMessage.emit("Не вдалося почати поїздку: невідома помилка")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.emit("Помилка при старті поїздки: ${e.message}")
            }
        }
    }

    fun onStopTrip() {
        val tripId = uiState.value.currentTripId ?: return
        if (!isTracking.value) return

        stopTrackingService()

        viewModelScope.launch {
            try {
                tripRepository.endTrip(tripId)
                _uiState.update { it.copy(currentTripId = null) }
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.emit("Не вдалося завершити поїздку: ${e.message}")
            }
        }
    }

    fun startTrackingService(newTripId: Long) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
            putExtra(TrackingService.EXTRA_TRIP_ID, newTripId)
        }
        context.startService(intent)
    }

    private fun stopTrackingService() {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }
        context.startService(intent)
    }
}