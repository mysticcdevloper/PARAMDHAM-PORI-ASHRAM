package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.features.main.MainPortalScreen
import com.example.features.onboarding.OnboardingScreen
import com.example.features.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    setContent {
      // Initialize AppViewModel which holds theme state & handles Room Database
      val appViewModel: AppViewModel = viewModel()
      
      // Observe custom Toast messages reactively
      LaunchedEffect(key1 = true) {
        appViewModel.toastMessage.collect { msg ->
          if (msg != null) {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            appViewModel.clearToast()
          }
        }
      }

      // Dynamically load selected spiritual theme
      MyApplicationTheme(themeMode = appViewModel.themeMode) {
        val navController = rememberNavController()

        NavHost(
          navController = navController,
          startDestination = "splash",
          modifier = Modifier.fillMaxSize()
        ) {
          // 1. SPLASH SCREEN
          composable("splash") {
            SplashScreen(
              isOnboardingCompleted = appViewModel.isOnboardingCompleted,
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
              language = appViewModel.appLanguage,
              onFinished = {
                appViewModel.completeOnboarding()
                navController.navigate("main") {
                  popUpTo("onboarding") { inclusive = true }
                }
              }
            )
          }

          // 3. MAIN PORTAL (Scaffold + Bottom Nav Bar + Sub-screens)
          composable("main") {
            MainPortalScreen(
              viewModel = appViewModel,
              language = appViewModel.appLanguage,
              onLogout = {
                appViewModel.resetOnboarding()
                navController.navigate("onboarding") {
                  popUpTo("main") { inclusive = true }
                }
              }
            )
          }
        }
      }
    }
  }
}
