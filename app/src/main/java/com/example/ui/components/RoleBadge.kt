package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.SpiritualRole

@Composable
fun RoleBadge(
  role: SpiritualRole,
  modifier: Modifier = Modifier,
  showLabel: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "badge_animation")

  // Golden gradient for borders
  val goldGradient = Brush.sweepGradient(
    colors = listOf(
      Color(0xFFFFD54F), // Light gold
      Color(0xFFFFB300), // Pure gold
      Color(0xFFFF8F00), // Dark gold
      Color(0xFFFFD54F)  // Light gold
    )
  )

  // Dynamic animations based on the role's animationType
  val scaleMultiplier by if (role.animationType == "pulse" || role.animationType == "scale") {
    infiniteTransition.animateFloat(
      initialValue = 0.95f,
      targetValue = 1.05f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "scale"
    )
  } else {
    mutableStateOf(1f)
  }

  val rotationAngle by if (role.animationType == "rotate") {
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 6000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "rotation"
    )
  } else {
    mutableStateOf(0f)
  }

  val shimmerOffset by if (role.animationType == "shimmer" || role.animationType == "glow") {
    infiniteTransition.animateFloat(
      initialValue = -100f,
      targetValue = 200f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 2000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "shimmer"
    )
  } else {
    mutableStateOf(0f)
  }

  val alphaValue by if (role.animationType == "fade") {
    infiniteTransition.animateFloat(
      initialValue = 0.6f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1200, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "alpha"
    )
  } else {
    mutableStateOf(1f)
  }

  // Determine container-level modification based on animation
  val animationModifier = when (role.animationType) {
    "pulse", "scale" -> Modifier.scale(scaleMultiplier)
    "rotate" -> Modifier.rotate(rotationAngle)
    "fade" -> Modifier.graphicsLayer { alpha = alphaValue }
    "glow" -> Modifier.shadow(
      elevation = (scaleMultiplier * 4).dp,
      shape = RoundedCornerShape(12.dp),
      ambientColor = role.badgeColor,
      spotColor = Color(0xFFFFB300)
    )
    else -> Modifier
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .then(animationModifier)
      .clip(RoundedCornerShape(12.dp))
      .background(role.badgeColor.copy(alpha = 0.15f))
      .border(
        BorderStroke(1.5.dp, goldGradient),
        shape = RoundedCornerShape(12.dp)
      )
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = role.icon,
      fontSize = 12.sp,
      modifier = Modifier.padding(end = if (showLabel) 4.dp else 0.dp)
    )
    if (showLabel) {
      Text(
        text = role.name,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = role.borderColor,
        letterSpacing = 0.5.sp
      )
    }
  }
}
