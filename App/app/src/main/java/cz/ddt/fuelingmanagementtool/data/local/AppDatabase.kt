package cz.ddt.fuelingmanagementtool.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.ddt.fuelingmanagementtool.data.local.dao.VehicleDao
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity

@Database(
    entities = [VehicleEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
}