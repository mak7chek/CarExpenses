package com.mak7chek.carexpenses.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.mak7chek.carexpenses.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//=================================================================
// 1. ViewModel для Екрану "Змінити ім'я"
//=================================================================

data class UpdateNameUiState(
    val name: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class UpdateNameViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateNameUiState())
    val uiState = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack = _navigateBack.asSharedFlow()

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            if (_uiState.value.name.isBlank()) {
                _userMessage.emit("Ім'я не може бути порожнім")
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.updateUserName(_uiState.value.name)

            if (result.isSuccess) {
                _userMessage.emit("Ім'я успішно оновлено!")
                _navigateBack.emit(Unit)
            } else {
                _userMessage.emit("Помилка: ${result.exceptionOrNull()?.message}")
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

//=================================================================
// 2. Composable для Екрану "Змінити ім'я"
//=================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateNameScreen(
    navController: NavController,
    viewModel: UpdateNameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            navController.popBackStack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Змінити ім'я") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Нове ім'я") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Button(
                onClick = viewModel::onSaveClicked,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Зберегти")
                }
            }
        }
    }
}

//=================================================================
// 3. ViewModel для Екрану "Змінити пароль"
//=================================================================

data class UpdatePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val passwordError: String? = null
)

@HiltViewModel
class UpdatePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdatePasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack = _navigateBack.asSharedFlow()

    fun onOldPasswordChange(pass: String) {
        _uiState.update { it.copy(oldPassword = pass) }
    }
    fun onNewPasswordChange(pass: String) {
        _uiState.update { it.copy(newPassword = pass) }
    }
    fun onConfirmPasswordChange(pass: String) {
        _uiState.update { it.copy(confirmPassword = pass) }
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.newPassword != state.confirmPassword) {
                _uiState.update { it.copy(passwordError = "Паролі не співпадають") }
                return@launch
            }
            if (state.newPassword.length < 6) { // Або твоя логіка валідації
                _uiState.update { it.copy(passwordError = "Пароль має бути > 6 символів") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, passwordError = null) }
            val result = userRepository.updateUserPassword(state.oldPassword, state.newPassword)

            if (result.isSuccess) {
                _userMessage.emit("Пароль успішно оновлено!")
                _navigateBack.emit(Unit)
            } else {
                _userMessage.emit("Помилка: ${result.exceptionOrNull()?.message}")
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

//=================================================================
// 4. Composable для Екрану "Змінити пароль"
//=================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePasswordScreen(
    navController: NavController,
    viewModel: UpdatePasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            navController.popBackStack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Змінити пароль") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.oldPassword,
                onValueChange = viewModel::onOldPasswordChange,
                label = { Text("Старий пароль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = uiState.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                label = { Text("Новий пароль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Підтвердіть новий пароль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.passwordError != null,
                supportingText = {
                    if (uiState.passwordError != null) {
                        Text(uiState.passwordError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Button(
                onClick = viewModel::onSaveClicked,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Зберегти")
                }
            }
        }
    }
}