package com.example.features.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.MemberProfile
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RoleBadge
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  viewModel: AppViewModel,
  onLogout: () -> Unit
) {
  val profile by viewModel.currentUserProfile.collectAsState()
  val privacy by viewModel.privacySettings.collectAsState()
  val devices by viewModel.devices.collectAsState()
  val notifications by viewModel.notifications.collectAsState()

  var selectedTab by remember { mutableStateOf(0) } // 0 = Profile, 1 = Security, 2 = Privacy, 3 = Notifications

  if (profile == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    return
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("profile_screen_root")
  ) {
    // Top visual card showing name & roles
    Card(
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
      border = BorderStroke(1.5.dp, Color(0xFFFFB300).copy(alpha = 0.4f))
    ) {
      Column(
        modifier = Modifier.padding(20.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFB300).copy(alpha = 0.15f))
            .border(2.dp, Color(0xFFFFB300), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (profile!!.gender == "Female") "🌸" else "🙏",
            fontSize = 36.sp
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = profile!!.fullName,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          fontFamily = FontFamily.Serif
        )

        Text(
          text = "${profile!!.city}, ${profile!!.state} • ${profile!!.email}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
          modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          profile!!.roles.forEach { role ->
            RoleBadge(role = role)
          }
        }
      }
    }

    // Scrollable horizontal tab switches
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color.Transparent,
      contentColor = MaterialTheme.colorScheme.primary,
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
      Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
        Text("Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
      }
      Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
        Text("Security", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
      }
      Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
        Text("Privacy", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
      }
      Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
        BadgedBox(
          badge = {
            if (notifications.isNotEmpty()) {
              Badge { Text(notifications.size.toString()) }
            }
          }
        ) {
          Text("Feeds", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }

    // Animated panel loading depending on the tab selected
    AnimatedContent(
      targetState = selectedTab,
      label = "profile_panels"
    ) { tab ->
      when (tab) {
        0 -> DetailsTab(profile = profile!!, viewModel = viewModel, onLogout = onLogout)
        1 -> SecurityTab(viewModel = viewModel, devices = devices)
        2 -> PrivacyTab(viewModel = viewModel, privacy = privacy)
        3 -> NotificationsTab(notifications = notifications)
      }
    }
  }
}

@Composable
fun DetailsTab(profile: MemberProfile, viewModel: AppViewModel, onLogout: () -> Unit) {
  val language = viewModel.appLanguage

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Discipleship Profile Details", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        DetailItem(label = "Spiritual Bio", value = profile.bio)
        DetailItem(label = "Mobile Number", value = profile.phoneNumber)
        DetailItem(label = "Date of Birth", value = profile.dob)
        DetailItem(label = "Gender", value = profile.gender)
        DetailItem(label = "Ashram Member ID", value = profile.ashramMemberId ?: "Not assigned yet")
        DetailItem(label = "Occupation", value = profile.occupation ?: "Spiritual seeker")
        DetailItem(label = "Emergency Contact", value = profile.emergencyContact)
        DetailItem(label = "Registration Status", value = profile.status.name)
      }
    }

    // Theme selector
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "App Theme Customization",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          fontFamily = FontFamily.Serif
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val themes = listOf(
            com.example.ui.theme.AshramThemeMode.LIGHT to "Saffron",
            com.example.ui.theme.AshramThemeMode.DARK to "Cosmic",
            com.example.ui.theme.AshramThemeMode.GOLDEN to "Golden",
            com.example.ui.theme.AshramThemeMode.HOLY to "Holy"
          )

          themes.forEach { (mode, name) ->
            val isSelected = viewModel.themeMode == mode
            Box(
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .clickable { viewModel.updateTheme(mode) },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = name,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    // Language selector
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "App Language Selection",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          fontFamily = FontFamily.Serif
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val languages = com.example.core.constants.AppConstants.AppLanguage.values()
          languages.forEach { lang ->
            val isSelected = viewModel.appLanguage == lang
            Box(
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .clickable { viewModel.updateLanguage(lang) },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = when (lang) {
                  com.example.core.constants.AppConstants.AppLanguage.ENGLISH -> "English"
                  com.example.core.constants.AppConstants.AppLanguage.HINDI -> "हिन्दी"
                  com.example.core.constants.AppConstants.AppLanguage.GUJARATI -> "ગુજરાતી"
                },
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    Button(
      onClick = onLogout,
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out from Portal", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun SecurityTab(viewModel: AppViewModel, devices: List<com.example.core.models.DeviceSession>) {
  var isPinAlertOpen by remember { mutableStateOf(false) }
  var enterPin by remember { mutableStateOf("") }

  GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text("App Lock & Secure Keys", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

      // 1. PIN Lock Configure
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(0.7f)) {
          Text("Enable App Lock PIN", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          Text("Secure opening with a 4-digit code.", fontSize = 11.sp, color = Color.Gray)
        }
        Switch(
          checked = viewModel.appLockPin != null,
          onCheckedChange = { checked ->
            if (checked) {
              isPinAlertOpen = true
            } else {
              viewModel.setAppLockPinCode(null)
            }
          }
        )
      }

      // 2. Biometrics Lock Configure
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(0.7f)) {
          Text("Biometric Authentication", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          Text("Enable quick fingerprint or face unlock.", fontSize = 11.sp, color = Color.Gray)
        }
        Switch(
          checked = viewModel.biometricsEnabled,
          onCheckedChange = { viewModel.toggleBiometrics(it) }
        )
      }

      Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

      // 3. Registered Devices list
      Text("Active Sessions & Devices", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

      devices.forEach { device ->
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(device.deviceName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("OS Version: ${device.osVersion}", fontSize = 10.sp, color = Color.Gray)
          }
        }
      }

      Button(
        onClick = { viewModel.logoutAllDevices() },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
      ) {
        Text("Sign Out from All Other Devices", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }
  }

  if (isPinAlertOpen) {
    AlertDialog(
      onDismissRequest = { isPinAlertOpen = false },
      title = { Text("Configure Seeker App PIN", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Enter 4-digit code to lock the application on launch:", fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
          OutlinedTextField(
            value = enterPin,
            onValueChange = { if (it.length <= 4) enterPin = it },
            label = { Text("PIN Code") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (enterPin.length == 4) {
              viewModel.setAppLockPinCode(enterPin)
              isPinAlertOpen = false
              enterPin = ""
            } else {
              viewModel.showToast("PIN code must be exactly 4-digits.")
            }
          }
        ) {
          Text("Save PIN")
        }
      },
      dismissButton = {
        TextButton(onClick = { isPinAlertOpen = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun PrivacyTab(viewModel: AppViewModel, privacy: com.example.core.models.PrivacySettings) {
  var showPhone by remember { mutableStateOf(privacy.showPhoneNumber) }
  var showPhoto by remember { mutableStateOf(privacy.showProfilePhoto) }
  var showSeen by remember { mutableStateOf(privacy.showLastSeen) }
  var showStatus by remember { mutableStateOf(privacy.showStatus) }
  var showEmail by remember { mutableStateOf(privacy.showEmail) }

  GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text("Directory Privacy Controls", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
      Text("Control which profile information is visible to other verified Ashram members in the public directory list.", fontSize = 11.sp, color = Color.Gray)

      PrivacyToggle(
        label = "Show Mobile Phone Number",
        description = "Allow other disciples to see your contact details.",
        checked = showPhone,
        onCheckedChange = { showPhone = it }
      )

      PrivacyToggle(
        label = "Show Profile Avatar Photo",
        description = "Display your photo inside lists and chats.",
        checked = showPhoto,
        onCheckedChange = { showPhoto = it }
      )

      PrivacyToggle(
        label = "Show Last Seen Timestamps",
        description = "Display when you last visited the ashram portal.",
        checked = showSeen,
        onCheckedChange = { showSeen = it }
      )

      PrivacyToggle(
        label = "Show Online Status Activity",
        description = "Let others know when you are active now.",
        checked = showStatus,
        onCheckedChange = { showStatus = it }
      )

      PrivacyToggle(
        label = "Show Personal Email Address",
        description = "Expose your email address to verified members.",
        checked = showEmail,
        onCheckedChange = { showEmail = it }
      )

      Button(
        onClick = { viewModel.updatePrivacySettings(showPhone, showPhoto, showSeen, showStatus, showEmail) },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
      ) {
        Text("Save Privacy Changes", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun NotificationsTab(notifications: List<com.example.core.models.RealtimeNotification>) {
  GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("System & Approval Feeds", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

      if (notifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
          Text("No system notifications active.", fontSize = 12.sp, color = Color.Gray)
        }
      } else {
        notifications.forEach { item ->
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(
                  if (item.category == "approval") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                  else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (item.category == "approval") "👑" else "🕉️",
                fontSize = 14.sp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text(item.message, fontSize = 11.sp, color = Color.Gray)
            }
          }
          Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        }
      }
    }
  }
}

@Composable
fun DetailItem(label: String, value: String) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 2.dp))
  }
}

@Composable
fun PrivacyToggle(
  label: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(0.75f)) {
      Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Text(description, fontSize = 10.sp, color = Color.Gray)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.weight(0.25f)
    )
  }
}
