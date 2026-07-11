package com.example.features.auth

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.MemberProfile
import com.example.core.models.MemberStatus
import com.example.features.profile.PrivacyToggle
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RajShyamaLogo
import com.example.ui.viewmodel.AppViewModel

@Composable
fun PendingApprovalScreen(
  viewModel: AppViewModel,
  onLogout: () -> Unit
) {
  val profile by viewModel.currentUserProfile.collectAsState()
  val privacy by viewModel.privacySettings.collectAsState()

  var isPrivacySettingsOpen by remember { mutableStateOf(false) }

  if (profile == null) return

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 40.dp)
      .testTag("pending_approval_root"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    RajShyamaLogo(size = 80.dp)
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Paramdham Podhi Ashram",
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif
    )

    Spacer(modifier = Modifier.height(24.dp))

    when (profile!!.status) {
      MemberStatus.PENDING -> {
        GlassmorphicCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = Color(0xFFFF9800).copy(alpha = 0.4f)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⏳ Verification Queue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
            
            Text(
              text = "Hari Om Seva Ji, your discipleship application is currently being reviewed by Ashram administrators.",
              fontSize = 13.sp,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
              lineHeight = 20.sp
            )

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("YOUR REGISTERED DETAILS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
              Text("• Name: ${profile!!.fullName}", fontSize = 12.sp)
              Text("• Phone: ${profile!!.phoneNumber}", fontSize = 12.sp)
              Text("• City: ${profile!!.city}, ${profile!!.state}", fontSize = 12.sp)
              Text("• Reg ID: ${profile!!.id.take(8).uppercase()}", fontSize = 12.sp)
            }

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

            Text(
              text = "For instant status approvals, please present your invitation code or contact support at +91 98765 43210.",
              fontSize = 11.sp,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy configurations option
        Button(
          onClick = { isPrivacySettingsOpen = true },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Configure Privacy Settings")
          }
        }
      }
      MemberStatus.REJECTED -> {
        GlassmorphicCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = Color.Red.copy(alpha = 0.4f)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(44.dp))
            Text("Membership Rejected", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            Text(
              text = "Unfortunately, your application could not be verified by our administrative board. Please contact support to correct registered profile details.",
              fontSize = 13.sp,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground
            )
          }
        }
      }
      MemberStatus.SUSPENDED -> {
        GlassmorphicCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = Color.Red.copy(alpha = 0.4f)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(44.dp))
            Text("Discipleship Suspended", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            Text(
              text = "Your access privileges to the active ashram directory and community chats have been suspended due to policy violations. Contact temple office for further reviews.",
              fontSize = 13.sp,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground
            )
          }
        }
      }
      MemberStatus.BLOCKED -> {
        GlassmorphicCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = Color.Red.copy(alpha = 0.4f)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(44.dp))
            Text("Access Blocked", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            Text(
              text = "Your IP and profile have been blocked from access. Please exit immediately.",
              fontSize = 13.sp,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground
            )
          }
        }
      }
      else -> {}
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
      onClick = onLogout,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out / Exit Portal")
      }
    }
  }

  // Privacy Dialog Setup
  if (isPrivacySettingsOpen) {
    var showPhone by remember { mutableStateOf(privacy.showPhoneNumber) }
    var showPhoto by remember { mutableStateOf(privacy.showProfilePhoto) }
    var showSeen by remember { mutableStateOf(privacy.showLastSeen) }
    var showStatus by remember { mutableStateOf(privacy.showStatus) }
    var showEmail by remember { mutableStateOf(privacy.showEmail) }

    AlertDialog(
      onDismissRequest = { isPrivacySettingsOpen = false },
      title = { Text("Pre-configure Privacy Toggles", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Control your visibility values once approved:", fontSize = 12.sp, color = Color.Gray)

          PrivacyToggle(label = "Show Phone Number", description = "Visible to other ashramites.", checked = showPhone, onCheckedChange = { showPhone = it })
          PrivacyToggle(label = "Show Profile Photo", description = "Visible on lists & messages.", checked = showPhoto, onCheckedChange = { showPhoto = it })
          PrivacyToggle(label = "Show Last Seen", description = "Show last portal active timestamps.", checked = showSeen, onCheckedChange = { showSeen = it })
          PrivacyToggle(label = "Show Online Activity", description = "Show online indicator bulb.", checked = showStatus, onCheckedChange = { showStatus = it })
          PrivacyToggle(label = "Show Email Address", description = "Expose email contact details.", checked = showEmail, onCheckedChange = { showEmail = it })
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updatePrivacySettings(showPhone, showPhoto, showSeen, showStatus, showEmail)
            isPrivacySettingsOpen = false
          }
        ) {
          Text("Save Preferences")
        }
      }
    )
  }
}
