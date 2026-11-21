package cz.ddt.fuelingmanagementtool.data.repository

import cz.ddt.fuelingmanagementtool.data.local.dao.FuelRecordDao
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import javax.inject.Inject

class FuelRecordRepository @Inject constructor(
    private val fuelRecordDao: FuelRecordDao
) {
    suspend fun getRecordById(id: Long): FuelRecordEntity? {
        return fuelRecordDao.getRecordById(id)
    }

    suspend fun updateRecord(record: FuelRecordEntity): Int {
        return fuelRecordDao.updateRecord(record)
    }
}