package com.mak7chek.carexpenses.data.dto

data class VehicleRequest(
    val make: String,
    val model: String,
    val year: Int
)

data class VehicleResponse(
    val id: Long,
    val make: String,
    val model: String,
    val year: Int
)