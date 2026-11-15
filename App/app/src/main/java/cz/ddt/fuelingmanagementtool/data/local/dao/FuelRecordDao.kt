
package cz.ddt.fuelingmanagementtool.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelRecordDao {

    @Insert
    suspend fun insert(record: FuelRecordEntity)

    @Query("SELECT * FROM fuel_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<FuelRecordEntity>>

    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY id DESC")
    fun getRecordsByVehicle(vehicleId: Int): Flow<List<FuelRecordEntity>>

    @Delete
    suspend fun delete(record: FuelRecordEntity)
}
