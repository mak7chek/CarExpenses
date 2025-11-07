// ui/screens/journal/JournalViewModel.kt
package com.mak7chek.carexpenses.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.local.entities.TripEntity
import com.mak7chek.carexpenses.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalUiState(
    val trips: List<TripEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tripRepository.allTrips.collect { tripList ->
                _uiState.update { it.copy(trips = tripList) }
            }
        }

        refreshTrips()
    }

    fun refreshTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                tripRepository.refreshTrips()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Не вдалося завантажити журнал"
                    )
                }
            }
        }
    }
}