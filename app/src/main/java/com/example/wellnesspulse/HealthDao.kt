package com.example.wellnesspulse

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<HealthLogEntity>>

    @Query("SELECT * FROM health_logs WHERE date = :date LIMIT 1")
    fun getLogByDate(date: String): Flow<HealthLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HealthLogEntity)
}