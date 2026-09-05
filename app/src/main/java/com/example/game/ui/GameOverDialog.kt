package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.GameEngine

@Composable
fun GameOverDialog(
  engine: GameEngine,
  onRespawn: () -> Unit,
  onNewWorld: () -> Unit
) {
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .testTag("game_over_dialog"),
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF1E0E10),
      tonalElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .border(2.dp, Color(0xFFE53935), RoundedCornerShape(16.dp))
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "YOU PERISHED",
          color = Color(0xFFFF5252),
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          textAlign = TextAlign.Center
        )

        Text(
          text = "The wilderness claims another soul. Hunger, frost, or beast proved too strong.",
          color = Color.White.copy(alpha = 0.8f),
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        )

        // Statistics Box
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A1417))
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Days Survived:", color = Color(0xFFFF8A80), fontSize = 13.sp)
            Text("${engine.survival.dayCount} Days", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Structures Built:", color = Color(0xFFFF8A80), fontSize = 13.sp)
            Text("${engine.placedStructures.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Explored Chunks:", color = Color(0xFFFF8A80), fontSize = 13.sp)
            Text("${engine.exploredChunks.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }

        // Buttons
        Button(
          onClick = onRespawn,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
          modifier = Modifier.fillMaxWidth().testTag("respawn_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Respawn & Keep World", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onNewWorld,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
          modifier = Modifier.fillMaxWidth().testTag("new_world_death_button")
        ) {
          Text("Generate New World")
        }
      }
    }
  }
}
