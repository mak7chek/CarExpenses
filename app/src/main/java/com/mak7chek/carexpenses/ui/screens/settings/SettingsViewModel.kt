// ui/screens/settings/SettingsViewModel.kt
package com.mak7chek.carexpenses.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _navigateToAuth = MutableSharedFlow<Unit>()
    val navigateToAuth = _navigateToAuth.asSharedFlow()

    fun onLogoutClicked() {
        viewModelScope.launch {
            authRepository.logout()
            _navigateToAuth.emit(Unit)
        }
    }
}