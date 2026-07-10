package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sadhana_logs")
data class SadhanaLog(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val dateString: String,      // Format: yyyy-MM-dd
  val japaCount: Int,          // Number of completed malas (1 mala = 108 chants)
  val meditationMinutes: Int,  // Duration of meditation in minutes
  val notes: String = "",       // Personal reflection or divine thoughts
  val timestamp: Long = System.currentTimeMillis()
)
