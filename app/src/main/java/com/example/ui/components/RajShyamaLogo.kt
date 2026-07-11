package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RajShyamaLogo(
  modifier: Modifier = Modifier,
  size: Dp = 100.dp
) {
  val goldColor = Color(0xFFFFD700)
  val deepGold = Color(0xFFFFA500)
  val skyBlue = Color(0xFF00E5FF)
  val peacockCyan = Color(0xFF00B1D2)
  val peacockGreen = Color(0xFF00E676)
  val crimsonRed = Color(0xFFFF1744)

  val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
  val rotationAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(25000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotate"
  )

  val floatAnim by infiniteTransition.animateFloat(
    initialValue = -3f,
    targetValue = 3f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "float"
  )

  Canvas(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
  ) {
    val w = this.size.width
    val h = this.size.height
    val center = Offset(w / 2f, h / 2f)

    // 1. Draw glowing celestial background (Blue/Indigo space aura)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0xFF0D47A1),
          Color(0xFF1A237E),
          Color(0xFF020215)
        ),
        center = center,
        radius = (w / 2f).coerceAtLeast(1f)
      ),
      radius = (w / 2f).coerceAtLeast(1f)
    )

    // 2. Draw golden background rays/aura rotating gently
    val numRays = 24
    val rayLength = w * 0.45f
    val innerRayRadius = w * 0.40f
    
    for (i in 0 until numRays) {
      val angleDeg = (i * (360f / numRays)) + rotationAnim
      val angleRad = Math.toRadians(angleDeg.toDouble())
      val cos = Math.cos(angleRad).toFloat()
      val sin = Math.sin(angleRad).toFloat()
      
      drawLine(
        color = goldColor.copy(alpha = 0.25f),
        start = Offset(center.x + innerRayRadius * cos, center.y + innerRayRadius * sin),
        end = Offset(center.x + rayLength * cos, center.y + rayLength * sin),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
      )
    }

    // 3. Draw premium circular golden border
    drawCircle(
      color = goldColor,
      radius = (w / 2f) - 3.dp.toPx(),
      style = Stroke(width = 2.dp.toPx())
    )
    drawCircle(
      color = deepGold.copy(alpha = 0.5f),
      radius = (w / 2f) - 6.dp.toPx(),
      style = Stroke(width = 1.dp.toPx())
    )

    // Apply gentle floating translation to the inner sacred figures
    drawContext.canvas.save()
    drawContext.canvas.translate(0f, floatAnim.dp.toPx())

    // ----------------------------------------------------------------
    // LEFT CROWN (Shri Raj Ji)
    // ----------------------------------------------------------------
    val leftCrownCenter = Offset(w * 0.36f, h * 0.44f)
    val leftCrownWidth = w * 0.25f
    val leftCrownHeight = h * 0.26f

    // Peacock feather on Shri Raj Ji's crown
    val featherPath = Path().apply {
      moveTo(leftCrownCenter.x, leftCrownCenter.y - leftCrownHeight * 0.6f)
      quadraticTo(
        leftCrownCenter.x - leftCrownWidth * 0.4f, leftCrownCenter.y - leftCrownHeight * 1.3f,
        leftCrownCenter.x - leftCrownWidth * 0.3f, leftCrownCenter.y - leftCrownHeight * 1.6f
      )
      quadraticTo(
        leftCrownCenter.x + leftCrownWidth * 0.2f, leftCrownCenter.y - leftCrownHeight * 1.2f,
        leftCrownCenter.x, leftCrownCenter.y - leftCrownHeight * 0.6f
      )
    }
    
    // Draw outer green quill of peacock feather
    drawPath(
      path = featherPath,
      color = peacockGreen,
      style = Fill
    )
    
    // Draw inner cyan eye
    val featherEyePath1 = Path().apply {
      moveTo(leftCrownCenter.x - leftCrownWidth * 0.1f, leftCrownCenter.y - leftCrownHeight * 1.0f)
      quadraticTo(
        leftCrownCenter.x - leftCrownWidth * 0.35f, leftCrownCenter.y - leftCrownHeight * 1.25f,
        leftCrownCenter.x - leftCrownWidth * 0.25f, leftCrownCenter.y - leftCrownHeight * 1.45f
      )
      quadraticTo(
        leftCrownCenter.x + leftCrownWidth * 0.05f, leftCrownCenter.y - leftCrownHeight * 1.15f,
        leftCrownCenter.x - leftCrownWidth * 0.1f, leftCrownCenter.y - leftCrownHeight * 1.0f
      )
    }
    drawPath(
      path = featherEyePath1,
      color = peacockCyan,
      style = Fill
    )

    // Draw inner indigo core
    drawCircle(
      color = Color(0xFF1A237E),
      radius = leftCrownWidth * 0.15f,
      center = Offset(leftCrownCenter.x - leftCrownWidth * 0.2f, leftCrownCenter.y - leftCrownHeight * 1.25f)
    )

    // Shri Raj Ji's Crown (Golden Mukut)
    val rajCrownPath = Path().apply {
      // Bottom band
      moveTo(leftCrownCenter.x - leftCrownWidth * 0.5f, leftCrownCenter.y + leftCrownHeight * 0.5f)
      lineTo(leftCrownCenter.x + leftCrownWidth * 0.5f, leftCrownCenter.y + leftCrownHeight * 0.5f)
      
      // Right upward peak
      lineTo(leftCrownCenter.x + leftCrownWidth * 0.4f, leftCrownCenter.y - leftCrownHeight * 0.1f)
      
      // Central high peak
      lineTo(leftCrownCenter.x, leftCrownCenter.y - leftCrownHeight * 0.7f)
      
      // Left upward peak
      lineTo(leftCrownCenter.x - leftCrownWidth * 0.4f, leftCrownCenter.y - leftCrownHeight * 0.1f)
      close()
    }
    drawPath(
      path = rajCrownPath,
      brush = Brush.verticalGradient(
        colors = listOf(goldColor, deepGold)
      )
    )
    drawPath(
      path = rajCrownPath,
      color = Color.Black.copy(alpha = 0.2f),
      style = Stroke(width = 1.dp.toPx())
    )

    // Crown Jewels (Ruby/Emerald dots on Raj Ji's crown)
    drawCircle(
      color = crimsonRed,
      radius = leftCrownWidth * 0.1f,
      center = Offset(leftCrownCenter.x, leftCrownCenter.y - leftCrownHeight * 0.2f)
    )
    drawCircle(
      color = peacockGreen,
      radius = leftCrownWidth * 0.06f,
      center = Offset(leftCrownCenter.x - leftCrownWidth * 0.2f, leftCrownCenter.y + leftCrownHeight * 0.1f)
    )
    drawCircle(
      color = peacockGreen,
      radius = leftCrownWidth * 0.06f,
      center = Offset(leftCrownCenter.x + leftCrownWidth * 0.2f, leftCrownCenter.y + leftCrownHeight * 0.1f)
    )


    // ----------------------------------------------------------------
    // RIGHT CROWN & VEIL (Shri Shyama Ji)
    // ----------------------------------------------------------------
    val rightCrownCenter = Offset(w * 0.64f, h * 0.47f)
    val rightCrownWidth = w * 0.23f
    val rightCrownHeight = h * 0.23f

    // Red Holy Veil (Odhni) hanging around Shyama Ji's head/crown
    val veilPath = Path().apply {
      moveTo(rightCrownCenter.x - rightCrownWidth * 0.6f, rightCrownCenter.y + rightCrownHeight * 0.5f)
      quadraticTo(
        rightCrownCenter.x, rightCrownCenter.y - rightCrownHeight * 0.9f,
        rightCrownCenter.x + rightCrownWidth * 1.0f, rightCrownCenter.y + rightCrownHeight * 0.6f
      )
      quadraticTo(
        rightCrownCenter.x + rightCrownWidth * 1.3f, rightCrownCenter.y + rightCrownHeight * 1.2f,
        rightCrownCenter.x + rightCrownWidth * 0.8f, rightCrownCenter.y + rightCrownHeight * 1.5f
      )
      lineTo(rightCrownCenter.x - rightCrownWidth * 0.2f, rightCrownCenter.y + rightCrownHeight * 1.4f)
      close()
    }
    drawPath(
      path = veilPath,
      color = crimsonRed.copy(alpha = 0.85f),
      style = Fill
    )
    drawPath(
      path = veilPath,
      color = goldColor,
      style = Stroke(width = 1.dp.toPx())
    )

    // Shri Shyama Ji's Crown (Elegant Golden Mukut)
    val shyamaCrownPath = Path().apply {
      moveTo(rightCrownCenter.x - rightCrownWidth * 0.5f, rightCrownCenter.y + rightCrownHeight * 0.5f)
      lineTo(rightCrownCenter.x + rightCrownWidth * 0.5f, rightCrownCenter.y + rightCrownHeight * 0.5f)
      lineTo(rightCrownCenter.x + rightCrownWidth * 0.4f, rightCrownCenter.y - rightCrownHeight * 0.2f)
      lineTo(rightCrownCenter.x + rightCrownWidth * 0.15f, rightCrownCenter.y - rightCrownHeight * 0.6f)
      lineTo(rightCrownCenter.x, rightCrownCenter.y - rightCrownHeight * 0.3f)
      lineTo(rightCrownCenter.x - rightCrownWidth * 0.15f, rightCrownCenter.y - rightCrownHeight * 0.6f)
      lineTo(rightCrownCenter.x - rightCrownWidth * 0.4f, rightCrownCenter.y - rightCrownHeight * 0.2f)
      close()
    }
    drawPath(
      path = shyamaCrownPath,
      brush = Brush.verticalGradient(
        colors = listOf(goldColor, deepGold)
      )
    )
    drawPath(
      path = shyamaCrownPath,
      color = Color.Black.copy(alpha = 0.2f),
      style = Stroke(width = 1.dp.toPx())
    )

    // Crown jewels for Shyama Ji's crown
    drawCircle(
      color = skyBlue,
      radius = rightCrownWidth * 0.08f,
      center = Offset(rightCrownCenter.x, rightCrownCenter.y + rightCrownHeight * 0.1f)
    )
    drawCircle(
      color = crimsonRed,
      radius = rightCrownWidth * 0.05f,
      center = Offset(rightCrownCenter.x - rightCrownWidth * 0.25f, rightCrownCenter.y + rightCrownHeight * 0.25f)
    )
    drawCircle(
      color = crimsonRed,
      radius = rightCrownWidth * 0.05f,
      center = Offset(rightCrownCenter.x + rightCrownWidth * 0.25f, rightCrownCenter.y + rightCrownHeight * 0.25f)
    )


    // ----------------------------------------------------------------
    // SACRED PINK LOTUS (At the base of the divine couple)
    // ----------------------------------------------------------------
    val lotusCenter = Offset(w * 0.5f, h * 0.76f)
    val lotusWidth = w * 0.52f
    val lotusHeight = h * 0.18f

    val pinkColor = Color(0xFFFF4081)
    val softPink = Color(0xFFFF80AB)

    // Center petal
    val centerPetal = Path().apply {
      moveTo(lotusCenter.x, lotusCenter.y + lotusHeight * 0.5f)
      quadraticTo(lotusCenter.x - lotusWidth * 0.15f, lotusCenter.y - lotusHeight * 0.1f, lotusCenter.x, lotusCenter.y - lotusHeight * 0.6f)
      quadraticTo(lotusCenter.x + lotusWidth * 0.15f, lotusCenter.y - lotusHeight * 0.1f, lotusCenter.x, lotusCenter.y + lotusHeight * 0.5f)
    }
    drawPath(path = centerPetal, color = pinkColor, style = Fill)
    drawPath(path = centerPetal, color = softPink, style = Stroke(width = 1.dp.toPx()))

    // Left inner petal
    val leftInnerPetal = Path().apply {
      moveTo(lotusCenter.x, lotusCenter.y + lotusHeight * 0.5f)
      quadraticTo(lotusCenter.x - lotusWidth * 0.35f, lotusCenter.y - lotusHeight * 0.05f, lotusCenter.x - lotusWidth * 0.2f, lotusCenter.y - lotusHeight * 0.5f)
      quadraticTo(lotusCenter.x - lotusWidth * 0.05f, lotusCenter.y + lotusHeight * 0.1f, lotusCenter.x, lotusCenter.y + lotusHeight * 0.5f)
    }
    drawPath(path = leftInnerPetal, color = pinkColor.copy(alpha = 0.95f), style = Fill)
    drawPath(path = leftInnerPetal, color = softPink, style = Stroke(width = 1.dp.toPx()))

    // Right inner petal
    val rightInnerPetal = Path().apply {
      moveTo(lotusCenter.x, lotusCenter.y + lotusHeight * 0.5f)
      quadraticTo(lotusCenter.x + lotusWidth * 0.35f, lotusCenter.y - lotusHeight * 0.05f, lotusCenter.x + lotusWidth * 0.2f, lotusCenter.y - lotusHeight * 0.5f)
      quadraticTo(lotusCenter.x + lotusWidth * 0.05f, lotusCenter.y + lotusHeight * 0.1f, lotusCenter.x, lotusCenter.y + lotusHeight * 0.5f)
    }
    drawPath(path = rightInnerPetal, color = pinkColor.copy(alpha = 0.95f), style = Fill)
    drawPath(path = rightInnerPetal, color = softPink, style = Stroke(width = 1.dp.toPx()))

    // Left outer petal
    val leftOuterPetal = Path().apply {
      moveTo(lotusCenter.x - lotusWidth * 0.1f, lotusCenter.y + lotusHeight * 0.4f)
      quadraticTo(lotusCenter.x - lotusWidth * 0.55f, lotusCenter.y + lotusHeight * 0.1f, lotusCenter.x - lotusWidth * 0.42f, lotusCenter.y - lotusHeight * 0.28f)
      quadraticTo(lotusCenter.x - lotusWidth * 0.15f, lotusCenter.y + lotusHeight * 0.25f, lotusCenter.x - lotusWidth * 0.1f, lotusCenter.y + lotusHeight * 0.4f)
    }
    drawPath(path = leftOuterPetal, color = pinkColor.copy(alpha = 0.85f), style = Fill)
    drawPath(path = leftOuterPetal, color = softPink, style = Stroke(width = 1.dp.toPx()))

    // Right outer petal
    val rightOuterPetal = Path().apply {
      moveTo(lotusCenter.x + lotusWidth * 0.1f, lotusCenter.y + lotusHeight * 0.4f)
      quadraticTo(lotusCenter.x + lotusWidth * 0.55f, lotusCenter.y + lotusHeight * 0.1f, lotusCenter.x + lotusWidth * 0.42f, lotusCenter.y - lotusHeight * 0.28f)
      quadraticTo(lotusCenter.x + lotusWidth * 0.15f, lotusCenter.y + lotusHeight * 0.25f, lotusCenter.x + lotusWidth * 0.1f, lotusCenter.y + lotusHeight * 0.4f)
    }
    drawPath(path = rightOuterPetal, color = pinkColor.copy(alpha = 0.85f), style = Fill)
    drawPath(path = rightOuterPetal, color = softPink, style = Stroke(width = 1.dp.toPx()))

    drawContext.canvas.restore()
  }
}
