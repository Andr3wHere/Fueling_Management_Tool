package cz.ddt.fuelingmanagementtool.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.ddt.fuelingmanagementtool.data.local.dao.FuelRecordDao
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun updateRecordFromMainThreadAndObserveChanges() = runTest {
        val record = FuelRecordEntity(
            date = LocalDate.of(2026, 11, 15),
            volumeCentilitres = 3690,
            totalPriceHalers = 139851,
            unitPriceHalersPerLitre = 3790,
            vehicleId = 7L
        )
        val savedRecord = record.copy(id = fuelRecordDao.insertRecord(record))
        val editedRecord = savedRecord.copy(
            date = LocalDate.of(2026, 11, 16),
            volumeCentilitres = 3890,
            totalPriceHalers = 159101,
            unitPriceHalersPerLitre = 4090
        )

        val updates = Channel<List<FuelRecordEntity>>(Channel.UNLIMITED)
        backgroundScope.launch {
            fuelRecordDao.getAllRecords()
                .distinctUntilChanged()
                .collect { records ->
                    updates.send(records)
                }
        }
        assertEquals(listOf(savedRecord), updates.receive())

        withContext(Dispatchers.Main) {
            assertEquals(savedRecord, fuelRecordDao.getRecordById(savedRecord.id))
            assertEquals(1, fuelRecordDao.updateRecord(editedRecord))
            assertEquals(editedRecord, fuelRecordDao.getRecordById(savedRecord.id))
        }

        assertEquals(listOf(editedRecord), updates.receive())
    }

    @Test
    fun deletesOnlySelectedRecord() = runTest {
        val firstRecord = FuelRecordEntity(
            date = LocalDate.of(2026, 11, 15),
            volumeCentilitres = 3690,
            totalPriceHalers = 139851,
            unitPriceHalersPerLitre = 3790,
            vehicleId = 7L
        )
        val secondRecord = firstRecord.copy(date = firstRecord.date.plusDays(1))
        val recordToDelete = firstRecord.copy(id = fuelRecordDao.insertRecord(firstRecord))
        val remainingRecord = secondRecord.copy(id = fuelRecordDao.insertRecord(secondRecord))

        val updates = Channel<List<FuelRecordEntity>>(Channel.UNLIMITED)
        backgroundScope.launch {
            fuelRecordDao.getAllRecords()
                .distinctUntilChanged()
                .collect { records ->
                    updates.send(records)
                }
        }
        assertEquals(listOf(remainingRecord, recordToDelete), updates.receive())

        fuelRecordDao.deleteRecord(recordToDelete)

        assertEquals(listOf(remainingRecord), updates.receive())
        assertNull(fuelRecordDao.getRecordById(recordToDelete.id))
    }
}
