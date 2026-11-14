package cz.ddt.fuelingmanagementtool.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "fuel_records")
data class FuelRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val volumeCentilitres: Long,
    val totalPriceHalers: Long,
    val unitPriceHalersPerLitre: Long,
)