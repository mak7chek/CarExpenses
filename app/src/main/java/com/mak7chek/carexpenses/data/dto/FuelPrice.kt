package com.mak7chek.carexpenses.data.dto

import com.mak7chek.carexpenses.ui.model.FuelType

data class FuelPriceUpdateRequest(
    val fuelType: FuelType,
    val price: Double
)

data class FuelPriceResponse(
    val fuelType: FuelType,
    val price: Double
)