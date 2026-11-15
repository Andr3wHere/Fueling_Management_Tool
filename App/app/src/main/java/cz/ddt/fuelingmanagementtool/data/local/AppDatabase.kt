
package cz.ddt.fuelingmanagementtool.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.ddt.fuelingmanagementtool.data.local.dao.VehicleDao
import cz.ddt.fuelingmanagementtool.data.local.dao.FuelRecordDao
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity

@Database(
    entities = [
        VehicleEntity::class,
        FuelRecordEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao

    abstract fun fuelRecordDao(): FuelRecordDao
}
