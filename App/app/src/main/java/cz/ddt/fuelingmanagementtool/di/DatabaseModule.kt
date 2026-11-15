package cz.ddt.fuelingmanagementtool.di

import android.content.Context
import androidx.room.Room
import cz.ddt.fuelingmanagementtool.data.local.AppDatabase
import cz.ddt.fuelingmanagementtool.data.local.dao.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cz.ddt.fuelingmanagementtool.data.local.dao.FuelRecordDao

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `fuel_records` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `date` TEXT NOT NULL,
                `liters` REAL NOT NULL,
                `price` REAL NOT NULL,
                `vehicleId` INTEGER
            )
            """.trimIndent()
        )
    }
}
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fueling_management_db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideVehicleDao(database: AppDatabase): VehicleDao {
        return database.vehicleDao()
    }

    @Provides
    @Singleton
    fun provideFuelRecordDao(database: AppDatabase): FuelRecordDao {
        return database.fuelRecordDao()
    }
}