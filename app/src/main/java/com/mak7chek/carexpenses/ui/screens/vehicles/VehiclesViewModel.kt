package com.mak7chek.carexpenses.ui.screens.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.local.entities.VehicleEntity
import com.mak7chek.carexpenses.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow // ІМПОРТ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow // ІМПОРТ
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehiclesUiState(
    val vehicles: List<VehicleEntity> = emptyList(),
    val isLoading: Boolean = false,
    val loadErrorMessage: String? = null
)

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiclesUiState())
    val uiState: StateFlow<VehiclesUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            vehicleRepository.allVehicles.collect { vehicleList ->
                _uiState.update { it.copy(vehicles = vehicleList) }
            }
        }
        refreshVehicles()
    }

    fun refreshVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadErrorMessage = null) }
            try {
                vehicleRepository.refreshVehicles()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadErrorMessage = "Не вдалося оновити список авто"
                    )
                }
            }
        }
    }

    fun onVehicleSwiped(vehicle: VehicleEntity) {
        viewModelScope.launch {
            try {
                vehicleRepository.deleteVehicle(vehicle.id)
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.emit("Не вдалося видалити авто")
            }
        }
    }
}