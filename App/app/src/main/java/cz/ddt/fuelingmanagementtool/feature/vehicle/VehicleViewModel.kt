package cz.ddt.fuelingmanagementtool.feature.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelType
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity
import cz.ddt.fuelingmanagementtool.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.database.sqlite.SQLiteException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    val vehicles = repository.observeVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _saveStatus = MutableStateFlow(VehicleSaveStatus.IDLE)
    val saveStatus = _saveStatus.asStateFlow()

    fun addVehicle(
        name: String,
        primaryFuelType: FuelType,
        primaryTankCapacity: Double? = null,
        secondaryFuelType: FuelType? = null,
        secondaryTankCapacity: Double? = null
    ) {
        if (_saveStatus.value == VehicleSaveStatus.SAVING ||
            _saveStatus.value == VehicleSaveStatus.SAVED
        ) return

        _saveStatus.value = VehicleSaveStatus.SAVING

        viewModelScope.launch {
            try {
                repository.addVehicle(
                    VehicleEntity(
                        name = name.trim(),
                        primaryFuelType = primaryFuelType,
                        primaryTankCapacity = primaryTankCapacity,
                        secondaryFuelType = secondaryFuelType,
                        secondaryTankCapacity = secondaryTankCapacity
                    )
                )
                _saveStatus.value = VehicleSaveStatus.SAVED
            } catch (_: SQLiteException) {
                _saveStatus.value = VehicleSaveStatus.FAILED
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = VehicleSaveStatus.IDLE
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }
}
enum class VehicleSaveStatus {
    IDLE, SAVING, SAVED, FAILED
}