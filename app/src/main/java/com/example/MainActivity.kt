package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.models.MemberStatus
import com.example.features.auth.AuthScreen
import com.example.features.auth.PendingApprovalScreen
import com.example.features.main.MainPortalScreen
import com.example.features.onboarding.OnboardingScreen
import com.example.features.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    var appViewModel: AppViewModel? = null
    var errorMessage: String? = null
    
    try {
      appViewModel = androidx.lifecycle.ViewModelProvider(this, androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application))[AppViewModel::class.java]
    } catch (e: Exception) {
      errorMessage = e.stackTraceToString()
    }
    
    setContent {
      if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Red)) {
          Text(errorMessage!!, color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
      } else {
        appViewModel?.let { appVm ->
          // Observe custom Toast messages reactively
          LaunchedEffect(key1 = true) {
            appVm.toastMessage.collect { msg ->
              if (msg != null) {
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                appVm.clearToast()
              }
            }
          }

          // Dynamically load selected spiritual theme
          MyApplicationTheme(themeMode = appVm.themeMode) {
            val navController = rememberNavController()

            NavHost(
              navController = navController,
              startDestination = "splash",
              modifier = Modifier.fillMaxSize()
            ) {
              // 1. SPLASH SCREEN
              composable("splash") {
                SplashScreen(
                  isOnboardingCompleted = appVm.isOnboardingCompleted,
                  onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                      popUpTo("splash") { inclusive = true }
                    }
                  },
                  onNavigateToMain = {
                    navController.navigate("main") {
                      popUpTo("splash") { inclusive = true }
                    }
                  }
                )
              }

              // 2. 5-SLIDE ONBOARDING SCREEN
              composable("onboarding") {
                OnboardingScreen(
                  language = appVm.appLanguage,
                  onFinished = {
                    appVm.completeOnboarding()
                    navController.navigate("main") {
                      popUpTo("onboarding") { inclusive = true }
                    }
                  }
                )
              }

              // 3. MAIN PORTAL GATE (Includes secure Auth guards)
              composable("main") {
                val profile by appVm.currentUserProfile.collectAsState()

                if (profile == null) {
                  AuthScreen(
                    viewModel = appVm,
                    onAuthSuccess = {
                      // Profile loaded reactively
                    }
                  )
                } else if (profile!!.status != MemberStatus.APPROVED) {
                  PendingApprovalScreen(
                    viewModel = appVm,
                    onLogout = {
                      appVm.signOut {
                        // Cleared to null, AuthScreen will render
                      }
                    }
                  )
                } else {
                  MainPortalScreen(
                    viewModel = appVm,
                    language = appVm.appLanguage,
                    onLogout = {
                      appVm.signOut {
                        // Cleared to null, AuthScreen will render
                      }
                    }
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
