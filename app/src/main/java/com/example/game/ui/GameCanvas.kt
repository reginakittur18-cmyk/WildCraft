package com.example.game.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.game.engine.GameEngine
import com.example.game.model.*
import kotlin.math.*

@Composable
fun GameCanvas(
  engine: GameEngine,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    val cameraX = size.width / 2f - engine.player.x
    val cameraY = size.height / 2f - engine.player.y

    // 1. Draw World Tiles
    drawWorldTiles(engine, cameraX, cameraY)

    // 2. Draw Placed Structures
    drawPlacedStructures(engine, cameraX, cameraY)

    // 3. Draw Resources (Trees, Rocks, Bushes)
    drawResources(engine, cameraX, cameraY)

    // 4. Draw Dropped Items
    drawDroppedItems(engine, cameraX, cameraY)

    // 5. Draw Wildlife
    drawWildlife(engine, cameraX, cameraY)

    // 6. Draw Monsters
    drawMonsters(engine, cameraX, cameraY)

    // 7. Draw Player
    drawPlayer(engine, cameraX, cameraY)

    // 8. Draw Particles
    drawParticles(engine, cameraX, cameraY)

    // 9. Draw Weather (Rain / Snow)
    drawWeather(engine)

    // 10. Draw Night Dynamic Lighting Darkness Mask
    drawNightLighting(engine, cameraX, cameraY)

    // 11. Draw Floating Combat & Loot Text
    drawFloatingTexts(engine, cameraX, cameraY)
  }
}

private fun DrawScope.drawWorldTiles(engine: GameEngine, camX: Float, camY: Float) {
  val tileSize = GameEngine.TILE_SIZE
  val minTx = floor((-camX) / tileSize).toInt() - 1
  val maxTx = ceil((-camX + size.width) / tileSize).toInt() + 1
  val minTy = floor((-camY) / tileSize).toInt() - 1
  val maxTy = ceil((-camY + size.height) / tileSize).toInt() + 1

  for (tx in minTx..maxTx) {
    for (ty in minTy..maxTy) {
      val tile = engine.getTile(tx, ty)
      val screenX = tx * tileSize + camX
      val screenY = ty * tileSize + camY

      // Base tile ground
      drawRect(
        color = tile.biome.baseColor,
        topLeft = Offset(screenX, screenY),
        size = Size(tileSize + 1f, tileSize + 1f)
      )

      // Tile texture detail
      when (tile.tileType) {
        TileType.DEEP_WATER, TileType.SHALLOW_WATER -> {
          val waveOffset = (sin((engine.player.walkCycleTime + tx + ty).toDouble()) * 3f).toFloat()
          drawLine(
            color = Color(0x33FFFFFF),
            start = Offset(screenX + 6f + waveOffset, screenY + 14f),
            end = Offset(screenX + tileSize - 6f + waveOffset, screenY + 14f),
            strokeWidth = 2f
          )
          drawLine(
            color = Color(0x22FFFFFF),
            start = Offset(screenX + 12f - waveOffset, screenY + 32f),
            end = Offset(screenX + tileSize - 12f - waveOffset, screenY + 32f),
            strokeWidth = 2f
          )
        }
        TileType.SAND -> {
          drawCircle(
            color = Color(0x22000000),
            radius = 1.5f,
            center = Offset(screenX + 14f, screenY + 16f)
          )
          drawCircle(
            color = Color(0x22FFFFFF),
            radius = 1.5f,
            center = Offset(screenX + 32f, screenY + 34f)
          )
        }
        TileType.SNOW -> {
          drawCircle(
            color = Color(0x44FFFFFF),
            radius = 2f,
            center = Offset(screenX + 20f, screenY + 18f)
          )
          drawCircle(
            color = Color(0x33B0BEC5),
            radius = 1.8f,
            center = Offset(screenX + 36f, screenY + 32f)
          )
        }
        TileType.GRASS -> {
          // Subtle grass blade accents
          if ((tx + ty) % 3 == 0) {
            drawLine(
              color = tile.biome.secondaryColor,
              start = Offset(screenX + 16f, screenY + 24f),
              end = Offset(screenX + 18f, screenY + 14f),
              strokeWidth = 2.2f
            )
            drawLine(
              color = tile.biome.secondaryColor,
              start = Offset(screenX + 21f, screenY + 24f),
              end = Offset(screenX + 25f, screenY + 16f),
              strokeWidth = 2.2f
            )
          }
        }
        TileType.ROCK_GROUND -> {
          drawCircle(
            color = Color(0x28000000),
            radius = 3f,
            center = Offset(screenX + 24f, screenY + 24f)
          )
        }
        TileType.SWAMP_MUD -> {
          drawCircle(
            color = Color(0x401A2416),
            radius = 5f,
            center = Offset(screenX + 20f, screenY + 22f)
          )
        }
        else -> Unit
      }
    }
  }
}

private fun DrawScope.drawPlacedStructures(engine: GameEngine, camX: Float, camY: Float) {
  val tileSize = GameEngine.TILE_SIZE
  for (struct in engine.placedStructures) {
    val sx = struct.tileX * tileSize + camX
    val sy = struct.tileY * tileSize + camY

    when (struct.type) {
      ItemType.CAMPFIRE -> {
        // Campfire stone ring
        drawCircle(
          color = Color(0xFF424242),
          radius = 18f,
          center = Offset(sx + tileSize / 2f, sy + tileSize / 2f),
          style = Stroke(width = 4f)
        )
        // Crossed logs
        drawLine(
          color = Color(0xFF5D4037),
          start = Offset(sx + 14f, sy + 14f),
          end = Offset(sx + 34f, sy + 34f),
          strokeWidth = 4f
        )
        drawLine(
          color = Color(0xFF4E342E),
          start = Offset(sx + 14f, sy + 34f),
          end = Offset(sx + 34f, sy + 14f),
          strokeWidth = 4f
        )
        // Fire flames if burning
        if (struct.fuelRemaining > 0f) {
          val flameFlicker = (sin(engine.player.walkCycleTime.toDouble() * 3.0) * 3f).toFloat()
          // Outer flame
          drawCircle(
            color = Color(0xFFFF5722),
            radius = 9f + flameFlicker,
            center = Offset(sx + tileSize / 2f, sy + tileSize / 2f - 3f)
          )
          // Core flame
          drawCircle(
            color = Color(0xFFFFD54F),
            radius = 5f + flameFlicker * 0.6f,
            center = Offset(sx + tileSize / 2f, sy + tileSize / 2f - 2f)
          )
        }
      }
      ItemType.STORAGE_CHEST -> {
        // Wood chest
        drawRoundRect(
          color = Color(0xFF6D4C41),
          topLeft = Offset(sx + 8f, sy + 12f),
          size = Size(32f, 24f),
          cornerRadius = CornerRadius(4f)
        )
        // Iron bands
        drawRect(
          color = Color(0xFF37474F),
          topLeft = Offset(sx + 12f, sy + 12f),
          size = Size(4f, 24f)
        )
        drawRect(
          color = Color(0xFF37474F),
          topLeft = Offset(sx + 32f, sy + 12f),
          size = Size(4f, 24f)
        )
        // Gold lock
        drawCircle(
          color = Color(0xFFFFD54F),
          radius = 3f,
          center = Offset(sx + 24f, sy + 24f)
        )
      }
      ItemType.WOODEN_WALL -> {
        // Sturdy palisade logs
        for (i in 0..3) {
          drawRoundRect(
            color = if (i % 2 == 0) Color(0xFF5D4037) else Color(0xFF4E342E),
            topLeft = Offset(sx + 6f + i * 9f, sy + 4f),
            size = Size(8f, 40f),
            cornerRadius = CornerRadius(2f)
          )
        }
      }
      ItemType.WOODEN_DOOR -> {
        drawRoundRect(
          color = Color(0xFF8D6E63),
          topLeft = Offset(sx + 8f, sy + 6f),
          size = Size(32f, 36f),
          cornerRadius = CornerRadius(3f)
        )
        drawCircle(
          color = Color(0xFFFFB300),
          radius = 3f,
          center = Offset(sx + 32f, sy + 24f)
        )
      }
      ItemType.BEDROLL -> {
        // Mat
        drawRoundRect(
          color = Color(0xFF2E7D32),
          topLeft = Offset(sx + 10f, sy + 8f),
          size = Size(28f, 32f),
          cornerRadius = CornerRadius(4f)
        )
        // Pillow
        drawRoundRect(
          color = Color(0xFFE8F5E9),
          topLeft = Offset(sx + 12f, sy + 10f),
          size = Size(24f, 10f),
          cornerRadius = CornerRadius(2f)
        )
      }
      ItemType.FARM_PLOT -> {
        // Tilled soil
        drawRoundRect(
          color = Color(0xFF3E2723),
          topLeft = Offset(sx + 6f, sy + 6f),
          size = Size(36f, 36f),
          cornerRadius = CornerRadius(3f)
        )
        // Crop progress
        if (struct.farmGrowthProgress > 0.3f) {
          drawCircle(
            color = Color(0xFF81C784),
            radius = 4f,
            center = Offset(sx + 16f, sy + 24f)
          )
          drawCircle(
            color = Color(0xFF81C784),
            radius = 4f,
            center = Offset(sx + 32f, sy + 24f)
          )
        }
        if (struct.farmGrowthProgress > 0.7f) {
          // Berries on crops
          drawCircle(
            color = Color(0xFFE53935),
            radius = 3.5f,
            center = Offset(sx + 16f, sy + 20f)
          )
          drawCircle(
            color = Color(0xFFE53935),
            radius = 3.5f,
            center = Offset(sx + 32f, sy + 20f)
          )
        }
      }
      else -> Unit
    }
  }
}

private fun DrawScope.drawResources(engine: GameEngine, camX: Float, camY: Float) {
  val tileSize = GameEngine.TILE_SIZE
  val minTx = floor((-camX) / tileSize).toInt() - 1
  val maxTx = ceil((-camX + size.width) / tileSize).toInt() + 1
  val minTy = floor((-camY) / tileSize).toInt() - 1
  val maxTy = ceil((-camY + size.height) / tileSize).toInt() + 1

  val windSway = (sin(engine.player.walkCycleTime.toDouble() * 0.8) * 2f).toFloat()

  for (tx in minTx..maxTx) {
    for (ty in minTy..maxTy) {
      val tile = engine.getTile(tx, ty)
      if (!tile.hasResource) continue

      val sx = tx * tileSize + camX
      val sy = ty * tileSize + camY
      val cx = sx + tileSize / 2f
      val cy = sy + tileSize / 2f

      when (tile.resource) {
        ResourceDeposit.TREE_OAK -> {
          // Shadow
          drawOval(
            color = Color(0x33000000),
            topLeft = Offset(cx - 16f, cy + 8f),
            size = Size(32f, 14f)
          )
          // Trunk
          drawRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(cx - 4f, cy - 2f),
            size = Size(8f, 18f)
          )
          // Foliage layered circles with sway
          drawCircle(
            color = Color(0xFF1B5E20),
            radius = 18f,
            center = Offset(cx + windSway, cy - 10f)
          )
          drawCircle(
            color = Color(0xFF2E7D32),
            radius = 14f,
            center = Offset(cx - 5f + windSway, cy - 16f)
          )
          drawCircle(
            color = Color(0xFF388E3C),
            radius = 12f,
            center = Offset(cx + 6f + windSway, cy - 14f)
          )
        }
        ResourceDeposit.TREE_PINE -> {
          // Shadow
          drawOval(
            color = Color(0x33000000),
            topLeft = Offset(cx - 14f, cy + 8f),
            size = Size(28f, 12f)
          )
          // Trunk
          drawRect(
            color = Color(0xFF4E342E),
            topLeft = Offset(cx - 3.5f, cy + 2f),
            size = Size(7f, 14f)
          )
          // Pine triangle tiers
          val path = Path().apply {
            moveTo(cx + windSway, cy - 28f)
            lineTo(cx - 16f + windSway * 0.5f, cy + 4f)
            lineTo(cx + 16f + windSway * 0.5f, cy + 4f)
            close()
          }
          drawPath(path, color = Color(0xFF133827))

          val topPath = Path().apply {
            moveTo(cx + windSway, cy - 26f)
            lineTo(cx - 12f + windSway * 0.7f, cy - 8f)
            lineTo(cx + 12f + windSway * 0.7f, cy - 8f)
            close()
          }
          drawPath(topPath, color = Color(0xFF1B4D36))
        }
        ResourceDeposit.TREE_PALM -> {
          drawOval(
            color = Color(0x33000000),
            topLeft = Offset(cx - 14f, cy + 8f),
            size = Size(28f, 10f)
          )
          // Slanted trunk
          drawLine(
            color = Color(0xFF8D6E63),
            start = Offset(cx - 2f, cy + 12f),
            end = Offset(cx + 6f + windSway, cy - 14f),
            strokeWidth = 6f
          )
          // Fronds
          for (deg in listOf(0, 60, 120, 180, 240, 300)) {
            val rad = Math.toRadians(deg.toDouble())
            drawLine(
              color = Color(0xFF33691E),
              start = Offset(cx + 6f + windSway, cy - 14f),
              end = Offset((cx + 6f + windSway + cos(rad) * 20f).toFloat(), (cy - 14f + sin(rad) * 16f).toFloat()),
              strokeWidth = 4f
            )
          }
        }
        ResourceDeposit.TREE_CACTUS -> {
          // Saguaro body
          drawRoundRect(
            color = Color(0xFF2E7D32),
            topLeft = Offset(cx - 4f, cy - 18f),
            size = Size(8f, 32f),
            cornerRadius = CornerRadius(4f)
          )
          // Left arm
          drawLine(
            color = Color(0xFF2E7D32),
            start = Offset(cx - 4f, cy - 4f),
            end = Offset(cx - 12f, cy - 4f),
            strokeWidth = 5f
          )
          drawLine(
            color = Color(0xFF2E7D32),
            start = Offset(cx - 12f, cy - 4f),
            end = Offset(cx - 12f, cy - 12f),
            strokeWidth = 5f
          )
          // Right arm
          drawLine(
            color = Color(0xFF2E7D32),
            start = Offset(cx + 4f, cy + 2f),
            end = Offset(cx + 12f, cy + 2f),
            strokeWidth = 5f
          )
          drawLine(
            color = Color(0xFF2E7D32),
            start = Offset(cx + 12f, cy + 2f),
            end = Offset(cx + 12f, cy - 6f),
            strokeWidth = 5f
          )
        }
        ResourceDeposit.ROCK_STONE -> {
          drawOval(
            color = Color(0x33000000),
            topLeft = Offset(cx - 14f, cy + 2f),
            size = Size(28f, 14f)
          )
          drawRoundRect(
            color = Color(0xFF78909C),
            topLeft = Offset(cx - 12f, cy - 8f),
            size = Size(24f, 18f),
            cornerRadius = CornerRadius(6f)
          )
          // Highlight crack
          drawLine(
            color = Color(0xFF90A4AE),
            start = Offset(cx - 8f, cy - 4f),
            end = Offset(cx - 2f, cy + 4f),
            strokeWidth = 2f
          )
        }
        ResourceDeposit.ROCK_IRON -> {
          drawOval(
            color = Color(0x33000000),
            topLeft = Offset(cx - 15f, cy + 2f),
            size = Size(30f, 14f)
          )
          drawRoundRect(
            color = Color(0xFF607D8B),
            topLeft = Offset(cx - 13f, cy - 10f),
            size = Size(26f, 20f),
            cornerRadius = CornerRadius(7f)
          )
          // Rusty metallic iron veins
          drawCircle(color = Color(0xFFFF8A65), radius = 3.5f, center = Offset(cx - 4f, cy - 3f))
          drawCircle(color = Color(0xFFFF8A65), radius = 2.5f, center = Offset(cx + 4f, cy + 2f))
        }
        ResourceDeposit.ROCK_COAL -> {
          drawRoundRect(
            color = Color(0xFF455A64),
            topLeft = Offset(cx - 13f, cy - 9f),
            size = Size(26f, 19f),
            cornerRadius = CornerRadius(6f)
          )
          // Black coal spots
          drawCircle(color = Color(0xFF212121), radius = 3f, center = Offset(cx - 3f, cy - 2f))
          drawCircle(color = Color(0xFF212121), radius = 2.5f, center = Offset(cx + 4f, cy + 1f))
        }
        ResourceDeposit.ROCK_GOLD -> {
          drawRoundRect(
            color = Color(0xFF546E7A),
            topLeft = Offset(cx - 14f, cy - 10f),
            size = Size(28f, 21f),
            cornerRadius = CornerRadius(7f)
          )
          // Shiny gold flecks
          drawCircle(color = Color(0xFFFFD54F), radius = 3.5f, center = Offset(cx - 4f, cy - 3f))
          drawCircle(color = Color(0xFFFFE082), radius = 2.5f, center = Offset(cx + 5f, cy + 1f))
        }
        ResourceDeposit.BUSH_BERRY -> {
          drawCircle(
            color = Color(0xFF388E3C),
            radius = 11f,
            center = Offset(cx, cy)
          )
          // Red berries
          drawCircle(color = Color(0xFFE53935), radius = 3f, center = Offset(cx - 4f, cy - 3f))
          drawCircle(color = Color(0xFFE53935), radius = 3f, center = Offset(cx + 4f, cy - 2f))
          drawCircle(color = Color(0xFFE53935), radius = 2.8f, center = Offset(cx, cy + 4f))
        }
        ResourceDeposit.BUSH_FIBER -> {
          // Hemp reeds
          for (i in -1..1) {
            drawLine(
              color = Color(0xFF8BC34A),
              start = Offset(cx + i * 6f, cy + 8f),
              end = Offset(cx + i * 8f + windSway, cy - 12f),
              strokeWidth = 3f
            )
          }
        }
        ResourceDeposit.BUSH_HERB -> {
          drawCircle(color = Color(0xFF26A69A), radius = 9f, center = Offset(cx, cy))
          drawCircle(color = Color(0xFFE040FB), radius = 2.5f, center = Offset(cx, cy - 4f))
        }
        ResourceDeposit.MUSHROOMS -> {
          // Stalk
          drawRect(color = Color(0xFFECEFF1), topLeft = Offset(cx - 3f, cy), size = Size(6f, 8f))
          // Red cap with white spots
          drawArc(
            color = Color(0xFFD32F2F),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - 9f, cy - 8f),
            size = Size(18f, 14f)
          )
          drawCircle(color = Color.White, radius = 1.8f, center = Offset(cx - 3f, cy - 4f))
          drawCircle(color = Color.White, radius = 1.8f, center = Offset(cx + 3f, cy - 4f))
        }
        else -> Unit
      }
    }
  }
}

private fun DrawScope.drawDroppedItems(engine: GameEngine, camX: Float, camY: Float) {
  for (item in engine.droppedItems) {
    val sx = item.x + camX
    val sy = item.y + camY + sin(item.bobTimer.toDouble()).toFloat() * 3f

    // Floating ground shadow
    drawOval(
      color = Color(0x33000000),
      topLeft = Offset(sx - 8f, item.y + camY + 8f),
      size = Size(16f, 6f)
    )

    // Item box
    val color = when (item.item.category) {
      ItemCategory.MATERIAL -> Color(0xFF8D6E63)
      ItemCategory.CONSUMABLE -> Color(0xFFE53935)
      ItemCategory.TOOL -> Color(0xFF78909C)
      ItemCategory.WEAPON -> Color(0xFFFF7043)
      ItemCategory.STRUCTURE -> Color(0xFFFFB300)
      ItemCategory.ARMOR -> Color(0xFF42A5F5)
    }

    drawRoundRect(
      color = color,
      topLeft = Offset(sx - 7f, sy - 7f),
      size = Size(14f, 14f),
      cornerRadius = CornerRadius(3f)
    )
    drawRoundRect(
      color = Color.White,
      topLeft = Offset(sx - 7f, sy - 7f),
      size = Size(14f, 14f),
      cornerRadius = CornerRadius(3f),
      style = Stroke(width = 1.2f)
    )
  }
}

private fun DrawScope.drawWildlife(engine: GameEngine, camX: Float, camY: Float) {
  for (animal in engine.wildlife) {
    val sx = animal.x + camX
    val sy = animal.y + camY

    // Shadow
    drawOval(
      color = Color(0x30000000),
      topLeft = Offset(sx - 10f, sy + 4f),
      size = Size(20f, 8f)
    )

    val bodyColor = if (animal.hurtFlashTimer > 0f) Color(0xFFFF5252) else when (animal.type) {
      WildlifeType.RABBIT -> Color(0xFFD7CCC8)
      WildlifeType.DEER -> Color(0xFFA1887F)
      WildlifeType.SHEEP -> Color(0xFFF5F5F5)
    }

    // Body
    drawRoundRect(
      color = bodyColor,
      topLeft = Offset(sx - 8f, sy - 6f),
      size = Size(16f, 12f),
      cornerRadius = CornerRadius(4f)
    )

    // Head
    val headX = sx + cos(animal.facingAngle) * 9f
    val headY = sy + sin(animal.facingAngle) * 9f
    drawCircle(
      color = bodyColor,
      radius = 5.5f,
      center = Offset(headX, headY)
    )

    if (animal.type == WildlifeType.RABBIT) {
      // Long ears
      drawLine(
        color = bodyColor,
        start = Offset(headX, headY),
        end = Offset(headX, headY - 8f),
        strokeWidth = 2.5f
      )
    } else if (animal.type == WildlifeType.DEER) {
      // Antlers
      drawLine(
        color = Color(0xFF4E342E),
        start = Offset(headX, headY),
        end = Offset(headX + 4f, headY - 7f),
        strokeWidth = 1.8f
      )
    }
  }
}

private fun DrawScope.drawMonsters(engine: GameEngine, camX: Float, camY: Float) {
  for (mob in engine.monsters) {
    val sx = mob.x + camX
    val sy = mob.y + camY

    // Shadow
    drawOval(
      color = Color(0x35000000),
      topLeft = Offset(sx - 12f, sy + 6f),
      size = Size(24f, 9f)
    )

    val bodyColor = if (mob.hurtFlashTimer > 0f) Color(0xFFFF1744) else when (mob.type) {
      MonsterType.WOLF -> Color(0xFF424242)
      MonsterType.SKELETON -> Color(0xFFECEFF1)
      MonsterType.SCORPION -> Color(0xFF8D6E63)
      MonsterType.FROST_YETI -> Color(0xFFCFD8DC)
    }

    when (mob.type) {
      MonsterType.WOLF -> {
        drawRoundRect(
          color = bodyColor,
          topLeft = Offset(sx - 12f, sy - 7f),
          size = Size(24f, 14f),
          cornerRadius = CornerRadius(5f)
        )
        val headX = sx + cos(mob.facingAngle) * 12f
        val headY = sy + sin(mob.facingAngle) * 12f
        drawCircle(color = bodyColor, radius = 6.5f, center = Offset(headX, headY))
        // Glowing red/yellow eyes
        drawCircle(color = Color(0xFFFFD600), radius = 1.8f, center = Offset(headX + 2f, headY - 1.5f))
      }
      MonsterType.SKELETON -> {
        // Skull
        drawCircle(color = bodyColor, radius = 7f, center = Offset(sx, sy - 8f))
        // Ribcage
        drawLine(color = bodyColor, start = Offset(sx, sy - 1f), end = Offset(sx, sy + 10f), strokeWidth = 3f)
        drawLine(color = bodyColor, start = Offset(sx - 6f, sy + 2f), end = Offset(sx + 6f, sy + 2f), strokeWidth = 2f)
        drawLine(color = bodyColor, start = Offset(sx - 6f, sy + 6f), end = Offset(sx + 6f, sy + 6f), strokeWidth = 2f)
        // Glowing cyan eye sockets
        drawCircle(color = Color(0xFF00E5FF), radius = 1.6f, center = Offset(sx - 2.5f, sy - 9f))
        drawCircle(color = Color(0xFF00E5FF), radius = 1.6f, center = Offset(sx + 2.5f, sy - 9f))
      }
      MonsterType.SCORPION -> {
        drawCircle(color = bodyColor, radius = 8f, center = Offset(sx, sy))
        // Stinger tail
        val tailX = sx - cos(mob.facingAngle) * 12f
        val tailY = sy - sin(mob.facingAngle) * 12f - 6f
        drawLine(color = bodyColor, start = Offset(sx, sy), end = Offset(tailX, tailY), strokeWidth = 3.5f)
        drawCircle(color = Color(0xFFFF1744), radius = 2.5f, center = Offset(tailX, tailY))
      }
      MonsterType.FROST_YETI -> {
        drawRoundRect(
          color = bodyColor,
          topLeft = Offset(sx - 16f, sy - 16f),
          size = Size(32f, 32f),
          cornerRadius = CornerRadius(8f)
        )
        // Icy blue horns
        drawLine(color = Color(0xFF80DEEA), start = Offset(sx - 8f, sy - 16f), end = Offset(sx - 14f, sy - 24f), strokeWidth = 3f)
        drawLine(color = Color(0xFF80DEEA), start = Offset(sx + 8f, sy - 16f), end = Offset(sx + 14f, sy - 24f), strokeWidth = 3f)
      }
    }
  }
}

private fun DrawScope.drawPlayer(engine: GameEngine, camX: Float, camY: Float) {
  val player = engine.player
  val sx = size.width / 2f
  val sy = size.height / 2f

  // Shadow
  drawOval(
    color = Color(0x38000000),
    topLeft = Offset(sx - 12f, sy + 10f),
    size = Size(24f, 10f)
  )

  // Walking leg bob
  val legOffset = if (player.isWalking) (sin(player.walkCycleTime.toDouble()) * 4f).toFloat() else 0f

  // Legs
  drawLine(
    color = Color(0xFF263238),
    start = Offset(sx - 4f, sy + 6f),
    end = Offset(sx - 4f, sy + 14f + legOffset),
    strokeWidth = 4f
  )
  drawLine(
    color = Color(0xFF263238),
    start = Offset(sx + 4f, sy + 6f),
    end = Offset(sx + 4f, sy + 14f - legOffset),
    strokeWidth = 4f
  )

  // Tunic / Armor
  val tunicColor = when (player.equippedArmor) {
    ItemType.LEATHER_TUNIC -> Color(0xFF8D6E63)
    ItemType.FUR_CLOAK -> Color(0xFF4E342E)
    ItemType.IRON_ARMOR -> Color(0xFF90A4AE)
    else -> Color(0xFF1E88E5) // Default adventuring tunic
  }

  drawRoundRect(
    color = tunicColor,
    topLeft = Offset(sx - 8f, sy - 6f),
    size = Size(16f, 16f),
    cornerRadius = CornerRadius(3f)
  )

  // Head
  drawCircle(
    color = Color(0xFFFFCC80),
    radius = 7f,
    center = Offset(sx, sy - 11f)
  )

  // Hair / Hood
  if (player.equippedArmor == ItemType.FUR_CLOAK) {
    drawArc(
      color = Color(0xFF3E2723),
      startAngle = 180f,
      sweepAngle = 180f,
      useCenter = true,
      topLeft = Offset(sx - 8f, sy - 19f),
      size = Size(16f, 14f)
    )
  } else {
    drawArc(
      color = Color(0xFF4E342E),
      startAngle = 170f,
      sweepAngle = 200f,
      useCenter = true,
      topLeft = Offset(sx - 7.5f, sy - 19f),
      size = Size(15f, 13f)
    )
  }

  // Weapon / Tool in hand
  val handX = sx + cos(player.facingAngle) * 14f
  val handY = sy + sin(player.facingAngle) * 14f

  val weapon = player.equippedWeapon
  if (weapon != null) {
    val swingAngle = if (player.isAttacking) {
      player.facingAngle + (player.attackTimer - 0.5f) * 1.6f
    } else {
      player.facingAngle + 0.4f
    }

    val weaponTipX = handX + cos(swingAngle) * 18f
    val weaponTipY = handY + sin(swingAngle) * 18f

    val weaponColor = when (weapon) {
      ItemType.WOODEN_CLUB -> Color(0xFF6D4C41)
      ItemType.STONE_AXE, ItemType.STONE_PICKAXE -> Color(0xFF78909C)
      ItemType.IRON_SWORD, ItemType.IRON_AXE, ItemType.IRON_PICKAXE -> Color(0xFFECEFF1)
      ItemType.HUNTER_BOW -> Color(0xFF8D6E63)
      else -> Color(0xFFB0BEC5)
    }

    drawLine(
      color = weaponColor,
      start = Offset(handX, handY),
      end = Offset(weaponTipX, weaponTipY),
      strokeWidth = 3.5f
    )

    // Blade / Axe head
    if (weapon == ItemType.STONE_AXE || weapon == ItemType.IRON_AXE) {
      drawCircle(color = weaponColor, radius = 4f, center = Offset(weaponTipX, weaponTipY))
    }
  }

  // Torch in second hand if equipped
  if (player.equippedLight == ItemType.TORCH) {
    val torchX = sx + cos(player.facingAngle - 1.2f) * 13f
    val torchY = sy + sin(player.facingAngle - 1.2f) * 13f
    drawLine(color = Color(0xFF5D4037), start = Offset(sx, sy), end = Offset(torchX, torchY), strokeWidth = 3f)
    drawCircle(color = Color(0xFFFF5722), radius = 5f, center = Offset(torchX, torchY))
    drawCircle(color = Color(0xFFFFD54F), radius = 2.5f, center = Offset(torchX, torchY))
  }
}

private fun DrawScope.drawParticles(engine: GameEngine, camX: Float, camY: Float) {
  for (p in engine.particles) {
    val sx = p.x + camX
    val sy = p.y + camY
    drawCircle(
      color = p.color.copy(alpha = p.alpha),
      radius = p.size,
      center = Offset(sx, sy)
    )
  }
}

private fun DrawScope.drawWeather(engine: GameEngine) {
  when (engine.survival.weather) {
    WeatherType.RAINY -> {
      for (i in 0..40) {
        val rx = ((Math.sin((engine.player.walkCycleTime * 2f + i * 17).toDouble()) * 0.5 + 0.5) * size.width).toFloat()
        val ry = (((engine.player.walkCycleTime * 400f + i * 37) % size.height)).toFloat()
        drawLine(
          color = Color(0x66B3E5FC),
          start = Offset(rx, ry),
          end = Offset(rx - 4f, ry + 16f),
          strokeWidth = 1.8f
        )
      }
    }
    WeatherType.SNOWSTORM -> {
      for (i in 0..50) {
        val sx = ((Math.sin((engine.player.walkCycleTime + i * 23).toDouble()) * 0.5 + 0.5) * size.width).toFloat()
        val sy = (((engine.player.walkCycleTime * 150f + i * 29) % size.height)).toFloat()
        drawCircle(
          color = Color(0x88FFFFFF),
          radius = 2.5f,
          center = Offset(sx, sy)
        )
      }
    }
    WeatherType.FOGGY -> {
      drawRect(
        color = Color(0x33ECEFF1),
        topLeft = Offset.Zero,
        size = size
      )
    }
    else -> Unit
  }
}

private fun DrawScope.drawNightLighting(engine: GameEngine, camX: Float, camY: Float) {
  val lightLevel = engine.survival.ambientLightLevel
  if (lightLevel >= 0.95f) return

  val darknessAlpha = ((1f - lightLevel) * 0.88f).coerceIn(0f, 0.88f)
  val pSx = size.width / 2f
  val pSy = size.height / 2f

  // Torch / base vision radius
  val lightRadius = if (engine.player.equippedLight == ItemType.TORCH) 340f else 190f

  // Smooth radial gradient light hole for player
  val brush = Brush.radialGradient(
    colors = listOf(
      Color.Transparent,
      Color(0x22000000),
      Color.Black.copy(alpha = darknessAlpha)
    ),
    center = Offset(pSx, pSy),
    radius = lightRadius
  )

  drawRect(
    brush = brush,
    topLeft = Offset.Zero,
    size = size
  )

  // Campfires glow in darkness
  for (struct in engine.placedStructures) {
    if (struct.type == ItemType.CAMPFIRE && struct.fuelRemaining > 0f) {
      val cSx = struct.tileX * GameEngine.TILE_SIZE + GameEngine.TILE_SIZE / 2f + camX
      val cSy = struct.tileY * GameEngine.TILE_SIZE + GameEngine.TILE_SIZE / 2f + camY
      val fireBrush = Brush.radialGradient(
        colors = listOf(
          Color(0x66FFB74D),
          Color(0x33FF9800),
          Color.Transparent
        ),
        center = Offset(cSx, cSy),
        radius = 210f
      )
      drawCircle(
        brush = fireBrush,
        radius = 210f,
        center = Offset(cSx, cSy)
      )
    }
  }
}

private fun DrawScope.drawFloatingTexts(engine: GameEngine, camX: Float, camY: Float) {
  val paint = Paint().apply {
    isAntiAlias = true
    textSize = 34f
    typeface = Typeface.DEFAULT_BOLD
    textAlign = Paint.Align.CENTER
  }

  for (t in engine.floatingTexts) {
    val sx = t.x + camX
    val sy = t.y + camY
    val alpha = (t.lifetime / t.maxLifetime).coerceIn(0f, 1f)

    drawIntoCanvas { canvas ->
      // Drop shadow for crisp readability
      paint.color = android.graphics.Color.argb((alpha * 180).toInt(), 0, 0, 0)
      canvas.nativeCanvas.drawText(t.text, sx + 1.5f, sy + 1.5f, paint)

      // Foreground text
      paint.color = android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (t.color.red * 255).toInt(),
        (t.color.green * 255).toInt(),
        (t.color.blue * 255).toInt()
      )
      canvas.nativeCanvas.drawText(t.text, sx, sy, paint)
    }
  }
}
