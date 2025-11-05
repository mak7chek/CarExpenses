package com.mak7chek.carexpenses.data.repository

import com.mak7chek.carexpenses.data.local.SessionManager
import com.mak7chek.carexpenses.data.dto.AuthRequest
import com.mak7chek.carexpenses.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    /**
     * Спробувати залогінитись.
     * Якщо успішно - зберегти токен.
     * @return true, якщо успіх, false, якщо ні.
     */
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

    /**
     * Спробувати зареєструватись.
     * Якщо успішно - зберегти токен.
     * @return true, якщо успіх, false, якщо ні.
     */
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

    /**
     * Вийти з акаунту.
     */
    suspend fun logout() {
        sessionManager.clearAuthToken()
        // Тут також можна очистити Room бази
        // (наприклад, vehicleDao.clearAll(), tripDao.clearAll())
    }
}