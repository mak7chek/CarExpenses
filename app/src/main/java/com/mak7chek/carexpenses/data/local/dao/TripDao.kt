// data/local/dao/TripDao.kt
package com.mak7chek.carexpenses.data.local.dao

import androidx.room.*
import com.mak7chek.carexpenses.data.local.entities.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun clearAndInsert(trips: List<TripEntity>) {
        clearAll()
        trips.forEach { insert(it) }
    }

    @Query("DELETE FROM trips")
    suspend fun clearAll()

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteById(tripId: Long)

    @Query("UPDATE trips SET notes = :notes WHERE id = :tripId")
    suspend fun updateNotes(tripId: Long, notes: String?)
}