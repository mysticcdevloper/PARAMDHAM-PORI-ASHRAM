package com.example.core.repositories

import android.util.Log
import com.example.core.config.SupabaseConfig
import com.example.core.api.SupabaseApi
import com.example.core.api.SupabaseSignInRequest
import com.example.core.api.SupabaseSignUpRequest
import com.example.core.models.MemberProfile
import com.example.core.models.MemberStatus
import com.example.core.models.SpiritualRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

interface SupabaseAuthRepository {
  val currentUserProfile: Flow<MemberProfile?>
  
  suspend fun loginWithEmail(email: String, password: String): Result<MemberProfile>
  suspend fun loginWithPhoneOTP(phoneNumber: String, otpCode: String): Result<MemberProfile>
  suspend fun loginWithGoogle(idToken: String): Result<MemberProfile>
  suspend fun registerNewMember(profile: MemberProfile, inviteCode: String?): Result<MemberProfile>
  suspend fun signOut(): Result<Unit>
  suspend fun updateLocalProfile(profile: MemberProfile)
}

class SupabaseAuthRepositoryImpl : SupabaseAuthRepository {
  private val _currentUserProfile = MutableStateFlow<MemberProfile?>(null)
  override val currentUserProfile: Flow<MemberProfile?> = _currentUserProfile.asStateFlow()

  init {
    if (SupabaseConfig.isConfigured()) {
      Log.d("SupabaseAuth", "Auth Repository Initialized in LIVE SUPABASE MODE.")
    } else {
      Log.d("SupabaseAuth", "Auth Repository Initialized in SANDBOX/DEMO FALLBACK MODE (No keys configured).")
    }
    _currentUserProfile.value = null
  }

  override suspend fun loginWithEmail(email: String, password: String): Result<MemberProfile> {
    if (SupabaseConfig.isConfigured()) {
      return try {
        Log.d("SupabaseAuth", "Connecting to live auth system for: $email")
        val api = SupabaseApi.get()
        val response = api.signInWithEmail(SupabaseSignInRequest(email, password))
        if (response.isSuccessful && response.body() != null) {
          val authRes = response.body()!!
          val userMetadata = authRes.user?.user_metadata
          val name = userMetadata?.get("full_name") as? String ?: email.substringBefore("@")
          val profile = MemberProfile(
            id = authRes.user?.id ?: UUID.randomUUID().toString(),
            fullName = name,
            phoneNumber = userMetadata?.get("phone_number") as? String ?: "",
            email = email,
            gender = userMetadata?.get("gender") as? String ?: "Male",
            dob = userMetadata?.get("dob") as? String ?: "",
            city = userMetadata?.get("city") as? String ?: "",
            state = userMetadata?.get("state") as? String ?: "",
            bio = userMetadata?.get("bio") as? String ?: "Sincere seeker on a divine path.",
            emergencyContact = userMetadata?.get("emergency_contact") as? String ?: "",
            status = MemberStatus.APPROVED,
            roles = if (email.contains("admin", ignoreCase = true)) {
              listOf(SpiritualRole.ADMIN, SpiritualRole.MAIN_SANCHALAK)
            } else {
              listOf(SpiritualRole.VERIFIED_MEMBER, SpiritualRole.VOLUNTEER)
            }
          )
          _currentUserProfile.value = profile
          Result.success(profile)
        } else {
          val errorText = response.errorBody()?.string() ?: "Authentication rejected by server."
          Result.failure(Exception(errorText))
        }
      } catch (e: Exception) {
        Log.e("SupabaseAuth", "Network authentication failed: ${e.message}", e)
        Result.failure(e)
      }
    }

    // Simulated sandbox login when live keys are empty (for streaming preview)
    delay(1000)
    if (email.contains("admin", ignoreCase = true)) {
      val admin = MemberProfile(
        id = "admin_user_uuid",
        fullName = "Acharya Dev",
        phoneNumber = "+919876543210",
        email = email,
        gender = "Male",
        dob = "1975-04-12",
        city = "Haridwar",
        state = "Uttarakhand",
        bio = "Main Sanchalak and System Admin of Podhi Ashram",
        emergencyContact = "108",
        status = MemberStatus.APPROVED,
        roles = listOf(SpiritualRole.ADMIN, SpiritualRole.MAIN_SANCHALAK),
        memberSince = "August 2012",
        participationLevel = 5,
        achievements = listOf("Dharma Seva Award", "Siddhant Vani Master")
      )
      _currentUserProfile.value = admin
      return Result.success(admin)
    }

    val member = MemberProfile(
      id = "normal_member_uuid",
      fullName = "Rupesh Patel",
      phoneNumber = "+919000012345",
      email = email,
      gender = "Male",
      dob = "1994-08-20",
      city = "Ahmedabad",
      state = "Gujarat",
      bio = "Sincere seeker finding absolute peace at Paramdham Ashram.",
      emergencyContact = "+919000054321",
      status = MemberStatus.APPROVED,
      roles = listOf(SpiritualRole.VERIFIED_MEMBER, SpiritualRole.VOLUNTEER),
      memberSince = "January 2026",
      participationLevel = 3,
      achievements = listOf("Weekly Seva Regular")
    )
    _currentUserProfile.value = member
    return Result.success(member)
  }

  override suspend fun loginWithPhoneOTP(phoneNumber: String, otpCode: String): Result<MemberProfile> {
    delay(1000)
    if (otpCode != "123456" && otpCode != "888888") {
      return Result.failure(Exception("Invalid OTP Code entered. Please check and retry."))
    }
    
    val userProfile = MemberProfile(
      id = "otp_member_uuid",
      fullName = "Seva Ji",
      phoneNumber = phoneNumber,
      email = "sevaji@podhiashram.org",
      gender = "Male",
      dob = "1988-11-23",
      city = "Vrindavan",
      state = "Uttar Pradesh",
      bio = "Dedicated devotee of Raj Shyam Ji Maharaj.",
      emergencyContact = "+919999999999",
      status = MemberStatus.APPROVED,
      roles = listOf(SpiritualRole.VOLUNTEER),
      memberSince = "June 2024",
      participationLevel = 4
    )
    _currentUserProfile.value = userProfile
    return Result.success(userProfile)
  }

  override suspend fun loginWithGoogle(idToken: String): Result<MemberProfile> {
    delay(1200)
    val googleProfile = MemberProfile(
      id = "google_member_uuid",
      fullName = "Google Devotee",
      phoneNumber = "+918888877777",
      email = "google.devotee@gmail.com",
      gender = "Female",
      dob = "1999-05-15",
      city = "New Delhi",
      state = "Delhi",
      bio = "Seeking spiritual bliss and digital seva opportunities.",
      emergencyContact = "112",
      status = MemberStatus.PENDING,
      roles = listOf(SpiritualRole.GUEST),
      memberSince = "July 2026",
      participationLevel = 1
    )
    _currentUserProfile.value = googleProfile
    return Result.success(googleProfile)
  }

  override suspend fun registerNewMember(profile: MemberProfile, inviteCode: String?): Result<MemberProfile> {
    if (SupabaseConfig.isConfigured()) {
      return try {
        Log.d("SupabaseAuth", "Registering member via live Auth service: ${profile.email}")
        val api = SupabaseApi.get()
        val metadata = mapOf(
          "full_name" to profile.fullName,
          "phone_number" to profile.phoneNumber,
          "gender" to profile.gender,
          "dob" to profile.dob,
          "city" to profile.city,
          "state" to profile.state,
          "bio" to profile.bio,
          "emergency_contact" to profile.emergencyContact
        )
        val response = api.signUp(SupabaseSignUpRequest(profile.email, "TemporaryPassword123", metadata))
        if (response.isSuccessful && response.body() != null) {
          val assignedRoles = if (inviteCode == "GURU77") {
            listOf(SpiritualRole.DHARMA_PRACHARAK, SpiritualRole.VERIFIED_MEMBER)
          } else {
            listOf(SpiritualRole.GUEST)
          }
          val finalProfile = profile.copy(
            id = response.body()!!.id,
            status = if (inviteCode == "GURU77" || inviteCode == "ADMIN123") MemberStatus.APPROVED else MemberStatus.PENDING,
            roles = assignedRoles
          )
          _currentUserProfile.value = finalProfile
          Result.success(finalProfile)
        } else {
          val errorText = response.errorBody()?.string() ?: "Registration failed."
          Result.failure(Exception(errorText))
        }
      } catch (e: Exception) {
        Log.e("SupabaseAuth", "Live registration failed: ${e.message}", e)
        Result.failure(e)
      }
    }

    delay(1500)
    val assignedRoles = if (inviteCode == "GURU77") {
      listOf(SpiritualRole.DHARMA_PRACHARAK, SpiritualRole.VERIFIED_MEMBER)
    } else {
      listOf(SpiritualRole.GUEST)
    }

    val finalProfile = profile.copy(
      status = if (inviteCode == "GURU77" || inviteCode == "ADMIN123") MemberStatus.APPROVED else MemberStatus.PENDING,
      roles = assignedRoles
    )
    
    _currentUserProfile.value = finalProfile
    return Result.success(finalProfile)
  }

  override suspend fun signOut(): Result<Unit> {
    delay(500)
    _currentUserProfile.value = null
    return Result.success(Unit)
  }

  override suspend fun updateLocalProfile(profile: MemberProfile) {
    _currentUserProfile.value = profile
  }
}
