package com.mak7chek.carexpenses.data.dto

data class TripStartRequest(
    val vehicleId: Long
)

// Точка для відправки на бекенд
data class LocationPointRequest(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

data class TrackBatchRequest(
    val points: List<LocationPointRequest>
)

// Точка, яку ми отримуємо від бекенда
data class LocationPointResponse(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

data class TripResponse(
    val id: Long,
    val startTime: String,
    val endTime: String?,
    val totalDistanceKm: Double?,
    val totalFuelConsumedL: Double?,
    val vehicle: VehicleResponse,
    val routePoints: List<LocationPointResponse>
)