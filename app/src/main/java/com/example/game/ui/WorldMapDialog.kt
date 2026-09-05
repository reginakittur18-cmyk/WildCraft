package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.GameEngine
import com.example.game.model.BiomeType
import com.example.game.model.ItemType
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun WorldMapDialog(
  engine: GameEngine,
  onDismiss: () -> Unit
) {
  val playerTx = floor(engine.player.x / GameEngine.TILE_SIZE).toInt()
  val playerTy = floor(engine.player.y / GameEngine.TILE_SIZE).toInt()
  val currentBiome = engine.worldGen.getBiomeAt(playerTx, playerTy)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.88f)
        .testTag("world_map_dialog"),
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF102018),
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Cartographer's Map",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
            Text(
              text = "Position: X: $playerTx, Y: $playerTy • ${currentBiome.displayName}",
              color = Color(0xFFA5D6A7),
              fontSize = 12.sp
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_map_button")) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Map Canvas Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x6681C784), RoundedCornerShape(10.dp))
            .background(Color(0xFF08140E))
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val mapRadiusTiles = 40 // View 80x80 tiles around player
            val mapStep = 2 // Sample every 2 tiles for fast rendering
            val tilePixelSize = size.width / (mapRadiusTiles * 2) * mapStep

            val centerScreen = Offset(size.width / 2f, size.height / 2f)

            // Render terrain
            for (dx in -mapRadiusTiles..mapRadiusTiles step mapStep) {
              for (dy in -mapRadiusTiles..mapRadiusTiles step mapStep) {
                val sampleTx = playerTx + dx
                val sampleTy = playerTy + dy
                val biome = engine.worldGen.getBiomeAt(sampleTx, sampleTy)

                val px = centerScreen.x + (dx / mapStep.toFloat()) * tilePixelSize
                val py = centerScreen.y + (dy / mapStep.toFloat()) * tilePixelSize

                drawRect(
                  color = biome.baseColor,
                  topLeft = Offset(px, py),
                  size = Size(tilePixelSize + 0.5f, tilePixelSize + 0.5f)
                )
              }
            }

            // Draw placed structures pins (Campfires, Chests, Bedrolls)
            for (struct in engine.placedStructures) {
              val sDx = struct.tileX - playerTx
              val sDy = struct.tileY - playerTy
              if (kotlin.math.abs(sDx) < mapRadiusTiles && kotlin.math.abs(sDy) < mapRadiusTiles) {
                val pinX = centerScreen.x + sDx * (tilePixelSize / mapStep)
                val pinY = centerScreen.y + sDy * (tilePixelSize / mapStep)

                val pinColor = when (struct.type) {
                  ItemType.CAMPFIRE -> Color(0xFFFF5722)
                  ItemType.STORAGE_CHEST -> Color(0xFFFFD54F)
                  ItemType.BEDROLL -> Color(0xFF81C784)
                  else -> Color.White
                }
                drawCircle(color = pinColor, radius = 4f, center = Offset(pinX, pinY))
                drawCircle(color = Color.Black, radius = 4f, center = Offset(pinX, pinY), style = Stroke(width = 1f))
              }
            }

            // Radar rings
            drawCircle(
              color = Color(0x3381C784),
              radius = size.width * 0.25f,
              center = centerScreen,
              style = Stroke(width = 1f)
            )
            drawCircle(
              color = Color(0x2281C784),
              radius = size.width * 0.45f,
              center = centerScreen,
              style = Stroke(width = 1f)
            )

            // Compass crosshairs
            drawLine(
              color = Color(0x33FFFFFF),
              start = Offset(centerScreen.x, 10f),
              end = Offset(centerScreen.x, size.height - 10f),
              strokeWidth = 1f
            )
            drawLine(
              color = Color(0x33FFFFFF),
              start = Offset(10f, centerScreen.y),
              end = Offset(size.width - 10f, centerScreen.y),
              strokeWidth = 1f
            )

            // Player Marker (blinking gold star)
            drawCircle(
              color = Color(0x55FFEB3B),
              radius = 12f,
              center = centerScreen
            )
            drawCircle(
              color = Color(0xFFFFD54F),
              radius = 6f,
              center = centerScreen
            )
            drawCircle(
              color = Color.Black,
              radius = 6f,
              center = centerScreen,
              style = Stroke(width = 1.5f)
            )
          }

          // Compass cardinal indicators
          Text(
            text = "N",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
          )
          Text(
            text = "S",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
          )
          Text(
            text = "W",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp)
          )
          Text(
            text = "E",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Biome Legend
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          LegendPill(color = BiomeType.FOREST.baseColor, name = "Forest")
          LegendPill(color = BiomeType.PLAINS.baseColor, name = "Plains")
          LegendPill(color = BiomeType.DESERT.baseColor, name = "Desert")
          LegendPill(color = BiomeType.SNOW_PEAKS.baseColor, name = "Snow")
          LegendPill(color = BiomeType.SHALLOW_WATER.baseColor, name = "Water")
        }
      }
    }
  }
}

@Composable
fun LegendPill(color: Color, name: String) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
    Text(text = name, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
  }
}
