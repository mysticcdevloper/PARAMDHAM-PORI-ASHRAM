package com.example.features.gallery

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants.AppLanguage
import com.example.ui.components.RajShyamaLogo

data class HolyWallpaper(
  val id: String,
  val title: String,
  val color: Color,
  val desc: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
  language: AppLanguage
) {
  var selectedWallpaper by remember { mutableStateOf<HolyWallpaper?>(null) }
  var isSettingWallpaper by remember { mutableStateOf(false) }
  var isSuccessfullySet by remember { mutableStateOf(false) }

  val wallpapers = listOf(
    HolyWallpaper("1", "Temple Golden Dome", Color(0xFFE65100), "A beautiful dome rising in golden light."),
    HolyWallpaper("2", "Sacred Saffron Aura", Color(0xFFFF6F00), "The tranquil warm saffron radiance of prayer."),
    HolyWallpaper("3", "Royal Divine Chants", Color(0xFF1A237E), "Deep blue evening representing cosmic devotion."),
    HolyWallpaper("4", "Maharaj Ji Glow", Color(0xFFFFB300), "A soft light illustrating inner enlightenment."),
    HolyWallpaper("5", "Temple Lotus Blossom", Color(0xFFE040FB), "Spiritual lotus representing pure consciousness."),
    HolyWallpaper("6", "Marigold Aura", Color(0xFFFF9800), "Golden marigold garlands of temple service.")
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("gallery_screen_content")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Text(
        text = "Holy Wallpapers Gallery",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 6.dp)
      )

      Text(
        text = "Tap on any divine background to preview or set as wallpaper.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 16.dp)
      )

      // Gallery grid
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(wallpapers) { wall ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
              .clip(RoundedCornerShape(20.dp))
              .combinedClickable(
                onClick = { selectedWallpaper = wall }
              ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
          ) {
            Column(modifier = Modifier.fillMaxSize()) {
              // Simulated image color block
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(0.7f)
                  .background(
                    brush = Brush.verticalGradient(
                      colors = listOf(wall.color.copy(alpha = 0.5f), wall.color)
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                RajShyamaLogo(size = 36.dp)
              }

              // Text details
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(0.3f)
                  .padding(10.dp),
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = wall.title,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onBackground,
                  maxLines = 1
                )
                Text(
                  text = "High Quality PNG",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }
    }

    // ----------------------------------------------------
    // FULL-SCREEN WALLPAPER PREVIEW DIALOG
    // ----------------------------------------------------
    if (selectedWallpaper != null) {
      val wall = selectedWallpaper!!

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.95f))
          .clickable { selectedWallpaper = null } // Click outside to close
          .testTag("gallery_preview_overlay"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clickable(enabled = false) { } // Prevent closing when clicking inside content
        ) {
          // Close button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            IconButton(
              onClick = { 
                selectedWallpaper = null 
                isSuccessfullySet = false
              },
              modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Big preview card
          Box(
            modifier = Modifier
              .width(240.dp)
              .height(380.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(
                brush = Brush.verticalGradient(
                  colors = listOf(wall.color.copy(alpha = 0.5f), wall.color)
                )
              )
              .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.padding(16.dp)
            ) {
              RajShyamaLogo(size = 72.dp)
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = wall.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = wall.desc,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
              )
            }
          }

          Spacer(modifier = Modifier.height(28.dp))

          // Action Button
          AnimatedContent(
            targetState = isSuccessfullySet,
            label = "wallpaper_state"
          ) { isSet ->
            if (isSet) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.primary)
                  .padding(horizontal = 20.dp, vertical = 10.dp)
              ) {
                Text(
                  "Wallpaper Applied Successfully! ✨",
                  color = Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            } else {
              Button(
                onClick = {
                  isSettingWallpaper = true
                  // Simulate applying wallpaper
                  isSuccessfullySet = true
                  isSettingWallpaper = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSettingWallpaper,
                modifier = Modifier.height(50.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Wallpaper, contentDescription = null, tint = Color.White)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    if (isSettingWallpaper) "Applying..." else "Set as Device Background",
                    fontWeight = FontWeight.Bold
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
