package com.mak7chek.carexpenses.data.repository

import com.mak7chek.carexpenses.data.dto.FuelPriceUpdateRequest
import com.mak7chek.carexpenses.ui.model.FuelType
import com.mak7chek.carexpenses.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FuelPriceRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getPrices(): Result<Map<FuelType, Double>> {
        return try {
            val prices = apiService.getFuelPrices()
            val priceMap = prices.associate { it.fuelType to it.price }
            Result.success(priceMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePrices(priceMap: Map<FuelType, Double>): Result<Unit> {
        return try {
            val request = priceMap.map { (type, price) ->
                FuelPriceUpdateRequest(type, price)
            }
            apiService.updateFuelPrices(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}