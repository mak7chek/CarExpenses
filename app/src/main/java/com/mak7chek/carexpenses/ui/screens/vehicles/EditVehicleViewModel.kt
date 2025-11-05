// ui/screens/vehicles/EditVehicleViewModel.kt
package com.mak7chek.carexpenses.ui.screens.vehicles

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
            // 3. Беремо дані з Room (який є кешем)
            vehicleRepository.getVehicleById(vehicleId).collect { vehicle ->
                if (vehicle != null) {
                    _uiState.update {
                        it.copy(
                            make = vehicle.make,
                            model = vehicle.model,
                            year = vehicle.year.toString()
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


    fun onSaveClick() {
        val state = _uiState.value

        if (state.make.isBlank() || state.model.isBlank() || state.year.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Всі поля мають бути заповнені") }
            return
        }

        val yearInt = state.year.toIntOrNull()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        if (yearInt == null || yearInt < 1900 || yearInt > currentYear) {
            _uiState.update { it.copy(errorMessage = "Введіть коректний рік (напр. 2015)") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val request = VehicleRequest(
            make = state.make.trim(),
            model = state.model.trim(),
            year = yearInt
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