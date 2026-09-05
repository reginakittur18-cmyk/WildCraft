package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.ItemCategory
import com.example.game.model.ItemType
import com.example.game.model.WeatherType
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun GameHud(
  engine: GameEngine,
  onOpenInventory: () -> Unit,
  onOpenMap: () -> Unit,
  onOpenSettings: () -> Unit,
  onAttack: () -> Unit,
  onInteract: () -> Unit,
  onDodge: () -> Unit,
  onJoystickMove: (x: Float, y: Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val pTx = floor(engine.player.x / GameEngine.TILE_SIZE).toInt()
  val pTy = floor(engine.player.y / GameEngine.TILE_SIZE).toInt()
  val currentTile = engine.getTile(pTx, pTy)

  Box(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // ----------------------------------------------------
    // TOP BAR: Survival Stats & World Status
    // ----------------------------------------------------
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      // Left: Vital Bars
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
          .background(Color(0xCC0E1F16), RoundedCornerShape(12.dp))
          .border(1.dp, Color(0x3381C784), RoundedCornerShape(12.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .widthIn(max = 190.dp)
      ) {
        // Health Bar
        VitalMeter(
          icon = Icons.Default.Favorite,
          current = engine.survival.health,
          max = engine.survival.maxHealth,
          barColor = Color(0xFFE53935),
          label = "${engine.survival.health.roundToInt()}"
        )
        // Hunger Bar
        VitalMeter(
          icon = Icons.Default.Restaurant,
          current = engine.survival.hunger,
          max = engine.survival.maxHunger,
          barColor = Color(0xFFFFB300),
          label = "${engine.survival.hunger.roundToInt()}%"
        )
        // Thirst Bar
        VitalMeter(
          icon = Icons.Default.WaterDrop,
          current = engine.survival.thirst,
          max = engine.survival.maxThirst,
          barColor = Color(0xFF03A9F4),
          label = "${engine.survival.thirst.roundToInt()}%"
        )
        // Stamina Bar
        VitalMeter(
          icon = Icons.Default.Bolt,
          current = engine.survival.stamina,
          max = engine.survival.maxStamina,
          barColor = Color(0xFF4CAF50),
          label = "${engine.survival.stamina.roundToInt()}%"
        )
        // Warmth / Temperature
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeviceThermostat,
            contentDescription = "Temperature",
            tint = if (engine.survival.isFreezing) Color(0xFF81D4FA) else if (engine.survival.isOverheating) Color(0xFFFF7043) else Color(0xFFFFD54F),
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "${engine.survival.warmth.roundToInt()}°C  " +
              if (engine.survival.isFreezing) "FREEZING!" else if (engine.survival.isNearFire) "Warm (Fire)" else "Normal",
            color = if (engine.survival.isFreezing) Color(0xFF81D4FA) else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Right: Time, Biome, and Navigation buttons
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Biome and Clock info card
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .background(Color(0xCC0E1F16), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x3381C784), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = if (engine.survival.isNight) Icons.Default.NightsStay else Icons.Default.WbSunny,
            contentDescription = "Time",
            tint = if (engine.survival.isNight) Color(0xFF81D4FA) else Color(0xFFFFD54F),
            modifier = Modifier.size(18.dp)
          )
          Column(horizontalAlignment = Alignment.End) {
            val hours = engine.survival.timeOfDay.toInt()
            val mins = ((engine.survival.timeOfDay - hours) * 60).toInt()
            val timeStr = "%02d:%02d".format(hours, mins)
            Text(
              text = "Day ${engine.survival.dayCount} • $timeStr",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
            Text(
              text = currentTile.biome.displayName,
              color = Color(0xFFA5D6A7),
              fontSize = 11.sp
            )
          }
        }

        // Action icons row (Backpack, Map, Settings)
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          HudCircleButton(
            icon = Icons.Default.Backpack,
            label = "Backpack",
            badgeCount = engine.inventory.size,
            onClick = onOpenInventory,
            testTag = "backpack_button"
          )
          HudCircleButton(
            icon = Icons.Default.Explore,
            label = "Map",
            onClick = onOpenMap,
            testTag = "world_map_button"
          )
          HudCircleButton(
            icon = Icons.Default.Settings,
            label = "Settings",
            onClick = onOpenSettings,
            testTag = "settings_button"
          )
        }
      }
    }

    // ----------------------------------------------------
    // BOTTOM AREA: Controls & Quickbar
    // ----------------------------------------------------
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .padding(bottom = 6.dp)
    ) {
      // Bottom Left: Joystick
      Box(
        modifier = Modifier.align(Alignment.BottomStart)
      ) {
        JoystickView(
          sizeDp = 135,
          onMove = onJoystickMove
        )
      }

      // Bottom Center: Quickbar (4 slots)
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 8.dp)
          .background(Color(0xCC0E1F16), RoundedCornerShape(12.dp))
          .border(1.dp, Color(0x3381C784), RoundedCornerShape(12.dp))
          .padding(6.dp)
      ) {
        val quickSlots = engine.inventory.take(4)
        for (i in 0 until 4) {
          val stack = quickSlots.getOrNull(i)
          val isEquipped = stack != null && (engine.player.equippedWeapon == stack.item || engine.player.equippedArmor == stack.item || engine.player.equippedLight == stack.item)

          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isEquipped) Color(0x5581C784) else Color(0x44000000))
              .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) Color(0xFF81C784) else Color(0x44FFFFFF),
                shape = RoundedCornerShape(8.dp)
              )
              .clickable {
                if (stack != null) {
                  engine.useItem(stack.item)
                }
              }
              .testTag("quickslot_$i"),
            contentAlignment = Alignment.Center
          ) {
            if (stack != null) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = stack.item.displayName.take(5),
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
                Text(
                  text = "x${stack.count}",
                  color = Color(0xFFFFD54F),
                  fontSize = 10.sp
                )
              }
            } else {
              Text(
                text = "${i + 1}",
                color = Color(0x66FFFFFF),
                fontSize = 12.sp
              )
            }
          }
        }
      }

      // Bottom Right: Action Buttons (Attack, Interact, Dodge)
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.align(Alignment.BottomEnd)
      ) {
        // Secondary actions (Interact, Dodge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // Interact / Loot button
          ActionButton(
            icon = Icons.Default.PanTool,
            label = "Interact",
            sizeDp = 48,
            backgroundColor = Color(0xFF2E7D32),
            onClick = onInteract,
            testTag = "interact_button"
          )
          // Dodge button
          ActionButton(
            icon = Icons.Default.DirectionsRun,
            label = "Dodge",
            sizeDp = 48,
            backgroundColor = Color(0xFF37474F),
            onClick = onDodge,
            testTag = "dodge_button"
          )
        }

        // Primary Large Attack Button
        ActionButton(
          icon = Icons.Default.Hardware,
          label = "Attack / Mine",
          sizeDp = 68,
          backgroundColor = Color(0xFFC62828),
          onClick = onAttack,
          testTag = "attack_button"
        )
      }
    }
  }
}

@Composable
fun VitalMeter(
  icon: ImageVector,
  current: Float,
  max: Float,
  barColor: Color,
  label: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = barColor,
      modifier = Modifier.size(13.dp)
    )
    Box(
      modifier = Modifier
        .weight(1f)
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(Color(0x44000000))
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth((current / max).coerceIn(0f, 1f))
          .clip(RoundedCornerShape(4.dp))
          .background(
            Brush.horizontalGradient(
              listOf(barColor.copy(alpha = 0.8f), barColor)
            )
          )
      )
    }
    Text(
      text = label,
      color = Color.White,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.widthIn(min = 28.dp)
    )
  }
}

@Composable
fun HudCircleButton(
  icon: ImageVector,
  label: String,
  badgeCount: Int? = null,
  onClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier = Modifier
      .size(44.dp)
      .clip(CircleShape)
      .background(Color(0xCC0E1F16))
      .border(1.dp, Color(0x4481C784), CircleShape)
      .clickable(onClick = onClick)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = Color(0xFFA5D6A7),
      modifier = Modifier.size(22.dp)
    )
    if (badgeCount != null && badgeCount > 0) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = 2.dp, y = (-2).dp)
          .size(16.dp)
          .clip(CircleShape)
          .background(Color(0xFFE53935)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "$badgeCount",
          color = Color.White,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
fun ActionButton(
  icon: ImageVector,
  label: String,
  sizeDp: Int,
  backgroundColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier = Modifier
      .size(sizeDp.dp)
      .clip(CircleShape)
      .background(
        Brush.radialGradient(
          colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0.8f))
        )
      )
      .border(2.dp, Color(0x88FFFFFF), CircleShape)
      .clickable(onClick = onClick)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = Color.White,
      modifier = Modifier.size((sizeDp * 0.48f).dp)
    )
  }
}
