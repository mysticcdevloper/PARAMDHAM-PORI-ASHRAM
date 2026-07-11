package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.constants.AppConstants
import com.example.core.models.*
import com.example.core.repositories.*
import com.example.database.AppDatabase
import com.example.database.SadhanaLog
import com.example.database.SadhanaRepository
import com.example.ui.theme.AshramThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {

  // Database and Repository init
  private val sadhanaLogDao = AppDatabase.getDatabase(application).sadhanaLogDao()
  private val sadhanaRepository = SadhanaRepository(sadhanaLogDao)

  // Module 2 Repositories
  private val authRepository: SupabaseAuthRepository = SupabaseAuthRepositoryImpl()
  private val dbRepository: SupabaseDbRepository = SupabaseDbRepositoryImpl()

  // Module 3 Chat Repository
  val chatRepository: ChatRepository = ChatRepositoryImpl()

  val chatConversations: StateFlow<List<ChatConversation>> = chatRepository.conversations
  val activeStatuses: StateFlow<List<MemberStatusUpdate>> = chatRepository.activeStatuses
  val activeBroadcasts: StateFlow<List<BroadcastCampaign>> = chatRepository.activeBroadcasts
  val downloadQueue: StateFlow<List<ChatDownloadTask>> = chatRepository.downloadQueue
  val favoritedMessages: StateFlow<List<ChatMessage>> = chatRepository.favoritedMessages

  private val _chatWallpaper = MutableStateFlow(ChatWallpaper.DEFAULT_SLATE)
  val chatWallpaper: StateFlow<ChatWallpaper> = _chatWallpaper.asStateFlow()

  fun updateChatWallpaper(wallpaper: ChatWallpaper) {
    _chatWallpaper.value = wallpaper
    showToast("Chat wallpaper set to: ${wallpaper.name.replace("_", " ")}")
  }

  // Exposed Live Sadhana logs Flow
  val sadhanaHistory: StateFlow<List<SadhanaLog>> = sadhanaRepository.allLogs
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Auth & Profile Flow
  val currentUserProfile: StateFlow<MemberProfile?> = authRepository.currentUserProfile
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = null
    )

  // System Flows for Member Management
  private val _allMembers = MutableStateFlow<List<MemberProfile>>(emptyList())
  val allMembers: StateFlow<List<MemberProfile>> = _allMembers.asStateFlow()

  private val _inviteCodes = MutableStateFlow<List<InviteCode>>(emptyList())
  val inviteCodes: StateFlow<List<InviteCode>> = _inviteCodes.asStateFlow()

  private val _notifications = MutableStateFlow<List<RealtimeNotification>>(emptyList())
  val notifications: StateFlow<List<RealtimeNotification>> = _notifications.asStateFlow()

  private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
  val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs.asStateFlow()

  // App Settings States
  var themeMode by mutableStateOf(AshramThemeMode.LIGHT)
    private set

  var appLanguage by mutableStateOf(AppConstants.AppLanguage.ENGLISH)
    private set

  var isOnboardingCompleted by mutableStateOf(false)
    private set

  // In-memory active Sadhana tracking (for the daily Sadhana logger)
  private val _japaCount = MutableStateFlow(0)
  val japaCount: StateFlow<Int> = _japaCount.asStateFlow()

  private val _meditationMinutes = MutableStateFlow(10)
  val meditationMinutes: StateFlow<Int> = _meditationMinutes.asStateFlow()

  private val _sadhanaNotes = MutableStateFlow("")
  val sadhanaNotes: StateFlow<String> = _sadhanaNotes.asStateFlow()

  // App status text or Toast message queue
  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  // Device list and Privacy configs
  private val _devices = MutableStateFlow<List<DeviceSession>>(emptyList())
  val devices: StateFlow<List<DeviceSession>> = _devices.asStateFlow()

  private val _privacySettings = MutableStateFlow(PrivacySettings())
  val privacySettings: StateFlow<PrivacySettings> = _privacySettings.asStateFlow()

  var appLockPin by mutableStateOf<String?>(null)
    private set

  var biometricsEnabled by mutableStateOf(false)
    private set

  init {
    loadPreferences()
    loadTodaySadhana()
    fetchSystemData()
  }

  fun fetchSystemData() {
    viewModelScope.launch {
      dbRepository.fetchAllMembers().onSuccess { _allMembers.value = it }
      dbRepository.fetchInviteCodes().onSuccess { _inviteCodes.value = it }
      dbRepository.fetchActivityLogs().onSuccess { _activityLogs.value = it }
      currentUserProfile.value?.let { profile ->
        dbRepository.fetchNotifications(profile.id).onSuccess { _notifications.value = it }
      }
    }
    // Set default devices
    _devices.value = listOf(
      DeviceSession("dev_1", "Pixel 8 Pro (Active)", "Android 14", System.currentTimeMillis()),
      DeviceSession("dev_2", "Samsung Galaxy Tab S9", "Android 13", System.currentTimeMillis() - 3600000 * 24)
    )
  }

  private fun loadPreferences() {
    val prefs = getApplication<Application>().getSharedPreferences("ashram_prefs", Application.MODE_PRIVATE)

    // Theme
    val themeStr = prefs.getString("theme_mode", AshramThemeMode.LIGHT.name) ?: AshramThemeMode.LIGHT.name
    themeMode = try { AshramThemeMode.valueOf(themeStr) } catch (e: Exception) { AshramThemeMode.LIGHT }

    // Language
    val langStr = prefs.getString("app_language", AppConstants.AppLanguage.ENGLISH.name) ?: AppConstants.AppLanguage.ENGLISH.name
    appLanguage = try { AppConstants.AppLanguage.valueOf(langStr) } catch (e: Exception) { AppConstants.AppLanguage.ENGLISH }

    // Onboarding
    isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)

    // App Lock
    appLockPin = prefs.getString("app_lock_pin", null)
    biometricsEnabled = prefs.getBoolean("biometrics_enabled", false)

    // Privacy
    _privacySettings.value = PrivacySettings(
      showPhoneNumber = prefs.getBoolean("privacy_show_phone", false),
      showProfilePhoto = prefs.getBoolean("privacy_show_photo", true),
      showLastSeen = prefs.getBoolean("privacy_show_seen", true),
      showStatus = prefs.getBoolean("privacy_show_status", true),
      showEmail = prefs.getBoolean("privacy_show_email", false)
    )
  }

  fun updateTheme(mode: AshramThemeMode) {
    themeMode = mode
    val prefs = getApplication<Application>().getSharedPreferences("ashram_prefs", Application.MODE_PRIVATE)
    prefs.edit().putString("theme_mode", mode.name).apply()
  }

  fun updateLanguage(lang: AppConstants.AppLanguage) {
    appLanguage = lang
    val prefs = getApplication<Application>().getSharedPreferences("ashram_prefs", Application.MODE_PRIVATE)
    prefs.edit().putString("app_language", lang.name).apply()
  }

  fun completeOnboarding() {
    isOnboardingCompleted = true
    val prefs = getApplication<Application>().getSharedPreferences("ashram_prefs", Application.MODE_PRIVATE)
    prefs.edit().putBoolean("onboarding_completed", true).apply()
  }

  fun resetOnboarding() {
    isOnboardingCompleted = false
    val prefs = getApplication<Application>().getSharedPreferences("ashram_prefs", Application.MODE_PRIVATE)
    prefs.edit().putBoolean("onboarding_completed", false).apply()
  }

  // --- Auth operations ---
  fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      val res = authRepository.loginWithEmail(email, password)
      if (res.isSuccess) {
        val profile = res.getOrNull()
        showToast("Welcome Back, ${profile?.fullName}!")
        fetchSystemData()
        onResult(true, null)
      } else {
        onResult(false, res.exceptionOrNull()?.message)
      }
    }
  }

  fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      val res = authRepository.resetPassword(email)
      if (res.isSuccess) {
        showToast("Password reset email sent to $email!")
        onResult(true, null)
      } else {
        onResult(false, res.exceptionOrNull()?.message)
      }
    }
  }

  fun loginWithPhoneOTP(phoneNumber: String, otp: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      val res = authRepository.loginWithPhoneOTP(phoneNumber, otp)
      if (res.isSuccess) {
        val profile = res.getOrNull()
        showToast("Logged in successfully via secure OTP!")
        fetchSystemData()
        onResult(true, null)
      } else {
        onResult(false, res.exceptionOrNull()?.message)
      }
    }
  }

  fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      val res = authRepository.loginWithGoogle(idToken)
      if (res.isSuccess) {
        showToast("Google connection established.")
        fetchSystemData()
        onResult(true, null)
      } else {
        onResult(false, res.exceptionOrNull()?.message)
      }
    }
  }

  fun registerNewMember(profile: MemberProfile, inviteCode: String?, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      val res = authRepository.registerNewMember(profile, inviteCode)
      if (res.isSuccess) {
        val finalProfile = res.getOrNull()!!
        showToast("Registration Complete!")
        fetchSystemData()
        onResult(true, null)
      } else {
        onResult(false, res.exceptionOrNull()?.message)
      }
    }
  }

  fun signOut(onComplete: () -> Unit) {
    viewModelScope.launch {
      authRepository.signOut()
      showToast("Signed out successfully.")
      onComplete()
    }
  }

  // --- Admin operations ---
  fun approveRejectMember(profileId: String, status: MemberStatus, reason: String? = null) {
    viewModelScope.launch {
      val reviewerId = currentUserProfile.value?.id ?: "admin"
      dbRepository.updateMemberStatus(profileId, status, reviewerId, reason)
      fetchSystemData()
      showToast("Member status updated to ${status.name}")
    }
  }

  fun assignMemberRoles(profileId: String, roles: List<SpiritualRole>) {
    viewModelScope.launch {
      dbRepository.updateMemberRoles(profileId, roles)
      fetchSystemData()
      showToast("Spiritual badges assigned successfully!")
    }
  }

  fun generateInviteCode(code: String, expiryDays: Int, maxUses: Int, role: SpiritualRole?) {
    viewModelScope.launch {
      val createdBy = currentUserProfile.value?.id ?: "admin"
      val expiry = System.currentTimeMillis() + (expiryDays * 86400000L)
      dbRepository.generateInviteCode(code, createdBy, expiry, maxUses, role)
      fetchSystemData()
      showToast("Generated Invite Code: ${code.uppercase()}")
    }
  }

  fun verifyInvite(code: String, onResult: (Result<InviteCode>) -> Unit) {
    viewModelScope.launch {
      val res = dbRepository.verifyInviteCode(code)
      onResult(res)
    }
  }

  // --- Security & Privacy settings ---
  fun updatePrivacySettings(
    showPhone: Boolean,
    showPhoto: Boolean,
    showSeen: Boolean,
    showStatus: Boolean,
    showEmail: Boolean
  ) {
    _privacySettings.value = PrivacySettings(showPhone, showPhoto, showSeen, showStatus, showEmail)
    val prefs = getApplication<Application>().getSharedPreferences("ashram_prefs", Application.MODE_PRIVATE)
    prefs.edit()
      .putBoolean("privacy_show_phone", showPhone)
      .putBoolean("privacy_show_photo", showPhoto)
      .putBoolean("privacy_show_seen", showSeen)
      .putBoolean("privacy_show_status", showStatus)
      .putBoolean("privacy_show_email", showEmail)
      .apply()
    showToast("Privacy preferences saved successfully.")
  }

  fun setAppLockPinCode(pin: String?) {
    appLockPin = pin
    val prefs = getApplication<Application>().getSharedPreferences("ashram_prefs", Application.MODE_PRIVATE)
    prefs.edit().putString("app_lock_pin", pin).apply()
    showToast(if (pin == null) "App lock disabled" else "App lock PIN enabled")
  }

  fun toggleBiometrics(enabled: Boolean) {
    biometricsEnabled = enabled
    val prefs = getApplication<Application>().getSharedPreferences("ashram_prefs", Application.MODE_PRIVATE)
    prefs.edit().putBoolean("biometrics_enabled", enabled).apply()
    showToast(if (enabled) "Biometric authentication activated" else "Biometrics deactivated")
  }

  fun logoutAllDevices() {
    _devices.value = listOf(
      DeviceSession("dev_1", "Pixel 8 Pro (Active)", "Android 14", System.currentTimeMillis())
    )
    showToast("Logged out of all secondary devices.")
  }

  // --- Sadhana Counter Control ---
  fun incrementJapa() {
    _japaCount.value += 1
  }

  fun decrementJapa() {
    if (_japaCount.value > 0) _japaCount.value -= 1
  }

  fun resetJapa() {
    _japaCount.value = 0
  }

  fun updateMeditation(mins: Int) {
    if (mins >= 0) {
      _meditationMinutes.value = mins
    }
  }

  fun updateNotes(notes: String) {
    _sadhanaNotes.value = notes
  }

  private fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
  }

  private fun loadTodaySadhana() {
    viewModelScope.launch {
      val today = getTodayDateString()
      val todayLog = sadhanaRepository.getLogByDate(today)
      if (todayLog != null) {
        _japaCount.value = todayLog.japaCount
        _meditationMinutes.value = todayLog.meditationMinutes
        _sadhanaNotes.value = todayLog.notes
      }
    }
  }

  fun saveTodaySadhana() {
    viewModelScope.launch {
      val today = getTodayDateString()
      val currentLog = sadhanaRepository.getLogByDate(today)
      val log = SadhanaLog(
        id = currentLog?.id ?: 0,
        dateString = today,
        japaCount = _japaCount.value,
        meditationMinutes = _meditationMinutes.value,
        notes = _sadhanaNotes.value
      )
      sadhanaRepository.insertLog(log)
      showToast(AppConstants.getTranslation(appLanguage, "sadhana_saved"))
    }
  }

  fun clearSadhanaHistory() {
    viewModelScope.launch {
      sadhanaRepository.clearLogs()
      _japaCount.value = 0
      _meditationMinutes.value = 10
      _sadhanaNotes.value = ""
    }
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
  }

  fun clearToast() {
    _toastMessage.value = null
  }
}
