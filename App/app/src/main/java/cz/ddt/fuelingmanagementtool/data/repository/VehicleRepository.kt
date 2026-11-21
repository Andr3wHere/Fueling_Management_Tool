package cz.ddt.fuelingmanagementtool.data.repository

import cz.ddt.fuelingmanagementtool.data.local.dao.VehicleDao
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity
import javax.inject.Inject

class VehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao
) {
    fun observeVehicles() = vehicleDao.getAllVehicles()

    suspend fun addVehicle(vehicle: VehicleEntity) =
        vehicleDao.insertVehicle(vehicle)

    suspend fun deleteVehicle(vehicle: VehicleEntity) =
        vehicleDao.deleteVehicle(vehicle)
}