package com.mak7chek.carexpenses.ui.screens.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mak7chek.carexpenses.data.local.entities.VehicleEntity
import com.mak7chek.carexpenses.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel()
{
    val vehicles : StateFlow<List<VehicleEntity>> = vehicleRepository.allVehicles
        .stateIn(
            scope =viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L) ,
            initialValue = emptyList()
        )
    init{
        refreshVehicles()
    }
    private fun refreshVehicles() {
        viewModelScope.launch {
            vehicleRepository.refreshVehicles()

        }
    }
    fun onVehicleSwiped(vehicle: VehicleEntity) {
        viewModelScope.launch {
            vehicleRepository.deleteVehicle(vehicle.id)
        }
    }

}