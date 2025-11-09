package com.mak7chek.carexpenses.data.repository

import com.mak7chek.carexpenses.data.dto.NoteUpdateRequest
import com.mak7chek.carexpenses.data.dto.TripDetailResponse
import com.mak7chek.carexpenses.data.local.dao.TripDao
import com.mak7chek.carexpenses.data.local.entities.TripEntity
import com.mak7chek.carexpenses.data.dto.TrackBatchRequest
import com.mak7chek.carexpenses.data.dto.TripResponse
import com.mak7chek.carexpenses.data.dto.TripStartRequest
import com.mak7chek.carexpenses.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val apiService: ApiService,
    private val tripDao: TripDao
) {
    val allTrips: Flow<List<TripEntity>> = tripDao.getAllTrips()



    suspend fun refreshAndFilterTrips(
        search: String?,
        vehicleId: Long?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
        minDistance: Double?,
        maxDistance: Double?
    ) {
        val networkTrips = apiService.getAllTrips(
            search = search,
            vehicleId = vehicleId,
            dateFrom = dateFrom?.toString(),
            dateTo = dateTo?.toString(),
            minDistance = minDistance,
            maxDistance = maxDistance
        )

        val tripEntities = networkTrips.map { it.toEntity() }

        tripDao.clearAndInsert(tripEntities)
    }

    suspend fun startTrip(vehicleId: Long): TripResponse? {
        return try {
            val request = TripStartRequest(vehicleId)
            val newTripResponse = apiService.startTrip(request)
            tripDao.insert(newTripResponse.toEntity())
            newTripResponse
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Надіслати "пачку" GPS-точок.
     */
    suspend fun trackTrip(tripId: Long, batch: TrackBatchRequest) {
        try {
            apiService.trackTrip(tripId, batch)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun endTrip(tripId: Long) {
        apiService.endTrip(tripId)

        refreshAndFilterTrips(null, null, null, null, null, null)
    }

    suspend fun getTripDetailsFromApi(tripId: Long): Result<TripDetailResponse> {
        return try {
            val detailResponse = apiService.getTripDetails(tripId)
            Result.success(detailResponse)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateTripNotes(tripId: Long, notes: String?) {
        apiService.updateTripNotes(tripId, NoteUpdateRequest(notes))
        tripDao.updateNotes(tripId, notes)
    }

    suspend fun delete(tripId:Long ){
        try {
            apiService.deleteTrip(tripId)
            tripDao.deleteById(tripId)
        } catch(e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
private fun TripResponse.toEntity(): TripEntity {
    return TripEntity(
        id = this.id,
        startTime = this.startTime,
        endTime = this.endTime,
        totalDistanceKm = this.totalDistanceKm,
        totalFuelConsumedL = this.totalFuelConsumedL,
        vehicleId = this.vehicleId,
        vehicleName = this.vehicleName,
        notes = this.notes
    )
}