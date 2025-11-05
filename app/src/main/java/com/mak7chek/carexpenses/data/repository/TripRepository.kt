// data/repository/TripRepository.kt
package com.mak7chek.carexpenses.data.repository

import com.mak7chek.carexpenses.data.local.dao.TripDao
import com.mak7chek.carexpenses.data.local.entities.TripEntity
import com.mak7chek.carexpenses.data.dto.TrackBatchRequest
import com.mak7chek.carexpenses.data.dto.TripResponse
import com.mak7chek.carexpenses.data.dto.TripStartRequest
import com.mak7chek.carexpenses.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val apiService: ApiService,
    private val tripDao: TripDao
) {

    /**
     * ГОЛОВНИЙ ПОТІК ДАНИХ (ЖУРНАЛ) ДЛЯ UI.
     */
    val allTrips: Flow<List<TripEntity>> = tripDao.getAllTrips()

    /**
     * Примусово оновити журнал поїздок з бекенду.
     */
    suspend fun refreshTrips() {
        try {
            val networkTrips = apiService.getAllTrips()

            val tripEntities = networkTrips.map { response ->
                response.toEntity()
            }

            tripDao.insertAll(tripEntities)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Почати нову поїздку.
     * @return Повертає TripResponse, бо нам потрібен tripId для трекінгу.
     */
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
            // Помилка. Можливо, варто зберегти "пачку" локально і
            // спробувати відправити пізніше (це вже складніша логіка)
            e.printStackTrace()
        }
    }

    /**
     * Завершити поїздку.
     * @return Повертає оновлену поїздку з розрахунками.
     */
    suspend fun endTrip(tripId: Long): TripResponse? {
        return try {
            val finishedTripResponse = apiService.endTrip(tripId)

            // 4. Оновити поїздку в кеші (Room)
            // OnConflictStrategy.REPLACE замінить стару на нову.
            tripDao.insert(finishedTripResponse.toEntity())

            finishedTripResponse
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Отримати деталі 1 поїздки (з routePoints).
     * Ми НЕ зберігаємо routePoints у кеші (бо їх багато).
     * Тому цей метод ЗАВЖДИ йде в мережу.
     */
    suspend fun getTripDetails(tripId: Long): TripResponse? {
        return try {
            apiService.getTripDetails(tripId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Проста функція-маппер, щоб не дублювати код.
 * Конвертує мережеву модель (DTO) у локальну (Entity).
 */
private fun TripResponse.toEntity(): TripEntity {
    return TripEntity(
        id = this.id,
        startTime = this.startTime,
        endTime = this.endTime,
        totalDistanceKm = this.totalDistanceKm,
        totalFuelConsumedL = this.totalFuelConsumedL,
        vehicleId = this.vehicle.id
    )
}