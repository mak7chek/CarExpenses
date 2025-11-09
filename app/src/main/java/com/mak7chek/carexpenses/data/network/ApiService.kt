// data/network/ApiService.kt
package com.mak7chek.carexpenses.data.network // Замініть на ваш пакет

// Імпортуйте всі ваші DTO-класи з кроку 1
import com.mak7chek.carexpenses.data.dto.* import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("api/fuel-prices")
    suspend fun getFuelPrices(): List<FuelPriceResponse>
    @POST("api/fuel-prices")
    suspend fun updateFuelPrices(@Body request: List<FuelPriceUpdateRequest>)

    //--- UserController ---
    @PUT("api/user/name")
    suspend fun updateName(
        @Body request: UpdateNameRequest
    ): Response<Map<String, String>>

    @PUT("api/user/password")
    suspend fun updatePassword(
        @Body request: UpdatePasswordRequest
    ): Response<Map<String, String>>

    @DELETE("api/user/me")
    suspend fun deleteAccount(): Response<Map<String, String>>


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


    //---TripController---
    @POST("/api/trips/start")
    suspend fun startTrip(@Body request: TripStartRequest): TripResponse

    @POST("/api/trips/{id}/track")
    suspend fun trackTrip(
        @Path("id") tripId: Long,
        @Body request: TrackBatchRequest
    ): Response<Unit>

    @POST("/api/trips/{id}/end")
    suspend fun endTrip(@Path("id") tripId: Long): Response<Map<String, String>>

    @GET("/api/trips")
    suspend fun getAllTrips(
        @Query("search") search: String?,
        @Query("vehicleId") vehicleId: Long?,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("minDistance") minDistance: Double?,
        @Query("maxDistance") maxDistance: Double?
    ): List<TripResponse>

    @GET("/api/trips/{id}")
    suspend fun getTripDetails(@Path("id") tripId: Long): TripDetailResponse

    @DELETE("/api/trips/{id}")
    suspend fun deleteTrip(@Path("id") tripId: Long): Response<Unit>

    @PUT("/api/trips/{id}/notes")
    suspend fun updateTripNotes(
        @Path("id") tripId: Long,
        @Body request: NoteUpdateRequest
    ): Response<Map<String, String>>
}