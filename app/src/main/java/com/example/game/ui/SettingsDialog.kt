package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.GameEngine

@Composable
fun SettingsDialog(
  engine: GameEngine,
  onDismiss: () -> Unit,
  onStartNewWorld: (seed: Long) -> Unit
) {
  var seedInput by remember { mutableStateOf(engine.worldSeed.toString()) }
  var showGuide by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .testTag("settings_dialog"),
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF132219),
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
          Text(
            text = "Game Settings",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_button")) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            // World Seed Card
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A3325))
                .border(1.dp, Color(0x4481C784), RoundedCornerShape(10.dp))
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text("Procedural World Generation", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(
                "Every world seed generates an endless, unique arrangement of biomes, mountains, rivers, and ore veins.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp
              )

              OutlinedTextField(
                value = seedInput,
                onValueChange = { seedInput = it },
                label = { Text("World Seed", color = Color(0xFFA5D6A7)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                  focusedBorderColor = Color(0xFF81C784),
                  unfocusedBorderColor = Color(0x44FFFFFF)
                ),
                modifier = Modifier.fillMaxWidth().testTag("seed_input_field")
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = {
                    val parsedSeed = seedInput.toLongOrNull() ?: (10000L..999999L).random()
                    onStartNewWorld(parsedSeed)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                  modifier = Modifier.weight(1f).testTag("generate_new_world_button")
                ) {
                  Text("New World")
                }
                OutlinedButton(
                  onClick = {
                    val randomSeed = (10000L..999999L).random()
                    seedInput = randomSeed.toString()
                    onStartNewWorld(randomSeed)
                  },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA5D6A7)),
                  modifier = Modifier.weight(1f).testTag("random_seed_button")
                ) {
                  Text("Randomize")
                }
              }
            }
          }

          item {
            // Survival Guide Accordion
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A3325))
                .border(1.dp, Color(0x4481C784), RoundedCornerShape(10.dp))
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Survival Guide", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextButton(onClick = { showGuide = !showGuide }) {
                  Text(if (showGuide) "Hide" else "Read", color = Color(0xFFA5D6A7))
                }
              }

              if (showGuide) {
                Text(
                  text = "🌲 Gathering & Mining:\n" +
                    "• Tap [ATTACK] near trees or boulders to chop wood and mine stone & iron.\n" +
                    "• Equip Stone Axe or Pickaxe to harvest 2x faster.\n\n" +
                    "🍎 Food & Water:\n" +
                    "• Tap [INTERACT] near Berry bushes to harvest wild food.\n" +
                    "• Stand near shallow water and tap [INTERACT] to drink fresh water.\n" +
                    "• Hunt hares and deer for raw meat; cook it over a campfire for massive health & hunger restore.\n\n" +
                    "🔥 Surviving the Cold & Night:\n" +
                    "• Night brings wolves and drops temperature by 12°C. In Snow Tundra, freezing occurs rapidly.\n" +
                    "• Place Campfires or carry Torches to ward off frost and predators.\n" +
                    "• Craft a Bedroll to sleep through the night until 6:00 AM dawn.\n\n" +
                    "⚔ Combat & Defense:\n" +
                    "• Equip weapons in your Backpack.\n" +
                    "• Use [DODGE] to dash out of reach when wolves or skeletons lunge.",
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 12.sp,
                  lineHeight = 17.sp
                )
              }
            }
          }

          item {
            // Save & Status Card
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E382A))
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text("Game Status", color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("• Day ${engine.survival.dayCount} (${engine.survival.weather.displayName})", color = Color.White, fontSize = 12.sp)
              Text("• World Seed: ${engine.worldSeed}", color = Color.White, fontSize = 12.sp)
              Text("• Placed Structures: ${engine.placedStructures.size}", color = Color.White, fontSize = 12.sp)
              Text("• Inventory Stacks: ${engine.inventory.size}", color = Color.White, fontSize = 12.sp)

              Spacer(modifier = Modifier.height(4.dp))
              Button(
                onClick = {
                  engine.addFloatingText("Game Saved!", Color(0xFF81C784), engine.player.x, engine.player.y - 20f)
                  onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                modifier = Modifier.fillMaxWidth().testTag("manual_save_button")
              ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save & Resume")
              }
            }
          }
        }
      }
    }
  }
}
