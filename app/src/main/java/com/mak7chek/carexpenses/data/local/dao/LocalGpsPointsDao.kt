package com.mak7chek.carexpenses.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mak7chek.carexpenses.data.local.entities.LocalGpsPoint

@Dao
interface LocalGpsPointsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint (point: LocalGpsPoint)

    @Query("SELECT * FROM  gps_points_buffer WHERE tripId = :tripId " )
    suspend fun getUnsyncedPointsForTrip(tripId:Long): List<LocalGpsPoint>

    @Query ("DELETE FROM gps_points_buffer WHERE id IN(:pointsIds)")
    suspend fun deletePointsByIds(pointsIds: List<Long>)
}