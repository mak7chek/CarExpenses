// /data/repository/UserRepository.kt
package com.mak7chek.carexpenses.data.repository

import com.mak7chek.carexpenses.data.dto.UpdateNameRequest
import com.mak7chek.carexpenses.data.dto.UpdatePasswordRequest
import com.mak7chek.carexpenses.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

// Цей репозиторій відповідає за *залогіненого* користувача
@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun updateUserName(newName: String): Result<String> {
        return try {
            val response = apiService.updateName(UpdateNameRequest(newName))
            if (response.isSuccessful) {
                Result.success(response.body()?.get("newName") ?: newName)
            } else {
                // TODO: Обробити помилку з response.errorBody()
                Result.failure(Exception("Помилка оновлення імені"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserPassword(old: String, new: String): Result<Unit> {
        return try {
            val response = apiService.updatePassword(UpdatePasswordRequest(old, new))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка оновлення пароля"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response = apiService.deleteAccount()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Не вдалося видалити акаунт"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}