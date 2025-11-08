package com.mak7chek.carexpenses.data.local.entities

import androidx.datastore.preferences.protobuf.Timestamp
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_points_buffer")
data class LocalGpsPoint(
    @PrimaryKey
    val id : Long=0,

    val tripId :Long,

    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
) {
}