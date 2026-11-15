package cz.ddt.fuelingmanagementtool.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.ddt.fuelingmanagementtool.data.local.dao.FuelRecordDao
import cz.ddt.fuelingmanagementtool.data.local.dao.VehicleDao
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        FuelRecordEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(LocalDateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao

    abstract fun fuelRecordDao(): FuelRecordDao
}