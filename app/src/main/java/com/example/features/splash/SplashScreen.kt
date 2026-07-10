package com.example.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.ui.components.FloatingParticlesBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  isOnboardingCompleted: Boolean,
  onNavigateToOnboarding: () -> Unit,
  onNavigateToMain: () -> Unit
) {
  var startAnimation by remember { mutableStateOf(false) }
  
  val alphaAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = tween(1500, easing = FastOutSlowInEasing),
    label = "alpha"
  )

  val scaleAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1.05f else 0.9f,
    animationSpec = tween(2000, easing = LinearOutSlowInEasing),
    label = "scale"
  )

  // Lotus drawing pulse animation
  val infiniteTransition = rememberInfiniteTransition(label = "lotus_pulse")
  val pulseFactor by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(2500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  LaunchedEffect(key1 = true) {
    startAnimation = true
    delay(2800) // Beautiful peaceful delay
    if (isOnboardingCompleted) {
      onNavigateToMain()
    } else {
      onNavigateToOnboarding()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
          )
        )
      )
      .testTag("splash_screen_root"),
    contentAlignment = Alignment.Center
  ) {
    // Elegant floating celestial particles
    FloatingParticlesBackground(particleColor = MaterialTheme.colorScheme.primary)

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .padding(24.dp)
        .alpha(alphaAnim)
    ) {
      // 1. Temple and Lotus Symbolic Golden Line-Art Component
      Box(
        modifier = Modifier
          .size(160.dp * scaleAnim)
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val accentColor = MaterialTheme.colorScheme.tertiary

        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val center = Offset(w / 2, h / 2)

          // Draw Golden Aura Halo
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(accentColor.copy(alpha = 0.28f), Color.Transparent),
              center = center,
              radius = w * 0.48f * pulseFactor
            ),
            radius = w * 0.48f * pulseFactor,
            center = center
          )

          // Draw Temple Dome Outline
          val domePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.3f, h * 0.7f)
            quadraticTo(w * 0.3f, h * 0.35f, w * 0.5f, h * 0.3f)
            quadraticTo(w * 0.7f, h * 0.35f, w * 0.7f, h * 0.7f)
          }
          drawPath(
            path = domePath,
            color = primaryColor.copy(alpha = 0.4f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
          )

          // Draw Spire of the Temple (Kalas)
          drawLine(
            color = primaryColor,
            start = Offset(w * 0.5f, h * 0.3f),
            end = Offset(w * 0.5f, h * 0.18f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
          )
          drawCircle(
            color = accentColor,
            radius = 6.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.16f)
          )

          // Draw Stylized Sacred Lotus Petals at the Base
          val petalPath = androidx.compose.ui.graphics.Path().apply {
            // Central Petal
            moveTo(w * 0.5f, h * 0.55f)
            cubicTo(w * 0.42f, h * 0.45f, w * 0.42f, h * 0.7f, w * 0.5f, h * 0.8f)
            moveTo(w * 0.5f, h * 0.55f)
            cubicTo(w * 0.58f, h * 0.45f, w * 0.58f, h * 0.7f, w * 0.5f, h * 0.8f)

            // Left Petal
            moveTo(w * 0.47f, h * 0.60f)
            cubicTo(w * 0.28f, h * 0.52f, w * 0.32f, h * 0.75f, w * 0.48f, h * 0.82f)

            // Right Petal
            moveTo(w * 0.53f, h * 0.60f)
            cubicTo(w * 0.72f, h * 0.52f, w * 0.68f, h * 0.75f, w * 0.52f, h * 0.82f)
          }
          drawPath(
            path = petalPath,
            color = primaryColor,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 2. Beautiful Temple App Name in Divine Serif Style
      Text(
        text = "PARAMDHAM",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 5.sp,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = FontFamily.Serif
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "PODHI ASHRAM",
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 3.sp,
        color = MaterialTheme.colorScheme.secondary,
        fontFamily = FontFamily.Serif
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 3. Subtle sub-text/slogan
      Text(
        text = "Divine Communion • Peaceful Devotion",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        fontWeight = FontWeight.Light,
        letterSpacing = 1.sp
      )
    }
  }
}
