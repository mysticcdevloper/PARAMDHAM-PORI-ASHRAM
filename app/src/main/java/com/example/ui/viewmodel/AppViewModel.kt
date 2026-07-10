package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.constants.AppConstants
import com.example.database.AppDatabase
import com.example.database.SadhanaLog
import com.example.database.SadhanaRepository
import com.example.ui.theme.AshramThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {

  // Database and Repository init
  private val sadhanaLogDao = AppDatabase.getDatabase(application).sadhanaLogDao()
  private val sadhanaRepository = SadhanaRepository(sadhanaLogDao)

  // Exposed Live Sadhana logs Flow
  val sadhanaHistory: StateFlow<List<SadhanaLog>> = sadhanaRepository.allLogs
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

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

  init {
    loadPreferences()
    loadTodaySadhana()
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

  // Sadhana Counter Control
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
        id = currentLog?.id ?: 0, // Update if exists, otherwise generate new primary key
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
