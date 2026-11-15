
package cz.ddt.fuelingmanagementtool.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_records")
data class FuelRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,
    val liters: Double,
    val price: Double,
    val vehicleId: Int? = null
)
