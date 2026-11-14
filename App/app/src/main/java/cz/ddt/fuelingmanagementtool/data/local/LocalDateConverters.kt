package cz.ddt.fuelingmanagementtool.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverters {

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long {
        return date.toEpochDay()
    }

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long): LocalDate {
        return LocalDate.ofEpochDay(epochDay)
    }
}