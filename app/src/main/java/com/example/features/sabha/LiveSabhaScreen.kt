package com.example.features.sabha

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants.AppLanguage
import com.example.ui.components.DivineGlowEffect
import com.example.ui.components.GlassmorphicCard

// Architecture model representing WebRTC Peer Connection State
data class WebRTCConnectionState(
  val isConnected: Boolean = false,
  val activeRoomId: String? = null,
  val isAudioMuted: Boolean = false,
  val isVideoMuted: Boolean = false,
  val latencyMs: Long = 0,
  val participantsCount: Int = 0
)

data class RecordedSatsang(
  val id: String,
  val title: String,
  val speaker: String,
  val date: String,
  val duration: String
)

@Composable
fun LiveSabhaScreen(
  language: AppLanguage
) {
  val context = LocalContext.current
  
  // Simulated WebRTC configuration state
  var rtcState by remember { mutableStateOf(WebRTCConnectionState()) }
  var showMeetSimulation by remember { mutableStateOf(false) }

  val recordings = listOf(
    RecordedSatsang("1", "Deeper Meanings of Siddhant Vani", "Mahadev Pranami", "08 July 2026", "45:12"),
    RecordedSatsang("2", "Special Guru Aarti & Prayer Session", "Mahadev Pranami", "05 July 2026", "32:00"),
    RecordedSatsang("3", "Devotional Chanting & Meditation Guide", "Mahadev Pranami", "01 July 2026", "58:40")
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("live_sabha_content")
  ) {
    Text(
      text = "Virtual Live Sabha Portal",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // Active Live Sabha Broadcast Card with Divine Glowing aura
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      contentAlignment = Alignment.Center
    ) {
      DivineGlowEffect(glowColor = MaterialTheme.colorScheme.primary)

      GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Status badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primary)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              "LIVE DISCOURSE NOW",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Daily Evening Sunderkand Path",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Serif
          )

          Text(
            text = "Guided by: Mahadev Pranami",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          // WebRTC Architecture Call Button
          Button(
            onClick = {
              rtcState = WebRTCConnectionState(
                isConnected = true,
                activeRoomId = "paramdham_sabha_8899",
                participantsCount = 45
              )
              showMeetSimulation = true

              // Open official jitsi link or simulated stream
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.jit.si/ParamdhamAshramSabha"))
              context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Podcasts, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Enter Virtual Sabha", fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Pre-arranged high-end Jitsi connection setup.",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
          )
        }
      }
    }

    // WebRTC connection status indicator block (Architecture display)
    if (rtcState.isConnected) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            "WebRTC Room Active Status:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Room ID: ${rtcState.activeRoomId}", fontSize = 11.sp)
            Text("Active Listeners: ${rtcState.participantsCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    // Recordings section
    Text(
      "Previous Recorded Satsangs",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(bottom = 10.dp)
    )

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(recordings) { rec ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
          Row(
            modifier = Modifier
              .padding(14.dp)
              .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                rec.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                "${rec.speaker} • ${rec.date}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
              )
            }

            Text(
              rec.duration,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}
