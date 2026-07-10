package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun GlassmorphicCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 24.dp,
  borderWidth: Dp = 1.dp,
  borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
  glowColor: Color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
  content: @Composable ColumnScope.() -> Unit
) {
  val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
  
  Box(
    modifier = modifier
      .background(
        brush = Brush.radialGradient(
          colors = listOf(glowColor, Color.Transparent),
          radius = 350f
        )
      )
  ) {
    Card(
      shape = RoundedCornerShape(cornerRadius),
      colors = CardDefaults.cardColors(containerColor = surfaceColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = borderWidth,
          brush = Brush.verticalGradient(
            colors = listOf(
              borderColor,
              MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
          ),
          shape = RoundedCornerShape(cornerRadius)
        )
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        content()
      }
    }
  }
}

// Draw a beautiful glowing golden background or aura behind text/buttons
@Composable
fun DivineGlowEffect(
  modifier: Modifier = Modifier,
  glowColor: Color = Color(0xFFFFD700)
) {
  val infiniteTransition = rememberInfiniteTransition(label = "glow")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          glowColor.copy(alpha = 0.18f * pulseScale),
          glowColor.copy(alpha = 0.04f),
          Color.Transparent
        ),
        center = Offset(canvasWidth / 2, canvasHeight / 2),
        radius = canvasWidth * 0.45f * pulseScale
      ),
      radius = canvasWidth * 0.45f * pulseScale,
      center = Offset(canvasWidth / 2, canvasHeight / 2)
    )
  }
}

// Decorative floating gold particles background canvas
@Composable
fun FloatingParticlesBackground(
  modifier: Modifier = Modifier,
  particleColor: Color = Color(0xFFFFD700)
) {
  val transition = rememberInfiniteTransition(label = "particles")
  val animState by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "float"
  )

  val particles = remember {
    List(15) {
      Particle(
        xPercent = Random.nextFloat(),
        yPercent = Random.nextFloat(),
        size = Random.nextFloat() * 4f + 2f,
        speedFactor = Random.nextFloat() * 0.15f + 0.05f
      )
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    particles.forEach { p ->
      val currentYPercent = (p.yPercent - (animState * p.speedFactor)) % 1f
      val adjustedY = if (currentYPercent < 0) currentYPercent + 1f else currentYPercent
      
      val drawX = p.xPercent * size.width
      val drawY = adjustedY * size.height
      
      drawCircle(
        color = particleColor.copy(alpha = 0.25f),
        radius = p.size,
        center = Offset(drawX, drawY)
      )
    }
  }
}

private data class Particle(
  val xPercent: Float,
  val yPercent: Float,
  val size: Float,
  val speedFactor: Float
)
