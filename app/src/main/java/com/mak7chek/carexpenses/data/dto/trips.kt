package com.mak7chek.carexpenses.data.dto


data class TripStartRequest(
    val vehicleId: Long
)

data class LocationPointRequest(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

data class TrackBatchRequest(
    val points: List<LocationPointRequest>
)

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
    val vehicleId: Long,
    val vehicleName: String,
    val notes: String?
)

data class TripDetailResponse(
    val id: Long,
    val startTime: String,
    val endTime: String?,
    val notes: String?,
    val vehicleName: String,
    val fuelType: String,

    val totalDistanceKm: Double,
    val avgConsumption: Double,
    val totalFuelConsumedL: Double,
    val pricePerLiter: Double,
    val totalCost: Double,

    val routePoints: List<LocationPointResponse>
)

data class NoteUpdateRequest(
    val notes: String?
)