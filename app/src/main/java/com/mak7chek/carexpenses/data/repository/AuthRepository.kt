package com.mak7chek.carexpenses.data.repository

import com.mak7chek.carexpenses.data.local.SessionManager
import com.mak7chek.carexpenses.data.dto.AuthRequest
import com.mak7chek.carexpenses.data.local.dao.TripDao
import com.mak7chek.carexpenses.data.local.dao.VehicleDao
import com.mak7chek.carexpenses.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val vehicleDao: VehicleDao,
    private val tripDao: TripDao
) {

    suspend fun login(request: AuthRequest): Boolean {
        return try {
            val response = apiService.login(request)
            sessionManager.saveAuthToken(response.token)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun register(request: AuthRequest): Boolean {
        return try {
            val response = apiService.register(request)
            sessionManager.saveAuthToken(response.token)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    fun checkCurrentToken(): Flow<String?> {
        return sessionManager.getAuthToken()
    }
    suspend fun logout() {
        sessionManager.clearAuthToken()
        vehicleDao.clearAll()
        tripDao.clearAll()
    }
}