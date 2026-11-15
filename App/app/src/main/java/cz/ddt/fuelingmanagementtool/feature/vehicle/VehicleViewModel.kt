
package cz.ddt.fuelingmanagementtool.feature.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.ddt.fuelingmanagementtool.data.local.dao.VehicleDao
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelType
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleDao: VehicleDao
) : ViewModel() {

    val vehicles = vehicleDao.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addVehicle(
        name: String,
        primaryFuelType: FuelType,
        primaryTankCapacity: Double? = null,
        secondaryFuelType: FuelType? = null,
        secondaryTankCapacity: Double? = null
    ) {
        viewModelScope.launch {
            vehicleDao.insertVehicle(
                VehicleEntity(
                    name = name,
                    primaryFuelType = primaryFuelType,
                    primaryTankCapacity = primaryTankCapacity,
                    secondaryFuelType = secondaryFuelType,
                    secondaryTankCapacity = secondaryTankCapacity
                )
            )
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            vehicleDao.deleteVehicle(vehicle)
        }
    }
}
