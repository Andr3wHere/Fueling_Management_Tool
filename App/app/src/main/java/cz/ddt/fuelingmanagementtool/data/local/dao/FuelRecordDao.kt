package cz.ddt.fuelingmanagementtool.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelRecordDao {

    @Insert
    suspend fun insertRecord(record: FuelRecordEntity): Long

    @Delete
    suspend fun deleteRecord(record: FuelRecordEntity)

    @Query("SELECT * FROM fuel_records ORDER BY date DESC, id DESC")
    fun getAllRecords(): Flow<List<FuelRecordEntity>>
}