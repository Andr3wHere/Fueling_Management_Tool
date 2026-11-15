package cz.ddt.fuelingmanagementtool.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.ddt.fuelingmanagementtool.data.local.dao.FuelRecordDao
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FuelRecordDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var fuelRecordDao: FuelRecordDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        fuelRecordDao = database.fuelRecordDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertObserveAndDeleteRecord() = runTest {
        val updates = Channel<List<FuelRecordEntity>>(Channel.UNLIMITED)

        backgroundScope.launch {
            fuelRecordDao.getAllRecords()
                .distinctUntilChanged()
                .collect { records ->
                    updates.send(records)
                }
        }

        assertTrue(updates.receive().isEmpty())

        val record = FuelRecordEntity(
            date = LocalDate.of(2026, 11, 11),
            volumeCentilitres = 4250,
            totalPriceHalers = 152575,
            unitPriceHalersPerLitre = 3590
        )

        val insertedId = fuelRecordDao.insertRecord(record)

        assertTrue(insertedId > 0)

        val savedRecord = record.copy(id = insertedId)
        val recordsAfterInsert = updates.receive()

        assertEquals(listOf(savedRecord), recordsAfterInsert)

        fuelRecordDao.deleteRecord(savedRecord)

        assertTrue(updates.receive().isEmpty())
    }
}