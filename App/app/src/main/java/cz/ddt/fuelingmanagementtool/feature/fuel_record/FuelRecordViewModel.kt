
package cz.ddt.fuelingmanagementtool.feature.fuel_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.ddt.fuelingmanagementtool.data.local.dao.FuelRecordDao
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import cz.ddt.fuelingmanagementtool.data.repository.FuelRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class FuelRecordViewModel @Inject constructor(
    private val fuelRecordDao: FuelRecordDao,
    private val fuelRecordRepository: FuelRecordRepository
) : ViewModel() {

    val records = fuelRecordDao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError = _saveError.asStateFlow()

    private val _saveSuccessful = MutableStateFlow(false)
    val saveSuccessful = _saveSuccessful.asStateFlow()

    suspend fun getRecordById(id: Long): FuelRecordEntity? {
        return fuelRecordRepository.getRecordById(id)
    }
    fun saveRecord(
        date: String,
        liters: String,
        price: String,
        unitPrice: String,
        vehicleId: Long,
        originalRecord: FuelRecordEntity? = null
    ) {
        if (_isSaving.value) return

        _saveError.value = null
        _saveSuccessful.value = false

        val parsedDate = try {
            LocalDate.parse(date.trim())
        } catch (e: Exception) {
            _saveError.value = "Neplatné datum. Použij RRRR-MM-DD."
            return
        }

        val volume = parseScaled(liters, 100)
        val totalPrice = parseScaled(price, 100)
        val unitPriceValue = parseScaled(unitPrice, 100)

        if (volume == null || volume <= 0L) {
            _saveError.value = "Zadej platné množství paliva."
            return
        }

        if (totalPrice == null || totalPrice <= 0L) {
            _saveError.value = "Zadej platnou celkovou cenu."
            return
        }

        if (unitPriceValue == null || unitPriceValue <= 0L) {
            _saveError.value = "Zadej platnou jednotkovou cenu."
            return
        }

        _isSaving.value = true

        viewModelScope.launch {
            try {
                if (originalRecord == null) {
                    fuelRecordDao.insertRecord(
                        FuelRecordEntity(
                            date = parsedDate,
                            volumeCentilitres = volume,
                            totalPriceHalers = totalPrice,
                            unitPriceHalersPerLitre = unitPriceValue,
                            vehicleId = vehicleId
                        )
                    )
                } else {
                    val updatedRecord = originalRecord.copy(
                        date = parsedDate,
                        volumeCentilitres = volume,
                        totalPriceHalers = totalPrice,
                        unitPriceHalersPerLitre = unitPriceValue
                    )

                    if (fuelRecordRepository.updateRecord(updatedRecord) == 0) {
                        _saveError.value = "Tankování už neexistuje."
                        return@launch
                    }
                }
                _saveSuccessful.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _saveError.value =
                    "Záznam se nepodařilo uložit: ${e.message ?: "Neznámá chyba"}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearSaveStatus() {
        _saveError.value = null
        _saveSuccessful.value = false
    }

    fun deleteRecord(record: FuelRecordEntity) {
        viewModelScope.launch {
            try {
                fuelRecordDao.deleteRecord(record)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _saveError.value =
                    "Záznam se nepodařilo smazat: ${e.message ?: "Neznámá chyba"}"
            }
        }
    }


    private fun parseScaled(
        input: String,
        scale: Int
    ): Long? {
        return try {
            val normalized = input
                .trim()
                .replace(Regex("[\\s\\u00A0\\u202F]"), "")
                .replace(',', '.')

            BigDecimal(normalized)
                .multiply(BigDecimal.valueOf(scale.toLong()))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        } catch (e: Exception) {
            null
        }
    }
}
