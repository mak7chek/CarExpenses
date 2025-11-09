package com.mak7chek.carexpenses.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: Long,
    val startTime: String,
    val endTime: String?,
    val totalDistanceKm: Double?,
    val totalFuelConsumedL: Double?,
    val vehicleId: Long,
    val vehicleName: String,
    val notes: String?
)