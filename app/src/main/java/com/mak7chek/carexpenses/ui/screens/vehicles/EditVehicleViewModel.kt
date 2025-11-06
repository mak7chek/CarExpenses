// ui/screens/vehicles/EditVehicleViewModel.kt
package com.mak7chek.carexpenses.ui.screens.vehicles

import androidx.compose.animation.core.animateValueAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.dto.VehicleRequest
import com.mak7chek.carexpenses.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditVehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    init {
        loadVehicleData()
    }

    private fun loadVehicleData() {
        viewModelScope.launch {
            vehicleRepository.getVehicleById(vehicleId).collect { vehicle ->
                if (vehicle != null) {
                    _uiState.update {
                        it.copy(
                            name = vehicle.name,
                            make = vehicle.make,
                            model = vehicle.model,
                            year = vehicle.year.toString(),
                            avgConsumptionLitersPer100Km = vehicle.avgConsumptionLitersPer100Km.toString()
                        )
                    }
                }
            }
        }
    }

    fun onMakeChange(make: String) {
        _uiState.update { it.copy(make = make, errorMessage = null) }
    }

    fun onModelChange(model: String) {
        _uiState.update { it.copy(model = model, errorMessage = null) }
    }

    fun onYearChange(year: String) {
        _uiState.update { it.copy(year = year, errorMessage = null) }
    }
    fun onNameChange(name: String) {
        _uiState.update { it.copy(name= name, errorMessage = null) }
    }
    fun onAvgConsumptionLitersPer100KmChange(value: String) {
        if (value.count { it == ',' } <= 0) {
            _uiState.update { it.copy(avgConsumptionLitersPer100Km = value, errorMessage = null) }
        }
    }


    fun onSaveClick() {
        val state = _uiState.value

        if (state.make.isBlank() || state.model.isBlank() || state.year.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Всі поля мають бути заповнені") }
            return
        }
        if (state.name.isBlank() || state.make.isBlank() || state.model.isBlank() ||
            state.year.isBlank() || state.avgConsumptionLitersPer100Km.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Всі поля мають бути заповнені") }
            return
        }

        val yearInt = state.year.toIntOrNull()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        if (yearInt == null || yearInt < 1900 || yearInt > currentYear) {
            _uiState.update { it.copy(errorMessage = "Введіть коректний рік") }
            return
        }
        val avgConsumption = state.avgConsumptionLitersPer100Km.toDoubleOrNull()
        if (avgConsumption == null || avgConsumption <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Введіть розхід)") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val request = VehicleRequest(
            name = state.name.trim(),
            make = state.make.trim(),
            model = state.model.trim(),
            year = yearInt,
            avgConsumptionLitersPer100Km = avgConsumption
        )

        viewModelScope.launch {
            try {
                vehicleRepository.updateVehicle(vehicleId, request)
                _navigationEvent.emit(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Помилка збереження. Перевірте мережу."
                    )
                }
            }
        }
    }
}