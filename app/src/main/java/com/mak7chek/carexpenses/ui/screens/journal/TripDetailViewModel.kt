package com.mak7chek.carexpenses.ui.screens.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.dto.TripResponse
import com.mak7chek.carexpenses.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Стан UI (Обличчя) для екрану Деталей
 */
data class TripDetailUiState(
    val trip: TripResponse? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * "Мізки" для екрану Деталей
 */
@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    savedStateHandle: SavedStateHandle // "Магія" Hilt для отримання ID з навігації
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState = _uiState.asStateFlow()

    // 1. Hilt *автоматично* дістає "tripId" з 'SavedStateHandle',
    //    тому що ми назвали його так само в 'Routes.kt' ("trip_detail/{tripId}")
    private val tripId: Long = checkNotNull(savedStateHandle["tripId"])

    init {
        // 2. Як тільки "мізки" створені, негайно завантажуємо дані
        loadTripDetails()
    }

    private fun loadTripDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 3. Йдемо в API (а не в Room), бо нам потрібні 'routePoints' для мапи
                val tripDetails = tripRepository.getTripDetails(tripId)
                _uiState.update {
                    it.copy(isLoading = false, trip = tripDetails)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Не вдалося завантажити деталі")
                }
            }
        }
    }
}