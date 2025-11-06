// ui/screens/vehicles/AddVehicleViewModel.kt
package com.mak7chek.carexpenses.ui.screens.vehicles

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
import java.util.Calendar
import javax.inject.Inject

/**
 * Стан UI для екрану додавання авто
 */
data class AddVehicleUiState(
    val name: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val avgConsumptionLitersPer100Km: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

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
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }
    fun onAvgConsumptionLitersPer100KmChange(value: String) {
        if (value.count { it == ',' } <= 0) {
            _uiState.update { it.copy(avgConsumptionLitersPer100Km = value, errorMessage = null) }
        }
    }
    fun onSaveClick() {
        if (_uiState.value.isLoading) return

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
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        if (yearInt == null || yearInt < 1900 || yearInt > currentYear) {
            _uiState.update { it.copy(errorMessage = "Введіть коректний рік") }
            return
        }
        val avgConsumption = state.avgConsumptionLitersPer100Km.toDoubleOrNull()
        if (avgConsumption == null || avgConsumption <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Введіть розхід)") }
            return
        }
        // --- 2. ДІЯ ---
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
                vehicleRepository.createVehicle(request)

                _navigationEvent.emit(Unit)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Помилка при збереженні. Спробуйте пізніше."
                    )
                }
                e.printStackTrace()
            }
        }
    }
}