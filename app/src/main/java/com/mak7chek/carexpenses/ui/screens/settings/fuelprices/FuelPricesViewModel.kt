package com.mak7chek.carexpenses.ui.screens.settings.fuelprices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.repository.FuelPriceRepository
import com.mak7chek.carexpenses.ui.model.FuelType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FuelPricesViewModel @Inject constructor(
    private val repository: FuelPriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FuelPricesUiState())
    val uiState = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack = _navigateBack.asSharedFlow()

    init {
        loadCurrentPrices()
    }

    private fun loadCurrentPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getPrices()
            if (result.isSuccess) {
                val priceMap = result.getOrNull() ?: emptyMap()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        petrolPrice = priceMap[FuelType.PETROL]?.toString() ?: "",
                        dieselPrice = priceMap[FuelType.DIESEL]?.toString() ?: "",
                        gasPrice = priceMap[FuelType.GAS]?.toString() ?: ""
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Не вдалося завантажити ціни") }
            }
        }
    }

    fun onPetrolChange(price: String) = _uiState.update { it.copy(petrolPrice = price, error = null) }
    fun onDieselChange(price: String) = _uiState.update { it.copy(dieselPrice = price, error = null) }
    fun onGasChange(price: String) = _uiState.update { it.copy(gasPrice = price, error = null) }

    fun onSaveClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Валідація та конвертація
            val petrol = _uiState.value.petrolPrice.toDoubleOrNull()
            val diesel = _uiState.value.dieselPrice.toDoubleOrNull()
            val gas = _uiState.value.gasPrice.toDoubleOrNull()

            if (petrol == null || diesel == null || gas == null) {
                _uiState.update { it.copy(isLoading = false, error = "Введіть коректні числа") }
                return@launch
            }

            val priceMap = mapOf(
                FuelType.PETROL to petrol,
                FuelType.DIESEL to diesel,
                FuelType.GAS to gas
            )

            val result = repository.updatePrices(priceMap)
            if (result.isSuccess) {
                _userMessage.emit("Ціни успішно оновлено!")
                _navigateBack.emit(Unit)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Помилка збереження") }
            }
        }
    }
}