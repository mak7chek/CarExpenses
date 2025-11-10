// ui/screens/journal/JournalViewModel.kt
package com.mak7chek.carexpenses.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.local.entities.TripEntity // 👈 1. ПРАЦЮЄМО З ENTITY
import com.mak7chek.carexpenses.data.local.entities.VehicleEntity
import com.mak7chek.carexpenses.data.repository.TripRepository
import com.mak7chek.carexpenses.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import com.mak7chek.carexpenses.data.DownloaderService
import com.mak7chek.carexpenses.data.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
data class JournalUiState(
    val trips: List<TripEntity> = emptyList(),
    val vehicles: List<VehicleEntity> = emptyList(),

    val searchQuery: String = "",
    val selectedVehicleId: Long? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val minDistance: String = "",
    val maxDistance: String = "",

    val isLoading: Boolean = false,
    val loadErrorMessage: String? = null
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val vehicleRepository: VehicleRepository,
    private val authRepository: AuthRepository,
    private val downloaderService: DownloaderService
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    private var filterJob: Job? = null

    init {
        viewModelScope.launch {
            vehicleRepository.allVehicles.collect { vehicles ->
                _uiState.update { it.copy(vehicles = vehicles) }
            }
        }

        viewModelScope.launch {
            tripRepository.allTrips.collect { tripsFromRoom ->
                _uiState.update { it.copy(trips = tripsFromRoom) }
            }
        }

        applyFilters()
    }

    fun onSearchQueryChange(query: String) { _uiState.update { it.copy(searchQuery = query) } }
    fun onVehicleSelected(id: Long?) { _uiState.update { it.copy(selectedVehicleId = id) } }
    fun onMinDistanceChange(dist: String) { _uiState.update { it.copy(minDistance = dist) } }
    fun onMaxDistanceChange(dist: String) { _uiState.update { it.copy(maxDistance = dist) } }
    fun onDateFromChange(date: LocalDate?) { _uiState.update { it.copy(dateFrom = date) } }
    fun onDateToChange(date: LocalDate?) { _uiState.update { it.copy(dateTo = date) } }
    // ---------------------------------------------------

    fun applyFilters() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadErrorMessage = null) }

            delay(300)

            val state = _uiState.value
            try {
                tripRepository.refreshAndFilterTrips(
                    search = state.searchQuery.takeIf { it.isNotBlank() },
                    vehicleId = state.selectedVehicleId,
                    dateFrom = state.dateFrom,
                    dateTo = state.dateTo,
                    minDistance = state.minDistance.toDoubleOrNull(),
                    maxDistance = state.maxDistance.toDoubleOrNull()
                )
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(isLoading = false, loadErrorMessage = "Не вдалося завантажити журнал")
                }
            }
        }
    }

    fun onTripSwiped(trip: TripEntity) {
        viewModelScope.launch {
            try {
                tripRepository.delete(trip.id)
                _userMessage.emit("Поїздку #${trip.id} видалено")
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.emit("Не вдалося видалити поїздку")
            }
        }
    }

    fun onExportClicked() {
        viewModelScope.launch {
            val token = authRepository.checkCurrentToken().firstOrNull()
            if (token == null) {
                _userMessage.emit("Помилка: Ви не авторизовані")
                return@launch
            }

            val query = buildFilterQuery()

            downloaderService.downloadTripsReport(token, query)

            _userMessage.emit("Завантаження звіту почалося...")
        }
    }
    private fun buildFilterQuery(): String {
        val state = _uiState.value
        val params = mutableListOf<String>()

        state.searchQuery.takeIf { it.isNotBlank() }?.let {
            params.add("search=$it")
        }
        state.selectedVehicleId?.let {
            params.add("vehicleId=$it")
        }
        state.dateFrom?.let {
            params.add("dateFrom=$it")
        }
        state.dateTo?.let {
            params.add("dateTo=$it")
        }
        state.minDistance.toDoubleOrNull()?.let {
            params.add("minDistance=$it")
        }
        state.maxDistance.toDoubleOrNull()?.let {
            params.add("maxDistance=$it")
        }

        return if (params.isEmpty()) "" else params.joinToString(prefix = "?", separator = "&")
    }
}