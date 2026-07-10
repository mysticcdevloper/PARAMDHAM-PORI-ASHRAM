package com.example.features.home

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.core.constants.AppConstants.AppLanguage
import com.example.core.constants.AppConstants.HolyVani
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RajShyamaLogo
import com.example.ui.viewmodel.AppViewModel

@Composable
fun HomeScreen(
  viewModel: AppViewModel,
  language: AppLanguage,
  onNavigateToSabha: () -> Unit,
  onNavigateToGallery: () -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  // Chanting play/pause simulation
  var isChantingPlaying by remember { mutableStateOf(false) }
  val audioProgressAnim = rememberInfiniteTransition(label = "audio")
  val pulseProgress by audioProgressAnim.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse"
  )

  // Get localized strings
  val welcomeText = AppConstants.getTranslation(language, "welcome_user")
  val thoughtTitle = AppConstants.getTranslation(language, "divine_thought")
  val vaniTitle = AppConstants.getTranslation(language, "holy_vani")
  val joinSabhaText = AppConstants.getTranslation(language, "quick_join")
  val prayerReminderText = AppConstants.getTranslation(language, "prayer_reminder")
  val upcomingProgramsText = AppConstants.getTranslation(language, "upcoming_programs")
  val announcementsText = AppConstants.getTranslation(language, "announcements")

  // Selected spiritual thought
  val thoughtsList = AppConstants.DIVINE_THOUGHTS[language] ?: listOf("Divine light guides us.")
  val dailyThoughtIndex = remember { (0 until thoughtsList.size).random() }
  val currentThought = thoughtsList[dailyThoughtIndex]

  // Selected holy Vani
  val vanisList = AppConstants.HOLY_VANIS[language] ?: emptyList()
  val holyVani = vanisList.firstOrNull() ?: HolyVani("1", "Vani", "Siddhant", "Union", "Ch 1")

  // Japa State
  val japaValue by viewModel.japaCount.collectAsState()
  val meditationMinutesValue by viewModel.meditationMinutes.collectAsState()
  val notesValue by viewModel.sadhanaNotes.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("home_screen_content")
  ) {
    // 1. Divine App Greeting Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = welcomeText,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          fontFamily = FontFamily.Serif
        )
        Text(
          text = "Paramdham Podhi Ashram Portal",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
          fontWeight = FontWeight.Light
        )
      }

      RajShyamaLogo(size = 44.dp)
    }

    // 2. Festival Banner Area (Static Premium illustration representation)
    GlassmorphicCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .background(
            brush = Brush.horizontalGradient(
              colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
              )
            )
          )
          .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(16.dp)
        ) {
          Text(
            text = "✨ SHRAVAN HOLY FESTIVAL ✨",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Join Special Daily Chanting & Satsang",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Managed by Mahadev Pranami",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // 3. Today's Divine Thought Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      ),
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = thoughtTitle,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Row {
            // Copy button
            IconButton(
              onClick = {
                clipboardManager.setText(AnnotatedString(currentThought))
                viewModel.showToast(AppConstants.getTranslation(language, "copied"))
              },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy Thought",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
              )
            }
            // Share button
            IconButton(
              onClick = {
                val sendIntent: Intent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, currentThought)
                  type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
              },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share Thought",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "“ $currentThought ”",
          fontSize = 15.sp,
          lineHeight = 22.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = FontFamily.Serif,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // 4. Today's Holy Vani & Audio simulation
    GlassmorphicCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = vaniTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = holyVani.chapter,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
          )
        }

        IconButton(
          onClick = { isChantingPlaying = !isChantingPlaying },
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
              if (isChantingPlaying) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
          Icon(
            imageVector = if (isChantingPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "Play Devotional Chants",
            tint = if (isChantingPlaying) Color.White else MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = holyVani.verse,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = holyVani.translation,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )

      if (isChantingPlaying) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            "Devotional Chants",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.3f)
          )
          LinearProgressIndicator(
            progress = { pulseProgress },
            modifier = Modifier
              .weight(0.7f)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
          )
        }
      }
    }

    // 5. Daily Sadhana Tracker Card (Local persistence helper via Room)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = prayerReminderText,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Button(
            onClick = { viewModel.saveTodaySadhana() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Text("Save Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Japa mala tracker
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              "Japa Mala Count",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              "1 Mala = 108 Repetitions",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { viewModel.decrementJapa() },
              modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
            ) {
              Icon(Icons.Default.Remove, contentDescription = "Minus", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }

            Text(
              text = "$japaValue",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 16.dp)
            )

            IconButton(
              onClick = { viewModel.incrementJapa() },
              modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
              Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White, modifier = Modifier.size(16.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meditation Sadhana duration slider
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              "Meditation Sadhana",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              "$meditationMinutesValue Mins",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Slider(
            value = meditationMinutesValue.toFloat(),
            onValueChange = { viewModel.updateMeditation(it.toInt()) },
            valueRange = 0f..120f,
            steps = 24,
            colors = SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary,
              inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Diary notes textfield
        OutlinedTextField(
          value = notesValue,
          onValueChange = { viewModel.updateNotes(it) },
          label = { Text("Devotional Sadhana Notes & Reflections", fontSize = 11.sp) },
          placeholder = { Text("Write your daily realizations...", fontSize = 12.sp) },
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
          ),
          textStyle = TextStyle(fontSize = 13.sp),
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // 6. Quick Join Live Sabha Card
    GlassmorphicCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
      glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(0.7f)) {
          Text(
            text = joinSabhaText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Join active Virtual Satsang Stream with Mahadev Pranami.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
          )
        }

        Button(
          onClick = { onNavigateToSabha() },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(0.3f)
        ) {
          Text(
            "Join",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    // 7. Upcoming Programs Timeline
    Text(
      text = upcomingProgramsText,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    val programs = listOf(
      "Daily Morning Aarti & Discourse" to "06:00 AM - 07:15 AM",
      "Special Sunderkand & Devotional Songs" to "Every Saturday, 05:30 PM",
      "Guru Purnima Grand Festival Sabha" to "July 15, Full Day Program"
    )

    programs.forEach { (title, time) ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          Text(time, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }
      }
    }

    // 8. Wallpapers Preview Slider
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Holy Wallpapers",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = FontFamily.Serif
      )
      Text(
        text = "View All",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.clickable { onNavigateToGallery() }
      )
    }

    // Displays color cards to simulate high quality wallpapers
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.padding(vertical = 4.dp)
    ) {
      val colors = listOf(
        Color(0xFFE65100) to "Divine Golden Dome",
        Color(0xFF1A237E) to "Sacred Saffron Aura",
        Color(0xFFFFB300) to "Maharaj Ji Glow",
        Color(0xFFFF6F00) to "Peaceful lotus"
      )

      items(colors) { (color, title) ->
        Box(
          modifier = Modifier
            .width(130.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.5f), color)
              )
            )
            .clickable { onNavigateToGallery() },
          contentAlignment = Alignment.BottomStart
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                brush = Brush.verticalGradient(
                  colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                )
              )
          )
          Text(
            text = title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(10.dp)
          )
        }
      }
    }

    // 9. Announcements Card
    Text(
      text = announcementsText,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            "New Meditation Hall Opening Soon",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "We are pleased to announce the completion of our peaceful meditation hall at the ashram center. Daily practices scheduled under Mahadev Pranami.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
          lineHeight = 16.sp
        )
      }
    }
  }
}
