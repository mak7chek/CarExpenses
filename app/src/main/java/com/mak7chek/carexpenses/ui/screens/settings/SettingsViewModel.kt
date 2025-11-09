package com.mak7chek.carexpenses.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.repository.AuthRepository
import com.mak7chek.carexpenses.data.repository.SettingsRepository
import com.mak7chek.carexpenses.data.repository.ThemeSetting
import com.mak7chek.carexpenses.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val currentTheme: ThemeSetting = ThemeSetting.SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigateToAuth = MutableSharedFlow<Unit>()
    val navigateToAuth = _navigateToAuth.asSharedFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.themeSetting.collectLatest { theme ->
                _uiState.update { it.copy(currentTheme = theme) }
            }
        }
    }

    // --- Керування діалоговими вікнами ---
    fun onLogoutClicked() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onDeleteAccountClicked() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onThemeClicked() {
        _uiState.update { it.copy(showThemeDialog = true) }
    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                showLogoutDialog = false,
                showDeleteDialog = false,
                showThemeDialog = false
            )
        }
    }

    // --- Логіка підтвердження ---

    fun onLogoutConfirm() {
        viewModelScope.launch {
            authRepository.logout()
            _navigateToAuth.emit(Unit)
        }
    }

    fun onDeleteAccountConfirm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
            val result = userRepository.deleteAccount()

            if (result.isSuccess) {
                authRepository.logout()
                _navigateToAuth.emit(Unit)
            } else {
                _userMessage.emit("Помилка видалення акаунта: ${result.exceptionOrNull()?.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onThemeSelected(theme: ThemeSetting) {
        viewModelScope.launch {
            settingsRepository.saveThemeSetting(theme)
            onDismissDialog()
        }
    }
}