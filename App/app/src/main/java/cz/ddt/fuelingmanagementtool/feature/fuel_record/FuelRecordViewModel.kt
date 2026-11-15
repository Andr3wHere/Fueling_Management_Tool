
package cz.ddt.fuelingmanagementtool.feature.fuel_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.ddt.fuelingmanagementtool.data.local.dao.FuelRecordDao
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FuelRecordViewModel @Inject constructor(
    private val fuelRecordDao: FuelRecordDao
) : ViewModel() {

    val records = fuelRecordDao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveRecord(
        date: String,
        liters: Double,
        price: Double
    ) {
        viewModelScope.launch {
            fuelRecordDao.insert(
                FuelRecordEntity(
                    date = date,
                    liters = liters,
                    price = price
                )
            )
        }
    }

    fun deleteRecord(record: FuelRecordEntity) {
        viewModelScope.launch {
            fuelRecordDao.delete(record)
        }
    }
}
