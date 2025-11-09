package com.mak7chek.carexpenses.ui.screens.settings.fuelprices

data class FuelPricesUiState(
    val petrolPrice: String = "",
    val dieselPrice: String = "",
    val gasPrice: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)