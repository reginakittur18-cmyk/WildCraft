package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

@Composable
fun JoystickView(
  modifier: Modifier = Modifier,
  sizeDp: Int = 140,
  onMove: (x: Float, y: Float) -> Unit
) {
  var knobOffset by remember { mutableStateOf(Offset.Zero) }
  val radiusPx = (sizeDp / 2f) * 2.5f // Approximate density scale or dynamically measured

  Box(
    modifier = modifier
      .size(sizeDp.dp)
      .testTag("virtual_joystick")
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { offset ->
            val center = Offset(size.width / 2f, size.height / 2f)
            val diff = offset - center
            val dist = hypot(diff.x, diff.y)
            val maxDist = size.width / 2f * 0.75f
            val clampedOffset = if (dist > maxDist) {
              diff * (maxDist / dist)
            } else {
              diff
            }
            knobOffset = clampedOffset
            onMove(clampedOffset.x / maxDist, clampedOffset.y / maxDist)
          },
          onDrag = { change, _ ->
            change.consume()
            val center = Offset(size.width / 2f, size.height / 2f)
            val diff = change.position - center
            val dist = hypot(diff.x, diff.y)
            val maxDist = size.width / 2f * 0.75f
            val clampedOffset = if (dist > maxDist) {
              diff * (maxDist / dist)
            } else {
              diff
            }
            knobOffset = clampedOffset
            onMove(clampedOffset.x / maxDist, clampedOffset.y / maxDist)
          },
          onDragEnd = {
            knobOffset = Offset.Zero
            onMove(0f, 0f)
          },
          onDragCancel = {
            knobOffset = Offset.Zero
            onMove(0f, 0f)
          }
        )
      }
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val outerRadius = size.width / 2f * 0.85f
      val knobRadius = size.width / 2f * 0.38f

      // Outer ring background
      drawCircle(
        color = Color(0x66102018),
        radius = outerRadius,
        center = center
      )
      // Outer ring stroke
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0x8881C784), Color(0x332E7D32)),
          center = center,
          radius = outerRadius
        ),
        radius = outerRadius,
        center = center,
        style = Stroke(width = 3.dp.toPx())
      )

      // Inner directional compass marks
      for (i in 0 until 4) {
        val angle = i * Math.PI / 2.0
        val markStart = Offset(
          center.x + (kotlin.math.cos(angle) * (outerRadius - 14.dp.toPx())).toFloat(),
          center.y + (kotlin.math.sin(angle) * (outerRadius - 14.dp.toPx())).toFloat()
        )
        val markEnd = Offset(
          center.x + (kotlin.math.cos(angle) * (outerRadius - 4.dp.toPx())).toFloat(),
          center.y + (kotlin.math.sin(angle) * (outerRadius - 4.dp.toPx())).toFloat()
        )
        drawLine(
          color = Color(0x88A5D6A7),
          start = markStart,
          end = markEnd,
          strokeWidth = 2.dp.toPx()
        )
      }

      // Inner knob with tactile gradient
      val knobCenter = center + knobOffset
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFF66BB6A), Color(0xFF2E7D32), Color(0xFF1B5E20)),
          center = knobCenter - Offset(4.dp.toPx(), 4.dp.toPx()),
          radius = knobRadius
        ),
        radius = knobRadius,
        center = knobCenter
      )
      // Knob border
      drawCircle(
        color = Color(0xCCFFFFFF),
        radius = knobRadius,
        center = knobCenter,
        style = Stroke(width = 2.dp.toPx())
      )
      // Knob center jewel
      drawCircle(
        color = Color(0xCCFFFFFF),
        radius = knobRadius * 0.25f,
        center = knobCenter
      )
    }
  }
}
