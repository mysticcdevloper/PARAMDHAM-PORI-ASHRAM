package com.example.features.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.core.constants.AppConstants.AppLanguage
import com.example.ui.theme.AshramThemeMode
import com.example.ui.viewmodel.AppViewModel

@Composable
fun MoreScreen(
  viewModel: AppViewModel,
  language: AppLanguage
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  // Notification toggles
  var isDailyQuoteEnabled by remember { mutableStateOf(true) }
  var isSabhaEnabled by remember { mutableStateOf(true) }
  var isLibraryEnabled by remember { mutableStateOf(false) }

  // App settings titles
  val settingsTitle = AppConstants.getTranslation(language, "settings")
  val themeLabel = AppConstants.getTranslation(language, "theme")
  val languageLabel = AppConstants.getTranslation(language, "language")
  val notificationsLabel = AppConstants.getTranslation(language, "notifications")
  val aboutLabel = AppConstants.getTranslation(language, "about_ashram")
  val privacyLabel = AppConstants.getTranslation(language, "privacy_policy")
  val adminLabel = AppConstants.getTranslation(language, "about_admin")
  val contactLabel = AppConstants.getTranslation(language, "contact_us")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("more_screen_content")
  ) {
    Text(
      text = settingsTitle,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // 1. MAIN ADMINISTRATOR CARD (Mahadev Pranami & 9654053044)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
      shape = RoundedCornerShape(24.dp),
      border = androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Admin avatar
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "MP",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = AppConstants.MAIN_ADMIN,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = adminLabel,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Hotline row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = contactLabel,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.secondary,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "+91 ${AppConstants.CONSULTATION_NUMBER}",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
          }

          Row {
            // Copy phone
            IconButton(
              onClick = {
                clipboardManager.setText(AnnotatedString(AppConstants.CONSULTATION_NUMBER))
                viewModel.showToast(AppConstants.getTranslation(language, "copied"))
              }
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy Number", tint = MaterialTheme.colorScheme.primary)
            }

            // Call phone
            IconButton(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                  data = Uri.parse("tel:${AppConstants.CONSULTATION_NUMBER}")
                }
                context.startActivity(intent)
              }
            ) {
              Icon(Icons.Default.Call, contentDescription = "Call Admin", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }
    }

    // 2. THEME SETTINGS (LIGHT, DARK, GOLDEN, HOLY)
    Text(
      text = themeLabel,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val themes = listOf(
        AshramThemeMode.LIGHT to "Saffron",
        AshramThemeMode.DARK to "Cosmic",
        AshramThemeMode.GOLDEN to "Golden",
        AshramThemeMode.HOLY to "Holy"
      )

      themes.forEach { (mode, name) ->
        val isSelected = viewModel.themeMode == mode
        Box(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.surface
            )
            .border(
              1.dp,
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
              RoundedCornerShape(12.dp)
            )
            .clickable { viewModel.updateTheme(mode) },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = name,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }
    }

    // 3. LANGUAGE SETTINGS (English, Hindi, Gujarati)
    Text(
      text = languageLabel,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val languages = AppConstants.AppLanguage.values()
      languages.forEach { lang ->
        val isSelected = viewModel.appLanguage == lang
        Box(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.surface
            )
            .border(
              1.dp,
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
              RoundedCornerShape(12.dp)
            )
            .clickable { viewModel.updateLanguage(lang) },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = when (lang) {
              AppLanguage.ENGLISH -> "English"
              AppLanguage.HINDI -> "हिन्दी"
              AppLanguage.GUJARATI -> "ગુજરાતી"
            },
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    // 4. NOTIFICATION SETTINGS (Switches)
    Text(
      text = notificationsLabel,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        // Switch 1
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Daily Spiritual Thought Alerts", fontSize = 13.sp)
          Switch(
            checked = isDailyQuoteEnabled,
            onCheckedChange = { isDailyQuoteEnabled = it },
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
          )
        }

        // Switch 2
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Live Sabha Broadcast Reminders", fontSize = 13.sp)
          Switch(
            checked = isSabhaEnabled,
            onCheckedChange = { isSabhaEnabled = it },
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
          )
        }
      }
    }

    // 5. ABOUT ASHRAM
    Text(
      text = aboutLabel,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Paramdham Podhi Ashram is a sacred temple and spiritual commune dedicated to peaceful meditation, the holy teachings of Raj Shyam Ji Maharaj, Sakhi Ji, and the continuous study of Siddhant Vani.",
          fontSize = 12.sp,
          lineHeight = 18.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "This application serves as the official portal to unite disciples across the globe in unified devotion.",
          fontSize = 12.sp,
          lineHeight = 18.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
      }
    }

    // 6. PRIVACY POLICY / CODE
    Text(
      text = privacyLabel,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "All communications, chats, and files inside Paramdham Podhi Ashram conform strictly to spiritual confidentiality principles and Row Level Security (RLS) policies implemented on Supabase data engines.",
          fontSize = 12.sp,
          lineHeight = 18.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
      }
    }
  }
}
