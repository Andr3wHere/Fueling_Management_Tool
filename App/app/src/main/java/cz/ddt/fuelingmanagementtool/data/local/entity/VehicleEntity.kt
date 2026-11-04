package cz.ddt.fuelingmanagementtool.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Vehicle with one required fuel type and an optional second fuel type.
 * Primary/secondary identifies the two entries, not actual fuel usage priority.
 * Tank capacities represent usable fuel quantities: litres for petrol, diesel
 * and LPG, kilograms for CNG. A null capacity means it was not provided.
 */
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryFuelType: FuelType,
    val primaryTankCapacity: Double? = null,
    val secondaryFuelType: FuelType? = null,
    val secondaryTankCapacity: Double? = null,
)
