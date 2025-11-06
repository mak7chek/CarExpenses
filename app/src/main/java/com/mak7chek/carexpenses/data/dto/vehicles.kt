package com.mak7chek.carexpenses.data.dto

data class VehicleRequest(
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val avgConsumptionLitersPer100Km: Double
)

data class VehicleResponse(
    val id: Long,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val avgConsumptionLitersPer100Km: Double
)