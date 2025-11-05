// data/network/ApiService.kt
package com.mak7chek.carexpenses.data.network // Замініть на ваш пакет

// Імпортуйте всі ваші DTO-класи з кроку 1
import com.mak7chek.carexpenses.data.dto.* import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AuthController ---
    @POST("/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    // --- VehicleController ---
    @POST("/api/vehicles")
    suspend fun createVehicle(@Body request: VehicleRequest): VehicleResponse

    @GET("/api/vehicles")
    suspend fun getVehicles(): List<VehicleResponse>

    @PUT("/api/vehicles/{id}")
    suspend fun updateVehicle(@Path("id") id: Long, @Body request: VehicleRequest): VehicleResponse

    @DELETE("/api/vehicles/{id}")
    suspend fun deleteVehicle(@Path("id") id: Long): Response<Unit>

    @POST("/api/trips/start")
    suspend fun startTrip(@Body request: TripStartRequest): TripResponse

    @POST("/api/trips/{id}/track")
    suspend fun trackTrip(
        @Path("id") tripId: Long,
        @Body request: TrackBatchRequest
    ): Response<Unit>

    @POST("/api/trips/{id}/end")
    suspend fun endTrip(@Path("id") tripId: Long): TripResponse

    @GET("/api/trips")
    suspend fun getAllTrips(): List<TripResponse>

    @GET("/api/trips/{id}")
    suspend fun getTripDetails(@Path("id") tripId: Long): TripResponse

    @DELETE("/api/trips/{id}")
    suspend fun deleteTrip(@Path("id") tripId: Long): Response<Unit>
}