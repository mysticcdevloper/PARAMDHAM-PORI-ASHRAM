package com.example.core.repositories

import android.util.Log
import com.example.core.config.SupabaseConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SupabaseUser(
  val id: String,
  val email: String,
  val fullName: String,
  val role: String = "member",
  val createdAt: Long = System.currentTimeMillis()
)

interface SupabaseAuthRepository {
  val currentUser: Flow<SupabaseUser?>
  suspend fun signUp(email: String, password: String, fullName: String): Result<SupabaseUser>
  suspend fun signIn(email: String, password: String): Result<SupabaseUser>
  suspend fun signOut(): Result<Unit>
  suspend fun refreshSession(): Result<SupabaseUser>
}

class SupabaseAuthRepositoryImpl : SupabaseAuthRepository {
  private val _currentUser = MutableStateFlow<SupabaseUser?>(null)
  override val currentUser: Flow<SupabaseUser?> = _currentUser.asStateFlow()

  init {
    Log.d("SupabaseAuth", "Auth Repository Initialized (Row Level Security ready)")
  }

  override suspend fun signUp(email: String, password: String, fullName: String): Result<SupabaseUser> {
    if (!SupabaseConfig.isConfigured()) {
      return Result.failure(Exception("Supabase is not configured yet. Set up in .env file."))
    }
    // Simulate Supabase Signup API Request (JWT Auth)
    delay(1000)
    val newUser = SupabaseUser(
      id = "user_${System.currentTimeMillis()}",
      email = email,
      fullName = fullName
    )
    _currentUser.value = newUser
    return Result.success(newUser)
  }

  override suspend fun signIn(email: String, password: String): Result<SupabaseUser> {
    if (!SupabaseConfig.isConfigured()) {
      return Result.failure(Exception("Supabase is not configured yet. Set up in .env file."))
    }
    // Simulate Supabase SignIn API Request (JWT Session Token returned)
    delay(1000)
    val mockUser = SupabaseUser(
      id = "user_9988",
      email = email,
      fullName = "Seva Ji"
    )
    _currentUser.value = mockUser
    return Result.success(mockUser)
  }

  override suspend fun signOut(): Result<Unit> {
    delay(500)
    _currentUser.value = null
    return Result.success(Unit)
  }

  override suspend fun refreshSession(): Result<SupabaseUser> {
    val current = _currentUser.value
    return if (current != null) {
      Result.success(current)
    } else {
      Result.failure(Exception("No active session"))
    }
  }
}
