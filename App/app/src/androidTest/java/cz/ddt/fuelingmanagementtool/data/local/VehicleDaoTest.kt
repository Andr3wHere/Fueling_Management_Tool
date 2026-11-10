package cz.ddt.fuelingmanagementtool.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.ddt.fuelingmanagementtool.data.local.dao.VehicleDao
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelType
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class VehicleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var vehicleDao: VehicleDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        vehicleDao = database.vehicleDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertVehicleAndGetAllVehicles() = runTest {
        val vehicle = VehicleEntity(
            id = 1L,
            name = "Škoda Octavia",
            primaryFuelType = FuelType.DIESEL,
            primaryTankCapacity = 50.0
        )

        vehicleDao.insertVehicle(vehicle)

        val vehiclesList = vehicleDao.getAllVehicles().first()

        assertTrue(vehiclesList.isNotEmpty())
        assertEquals(1, vehiclesList.size)
        assertEquals(vehicle.name, vehiclesList[0].name)
        assertEquals(vehicle.primaryFuelType, vehiclesList[0].primaryFuelType)
    }
}