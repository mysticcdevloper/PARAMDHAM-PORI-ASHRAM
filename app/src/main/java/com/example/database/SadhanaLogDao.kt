package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SadhanaLogDao {
  @Query("SELECT * FROM sadhana_logs ORDER BY timestamp DESC")
  fun getAllLogs(): Flow<List<SadhanaLog>>

  @Query("SELECT * FROM sadhana_logs WHERE dateString = :date LIMIT 1")
  suspend fun getLogByDate(date: String): SadhanaLog?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: SadhanaLog)

  @Query("DELETE FROM sadhana_logs WHERE id = :id")
  suspend fun deleteLogById(id: Int)

  @Query("DELETE FROM sadhana_logs")
  suspend fun clearAllLogs()
}
