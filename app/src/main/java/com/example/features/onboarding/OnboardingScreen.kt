package com.example.features.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.core.constants.AppConstants.AppLanguage
import com.example.ui.components.FloatingParticlesBackground
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RajShyamaLogo
import kotlinx.coroutines.launch

data class OnboardingPageData(
  val titleKey: String,
  val descKey: String,
  val iconType: OnboardingIconType
)

enum class OnboardingIconType {
  COMMUNITY, CHAT, SABHA, LIBRARY, PRAYER
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
  language: AppLanguage,
  onFinished: () -> Unit
) {
  val pages = listOf(
    OnboardingPageData(
      titleKey = "Holy Community",
      descKey = "Join the global spiritual community of Paramdham Podhi Ashram. Align with fellow seekers and share pure divine frequencies.",
      iconType = OnboardingIconType.COMMUNITY
    ),
    OnboardingPageData(
      titleKey = "Chats",
      descKey = "Communicate securely with your spiritual groups, coordinators, and families in a dedicated, distraction-free environment.",
      iconType = OnboardingIconType.CHAT
    ),
    OnboardingPageData(
      titleKey = "Live Sabha",
      descKey = "Participate in virtual audio sabhas, view spiritual discourses, and sing holy bhajans guided by Maharaj Ji's guidance.",
      iconType = OnboardingIconType.SABHA
    ),
    OnboardingPageData(
      titleKey = "Holy Library",
      descKey = "Unlock instant access to sacred texts, including Siddhant Vani and Tartam Sagar. Search, read, and bookmark with peace.",
      iconType = OnboardingIconType.LIBRARY
    ),
    OnboardingPageData(
      titleKey = "Prayer & Sadhana",
      descKey = "Track your daily Japa mala counts, log meditation sessions, and maintain a quiet spiritual routine in our dedicated tracker.",
      iconType = OnboardingIconType.PRAYER
    )
  )

  val pagerState = rememberPagerState(pageCount = { pages.size })
  val scope = rememberCoroutineScope()

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("onboarding_root"),
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          RajShyamaLogo(size = 36.dp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "PARAMDHAM",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Serif
          )
        }

        if (pagerState.currentPage < pages.size - 1) {
          Text(
            text = AppConstants.getTranslation(language, "onboarding_skip"),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier
              .clickable { onFinished() }
              .testTag("onboarding_skip_btn")
              .padding(8.dp)
          )
        }
      }
    },
    bottomBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Page Indicators
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          repeat(pages.size) { index ->
            val isSelected = pagerState.currentPage == index
            Box(
              modifier = Modifier
                .height(8.dp)
                .width(if (isSelected) 24.dp else 8.dp)
                .clip(CircleShape)
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary 
                  else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
            )
          }
        }

        // Action Button (Next / Get Started)
        if (pagerState.currentPage == pages.size - 1) {
          Button(
            onClick = { onFinished() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .testTag("onboarding_get_started_btn")
              .height(52.dp)
          ) {
            Text(
              text = AppConstants.getTranslation(language, "onboarding_start"),
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onPrimary
            )
          }
        } else {
          FilledIconButton(
            onClick = {
              scope.launch {
                val nextPage = pagerState.currentPage + 1
                if (nextPage < pages.size) {
                  pagerState.animateScrollToPage(nextPage)
                }
              }
            },
            colors = IconButtonDefaults.filledIconButtonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
              .size(52.dp)
              .testTag("onboarding_next_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Next Page",
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.background,
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
          )
        )
    ) {
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
      ) { pageIndex ->
        val page = pages[pageIndex]
        OnboardingPageView(page = page, language = language)
      }
    }
  }
}

@Composable
fun OnboardingPageView(page: OnboardingPageData, language: AppLanguage) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxSize()
  ) {
    // Beautiful Vector Illustration container
    GlassmorphicCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
      borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp),
        contentAlignment = Alignment.Center
      ) {
        val prim = MaterialTheme.colorScheme.primary
        val tert = MaterialTheme.colorScheme.tertiary
        val sec = MaterialTheme.colorScheme.secondary

        // Custom canvas illustrations for each spiritual theme
        Canvas(modifier = Modifier.fillMaxSize()) {
          val cx = size.width / 2
          val cy = size.height / 2
          val radius = (size.width.coerceAtMost(size.height) * 0.35f).coerceAtLeast(1f)

          // Inner Divine Background Halo
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(tert.copy(alpha = 0.35f), Color.Transparent),
              center = Offset(cx, cy),
              radius = (radius * 1.2f).coerceAtLeast(1f)
            ),
            radius = radius * 1.2f,
            center = Offset(cx, cy)
          )

          when (page.iconType) {
            OnboardingIconType.COMMUNITY -> {
              // Draw Community Network of Lotus Hearts
              drawCircle(color = prim, radius = 10.dp.toPx(), center = Offset(cx, cy))
              drawCircle(color = sec, radius = 7.dp.toPx(), center = Offset(cx - 50.dp.toPx(), cy - 40.dp.toPx()))
              drawCircle(color = sec, radius = 7.dp.toPx(), center = Offset(cx + 50.dp.toPx(), cy - 40.dp.toPx()))
              drawCircle(color = sec, radius = 7.dp.toPx(), center = Offset(cx - 40.dp.toPx(), cy + 50.dp.toPx()))
              drawCircle(color = sec, radius = 7.dp.toPx(), center = Offset(cx + 40.dp.toPx(), cy + 50.dp.toPx()))

              // Draw Connecting Golden Waves
              drawLine(color = tert.copy(alpha = 0.6f), start = Offset(cx, cy), end = Offset(cx - 50.dp.toPx(), cy - 40.dp.toPx()), strokeWidth = 2.dp.toPx())
              drawLine(color = tert.copy(alpha = 0.6f), start = Offset(cx, cy), end = Offset(cx + 50.dp.toPx(), cy - 40.dp.toPx()), strokeWidth = 2.dp.toPx())
              drawLine(color = tert.copy(alpha = 0.6f), start = Offset(cx, cy), end = Offset(cx - 40.dp.toPx(), cy + 50.dp.toPx()), strokeWidth = 2.dp.toPx())
              drawLine(color = tert.copy(alpha = 0.6f), start = Offset(cx, cy), end = Offset(cx + 40.dp.toPx(), cy + 50.dp.toPx()), strokeWidth = 2.dp.toPx())
            }
            OnboardingIconType.CHAT -> {
              // Draw Chanting Speech Bubble with Om / Spiritual vibe
              val bubblePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - 50.dp.toPx(), cy - 30.dp.toPx())
                lineTo(cx + 50.dp.toPx(), cy - 30.dp.toPx())
                quadraticTo(cx + 70.dp.toPx(), cy - 30.dp.toPx(), cx + 70.dp.toPx(), cy)
                quadraticTo(cx + 70.dp.toPx(), cy + 30.dp.toPx(), cx + 50.dp.toPx(), cy + 30.dp.toPx())
                lineTo(cx - 20.dp.toPx(), cy + 30.dp.toPx())
                lineTo(cx - 45.dp.toPx(), cy + 55.dp.toPx())
                lineTo(cx - 40.dp.toPx(), cy + 30.dp.toPx())
                quadraticTo(cx - 70.dp.toPx(), cy + 30.dp.toPx(), cx - 70.dp.toPx(), cy)
                quadraticTo(cx - 70.dp.toPx(), cy - 30.dp.toPx(), cx - 50.dp.toPx(), cy - 30.dp.toPx())
              }
              drawPath(
                path = bubblePath,
                color = prim,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
              )
              // Draw an elegant lotus blossom inside chat bubble
              drawCircle(color = tert, radius = 6.dp.toPx(), center = Offset(cx, cy))
              drawLine(color = tert, start = Offset(cx - 15.dp.toPx(), cy), end = Offset(cx + 15.dp.toPx(), cy), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            }
            OnboardingIconType.SABHA -> {
              // Draw Live Sabha Sound Broadcasting rays with holy bell
              drawCircle(
                color = prim,
                radius = radius * 0.4f,
                style = Stroke(width = 3.dp.toPx())
              )
              drawCircle(
                color = tert,
                radius = radius * 0.65f,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
              )
              // Broadcast Rays
              drawLine(color = tert, start = Offset(cx - 20.dp.toPx(), cy - 20.dp.toPx()), end = Offset(cx - 45.dp.toPx(), cy - 45.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
              drawLine(color = tert, start = Offset(cx + 20.dp.toPx(), cy - 20.dp.toPx()), end = Offset(cx + 45.dp.toPx(), cy - 45.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
              drawLine(color = tert, start = Offset(cx - 20.dp.toPx(), cy + 20.dp.toPx()), end = Offset(cx - 45.dp.toPx(), cy + 45.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
              drawLine(color = tert, start = Offset(cx + 20.dp.toPx(), cy + 20.dp.toPx()), end = Offset(cx + 45.dp.toPx(), cy + 45.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)

              // Inside Bell / Microphone Concept
              drawCircle(color = prim, radius = 8.dp.toPx(), center = Offset(cx, cy))
            }
            OnboardingIconType.LIBRARY -> {
              // Draw open scripture book line-art
              val bookPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, cy + 25.dp.toPx())
                // Left page
                cubicTo(cx - 20.dp.toPx(), cy - 5.dp.toPx(), cx - 50.dp.toPx(), cy - 10.dp.toPx(), cx - 60.dp.toPx(), cy - 20.dp.toPx())
                lineTo(cx - 60.dp.toPx(), cy + 20.dp.toPx())
                cubicTo(cx - 50.dp.toPx(), cy + 30.dp.toPx(), cx - 20.dp.toPx(), cy + 35.dp.toPx(), cx, cy + 55.dp.toPx())

                // Right page
                cubicTo(cx + 20.dp.toPx(), cy + 35.dp.toPx(), cx + 50.dp.toPx(), cy + 30.dp.toPx(), cx + 60.dp.toPx(), cy + 20.dp.toPx())
                lineTo(cx + 60.dp.toPx(), cy - 20.dp.toPx())
                cubicTo(cx + 50.dp.toPx(), cy - 10.dp.toPx(), cx + 20.dp.toPx(), cy - 5.dp.toPx(), cx, cy + 25.dp.toPx())
              }
              drawPath(
                path = bookPath,
                color = prim,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
              )
              // Draw Golden Flame of wisdom rising above
              val flamePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, cy - 5.dp.toPx())
                quadraticTo(cx - 12.dp.toPx(), cy - 25.dp.toPx(), cx, cy - 45.dp.toPx())
                quadraticTo(cx + 12.dp.toPx(), cy - 25.dp.toPx(), cx, cy - 5.dp.toPx())
              }
              drawPath(path = flamePath, color = tert)
            }
            OnboardingIconType.PRAYER -> {
              // Draw Prayer Mala Ring (dots) and Devotional hands
              for (i in 0 until 18) {
                val angle = i * (360f / 18) * (Math.PI / 180f)
                val dotX = cx + (radius * 0.7f * Math.cos(angle)).toFloat()
                val dotY = cy + (radius * 0.7f * Math.sin(angle)).toFloat()
                drawCircle(color = tert, radius = 4.dp.toPx(), center = Offset(dotX, dotY))
              }

              // Center Golden Sun
              drawCircle(color = prim, radius = 12.dp.toPx(), center = Offset(cx, cy))
              drawLine(color = prim, start = Offset(cx, cy - 20.dp.toPx()), end = Offset(cx, cy + 20.dp.toPx()), strokeWidth = 2.dp.toPx())
              drawLine(color = prim, start = Offset(cx - 20.dp.toPx(), cy), end = Offset(cx + 20.dp.toPx(), cy), strokeWidth = 2.dp.toPx())
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Title text
    Text(
      text = page.titleKey,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Description text
    Text(
      text = page.descKey,
      fontSize = 14.sp,
      lineHeight = 22.sp,
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    )
  }
}
