package com.mak7chek.carexpenses.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.dto.AuthRequest
import com.mak7chek.carexpenses.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class AuthUiState(

    val name : String?="",
    val email : String ="",
    val password : String = "",
    val isLoading: Boolean = false,
    val isRegisterMode: Boolean = false,//false = логін / true =  регістрація
    val errorMessage : String? = null) {

}
@HiltViewModel
class AuthViewModel @Inject constructor (
    private val authRepository: AuthRepository
): ViewModel(){
    private val _uiState= MutableStateFlow(AuthUiState())

    val uiState = _uiState.asStateFlow()
    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()
    fun onNameChange (name:String){
        _uiState.update { it.copy(name =name, errorMessage = null) }
    }
    fun onUsernameChange(email:String){
        _uiState.update {
            it.copy(email = email , errorMessage = null)


        }
    }

    fun onPasswordChange(newPassword:String){
        _uiState.update {
            it.copy(password = newPassword , errorMessage = null)


        }
    }
    fun onToggleMode(){
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode , errorMessage = null)


        }
    }
    fun onAuthAction() {
        if (_uiState.value.isLoading) return

        val state = _uiState.value // Отримуємо стабільну копію стану

        // --- 1. ВАЛІДАЦІЯ ---
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email та пароль не можуть бути пустими") }
            return
        }
        var processedName: String? = null
        if (state.isRegisterMode) {
            if ( state.name.isNullOrBlank()) {
                _uiState.update { it.copy(errorMessage = "Ім'я не може бути пустим") }
                return
            }

            processedName = state.name.trim()
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val request = AuthRequest(
            name = processedName,
            email = state.email.trim(),
            password = state.password
        )

        viewModelScope.launch {
            val success = if (state.isRegisterMode) {
                authRepository.register(request)
            } else {
                authRepository.login(request)
            }

            if (success) {
                _navigationEvent.emit(Unit)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Невірні дані або email вже зайнятий"
                    )
                }
            }
        }
    }
}