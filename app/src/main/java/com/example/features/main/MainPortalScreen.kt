package com.example.features.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.core.constants.AppConstants.AppLanguage
import com.example.features.chat.ChatsScreen
import com.example.features.gallery.GalleryScreen
import com.example.features.home.HomeScreen
import com.example.features.library.LibraryScreen
import com.example.features.more.MoreScreen
import com.example.features.sabha.LiveSabhaScreen
import com.example.ui.viewmodel.AppViewModel

sealed class PortalTab(val route: String, val icon: ImageVector, val labelKey: String) {
  object Home : PortalTab("home", Icons.Default.Home, "home")
  object Chats : PortalTab("chats", Icons.Default.Chat, "chats")
  object Sabha : PortalTab("sabha", Icons.Default.Podcasts, "sabha")
  object Library : PortalTab("library", Icons.Default.MenuBook, "library")
  object Gallery : PortalTab("gallery", Icons.Default.PhotoLibrary, "gallery")
  object More : PortalTab("more", Icons.Default.Settings, "more")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPortalScreen(
  viewModel: AppViewModel,
  language: AppLanguage,
  onLogout: () -> Unit
) {
  var activeTab by remember { mutableStateOf<PortalTab>(PortalTab.Home) }

  val tabs = listOf(
    PortalTab.Home,
    PortalTab.Chats,
    PortalTab.Sabha,
    PortalTab.Library,
    PortalTab.Gallery,
    PortalTab.More
  )

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("main_portal_root"),
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        tabs.forEach { tab ->
          val isSelected = activeTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { activeTab = tab },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.labelKey,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
              )
            },
            label = {
              Text(
                text = AppConstants.getTranslation(language, tab.labelKey),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.background,
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
          )
        )
    ) {
      AnimatedContent(
        targetState = activeTab,
        transitionSpec = {
          fadeIn() togetherWith fadeOut()
        },
        label = "portal_tabs_transition"
      ) { currentTab ->
        when (currentTab) {
          PortalTab.Home -> HomeScreen(
            viewModel = viewModel,
            language = language,
            onNavigateToSabha = { activeTab = PortalTab.Sabha },
            onNavigateToGallery = { activeTab = PortalTab.Gallery }
          )
          PortalTab.Chats -> ChatsScreen(language = language)
          PortalTab.Sabha -> LiveSabhaScreen(language = language)
          PortalTab.Library -> LibraryScreen(language = language)
          PortalTab.Gallery -> GalleryScreen(language = language)
          PortalTab.More -> MoreScreen(viewModel = viewModel, language = language)
        }
      }
    }
  }
}
