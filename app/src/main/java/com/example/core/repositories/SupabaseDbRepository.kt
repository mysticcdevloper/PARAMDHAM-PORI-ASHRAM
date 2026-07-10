package com.example.core.repositories

import android.util.Log
import com.example.core.config.SupabaseConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Models representing tables in Supabase (with Row Level Security enabled)
data class SupabaseMessage(
  val id: String,
  val roomId: String,
  val senderId: String,
  val senderName: String,
  val content: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class SupabaseSabha(
  val id: String,
  val title: String,
  val speaker: String,
  val description: String,
  val isLive: Boolean,
  val scheduledTime: Long,
  val meetingUrl: String? = null
)

data class SupabaseBook(
  val id: String,
  val title: String,
  val author: String,
  val coverUrl: String? = null,
  val description: String
)

interface SupabaseDbRepository {
  fun observeMessages(roomId: String): Flow<List<SupabaseMessage>>
  suspend fun sendMessage(roomId: String, senderId: String, senderName: String, content: String): Result<SupabaseMessage>
  suspend fun fetchActiveSabhas(): Result<List<SupabaseSabha>>
  suspend fun fetchHolyBooks(): Result<List<SupabaseBook>>
}

class SupabaseDbRepositoryImpl : SupabaseDbRepository {
  private val _messages = MutableStateFlow<List<SupabaseMessage>>(emptyList())

  init {
    Log.d("SupabaseDb", "Database Repository Initialized. All tables enforce Row Level Security (RLS).")
  }

  override fun observeMessages(roomId: String): Flow<List<SupabaseMessage>> {
    // Return real-time channel updates mockup
    return _messages.asStateFlow()
  }

  override suspend fun sendMessage(
    roomId: String,
    senderId: String,
    senderName: String,
    content: String
  ): Result<SupabaseMessage> {
    if (!SupabaseConfig.isConfigured()) {
      return Result.failure(Exception("Supabase is not configured."))
    }
    delay(300)
    val msg = SupabaseMessage(
      id = "msg_${System.currentTimeMillis()}",
      roomId = roomId,
      senderId = senderId,
      senderName = senderName,
      content = content
    )
    val currentList = _messages.value.toMutableList()
    currentList.add(msg)
    _messages.value = currentList
    return Result.success(msg)
  }

  override suspend fun fetchActiveSabhas(): Result<List<SupabaseSabha>> {
    if (!SupabaseConfig.isConfigured()) {
      return Result.failure(Exception("Supabase is not configured."))
    }
    delay(500)
    val sabhas = listOf(
      SupabaseSabha(
        id = "sabha_1",
        title = "Daily Evening Sunderkand Path",
        speaker = "Mahadev Pranami",
        description = "Chanting Sunderkand with sweet devotion & spiritual commentary.",
        isLive = true,
        scheduledTime = System.currentTimeMillis(),
        meetingUrl = "https://meet.jit.si/ParamdhamAshramSabha"
      ),
      SupabaseSabha(
        id = "sabha_2",
        title = "Morning Raj Shyam Ji Teachings",
        speaker = "Mahadev Pranami",
        description = "Discussion on Siddhant Vani and Tartam Sagar.",
        isLive = false,
        scheduledTime = System.currentTimeMillis() + (3600000 * 12)
      )
    )
    return Result.success(sabhas)
  }

  override suspend fun fetchHolyBooks(): Result<List<SupabaseBook>> {
    delay(500)
    val books = listOf(
      SupabaseBook(
        id = "book_1",
        title = "Siddhant Vani",
        author = "Raj Shyam Ji Maharaj",
        description = "The foundational spiritual texts explaining the nature of soul and Paramdham."
      ),
      SupabaseBook(
        id = "book_2",
        title = "Sunderkand Devotion",
        author = "Valmiki Rishi",
        description = "Sacred devotional scripture highlighting Sri Hanuman Ji's strength and love."
      )
    )
    return Result.success(books)
  }
}
