package com.mak7chek.carexpenses.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val avgConsumptionLitersPer100Km : Double,
    val fuelType: String?
)