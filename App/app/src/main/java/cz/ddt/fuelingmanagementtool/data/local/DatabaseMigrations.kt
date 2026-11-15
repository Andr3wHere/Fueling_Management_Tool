
package cz.ddt.fuelingmanagementtool.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val columns = mutableSetOf<String>()

        db.query("PRAGMA table_info(fuel_records)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(nameIndex))
            }
        }

        if ("liters" in columns && "price" in columns) {
            // Původní struktura databáze
            db.execSQL(
                """
                CREATE TABLE fuel_records_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date INTEGER NOT NULL,
                    volumeCentilitres INTEGER NOT NULL,
                    totalPriceHalers INTEGER NOT NULL,
                    unitPriceHalersPerLitre INTEGER NOT NULL,
                    vehicleId INTEGER
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                INSERT INTO fuel_records_new (
                    id, date, volumeCentilitres,
                    totalPriceHalers, unitPriceHalersPerLitre,
                    vehicleId
                )
                SELECT
                    id,
                    CAST(
                        julianday(
                            substr(date, 7, 4) || '-' ||
                            substr(date, 4, 2) || '-' ||
                            substr(date, 1, 2)
                        ) - julianday('1970-01-01')
                        AS INTEGER
                    ),
                    CAST(ROUND(liters * 100) AS INTEGER),
                    CAST(ROUND(price * 100) AS INTEGER),
                    CASE
                        WHEN liters > 0
                        THEN CAST(ROUND(price * 100 / liters) AS INTEGER)
                        ELSE 0
                    END,
                    vehicleId
                FROM fuel_records
                """.trimIndent()
            )

            db.execSQL("DROP TABLE fuel_records")
            db.execSQL(
                "ALTER TABLE fuel_records_new RENAME TO fuel_records"
            )
        } else if ("vehicleId" !in columns) {
            // Novější struktura verze 2
            db.execSQL(
                "ALTER TABLE fuel_records ADD COLUMN vehicleId INTEGER"
            )
        }
    }
}
