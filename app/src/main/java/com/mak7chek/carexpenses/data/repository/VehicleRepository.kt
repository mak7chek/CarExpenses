// data/repository/VehicleRepository.kt
package com.mak7chek.carexpenses.data.repository

import com.mak7chek.carexpenses.data.local.dao.VehicleDao
import com.mak7chek.carexpenses.data.local.entities.VehicleEntity
import com.mak7chek.carexpenses.data.dto.VehicleRequest
import com.mak7chek.carexpenses.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepository @Inject constructor(
    private val apiService: ApiService,
    private val vehicleDao: VehicleDao
) {

    /**
     * ГОЛОВНИЙ ПОТІК ДАНИХ ДЛЯ UI.
     * UI (ViewModel) буде "слухати" цей Flow.
     * Як тільки дані в Room (DAO) зміняться, UI про це дізнається.
     */
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()

    /**
     * Примусово оновити список авто з бекенду.
     * Ми беремо дані з ApiService і кладемо їх у Room (DAO).
     * А оскільки UI "слухає" DAO, він автоматично оновиться.
     */
    suspend fun refreshVehicles() {
        try {
            // 1. Отримуємо свіжі дані з "банку" (Мережі)
            val networkVehicles = apiService.getVehicles()

            // 2. Конвертуємо їх у модель "гаманця" (Room Entity)
            val vehicleEntities = networkVehicles.map { response ->
                VehicleEntity(
                    id = response.id,
                    name = response.name,
                    make = response.make,
                    model = response.model,
                    year = response.year,
                    avgConsumptionLitersPer100Km = response.avgConsumptionLitersPer100Km,
                    fuelType = response.fuelType
                )
            }


            vehicleDao.clearAndInsert(vehicleEntities)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Створити нове авто.
     */
    suspend fun createVehicle(request: VehicleRequest) {
        try {
            apiService.createVehicle(request)
            refreshVehicles()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun deleteVehicle(id: Long) {
        try {
            apiService.deleteVehicle(id)
            refreshVehicles()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getVehicleById(id: Long): Flow<VehicleEntity?> {
        return vehicleDao.getVehicleById(id)
    }
    suspend fun updateVehicle(id: Long, request: VehicleRequest) {
        try {
            apiService.updateVehicle(id, request)
            refreshVehicles()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}