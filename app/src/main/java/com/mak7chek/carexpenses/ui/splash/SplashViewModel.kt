// ui/splash/SplashViewModel.kt
package com.mak7chek.carexpenses.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthState {
    LOADING,
    AUTHENTICATED,
    UNAUTHENTICATED
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = authRepository.checkCurrentToken().first()

            if (token.isNullOrBlank()) {
                _authState.value = AuthState.UNAUTHENTICATED
            } else {
                // TODO: В ідеалі, тут треба зробити 1 запит до API,
                // щоб перевірити, чи токен не "протух".
                // Але поки що, якщо токен *є*, вважаємо, що все добре.
                _authState.value = AuthState.AUTHENTICATED
            }
        }
    }
}