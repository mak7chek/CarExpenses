// data/local/AppDatabase.kt
package com.mak7chek.carexpenses.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mak7chek.carexpenses.data.local.dao.LocalGpsPointsDao
import com.mak7chek.carexpenses.data.local.dao.TripDao
import com.mak7chek.carexpenses.data.local.dao.VehicleDao
import com.mak7chek.carexpenses.data.local.entities.LocalGpsPoint
import com.mak7chek.carexpenses.data.local.entities.TripEntity
import com.mak7chek.carexpenses.data.local.entities.VehicleEntity

@Database(
    entities = [VehicleEntity::class, TripEntity::class, LocalGpsPoint::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao

    abstract fun localGpsPointDao (): LocalGpsPointsDao
}