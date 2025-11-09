package com.mak7chek.carexpenses.ui.screens.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.dto.TripDetailResponse // 👈 1. ВИКОРИСТОВУЄМО НОВИЙ DTO
import com.mak7chek.carexpenses.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripDetailUiState(
    val trip: TripDetailResponse? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    private val tripId: Long = checkNotNull(savedStateHandle["tripId"])

    init {
        loadTripDetails()
    }

    private fun loadTripDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = tripRepository.getTripDetailsFromApi(tripId)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isLoading = false, trip = result.getOrNull())
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Не вдалося завантажити деталі")
                }
            }
        }
    }

    fun onNotesChange(newNotes: String) {
        _uiState.update { state ->
            state.copy(
                trip = state.trip?.copy(notes = newNotes)
            )
        }
    }

    fun saveNotes() {
        val currentNotes = _uiState.value.trip?.notes

        viewModelScope.launch {
            try {
                tripRepository.updateTripNotes(tripId, currentNotes)
                _userMessage.emit("Нотатку збережено")
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.emit("Помилка збереження нотатки")
            }
        }
    }
}