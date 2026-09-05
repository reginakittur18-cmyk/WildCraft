package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.game.engine.GameEngine

@Composable
fun GameScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val engine = remember { GameEngine(context) }

  var joyX by remember { mutableFloatStateOf(0f) }
  var joyY by remember { mutableFloatStateOf(0f) }

  var showInventory by remember { mutableStateOf(false) }
  var showMap by remember { mutableStateOf(false) }
  var showSettings by remember { mutableStateOf(false) }

  // Game Loop at 60 FPS
  var frameCount by remember { mutableLongStateOf(0L) }
  LaunchedEffect(Unit) {
    var lastTimeNanos = 0L
    while (true) {
      withFrameNanos { timeNanos ->
        if (lastTimeNanos != 0L) {
          val dt = ((timeNanos - lastTimeNanos) / 1_000_000_000.0).toFloat()
          engine.update(dt, joyX, joyY)
          frameCount++
        }
        lastTimeNanos = timeNanos
      }
    }
  }

  // Trigger recomposition on frame count
  val currentFrame = frameCount

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0D1B13))
      .testTag("game_screen")
  ) {
    // 1. World Canvas (Procedural open world, lighting, player, creatures)
    GameCanvas(
      engine = engine,
      modifier = Modifier.fillMaxSize()
    )

    // 2. HUD Overlay (Vitals, quickbar, joystick, action buttons)
    GameHud(
      engine = engine,
      onOpenInventory = { showInventory = true },
      onOpenMap = { showMap = true },
      onOpenSettings = { showSettings = true },
      onAttack = { engine.onAttackAction() },
      onInteract = {
        val result = engine.onInteractAction()
        if (result == "Opened Chest") {
          // Trigger chest dialog
        }
      },
      onDodge = { engine.onDodgeAction() },
      onJoystickMove = { x, y ->
        joyX = x
        joyY = y
      },
      modifier = Modifier.fillMaxSize()
    )

    // 3. Modals & Dialogs
    if (showInventory) {
      InventoryDialog(
        engine = engine,
        onDismiss = { showInventory = false }
      )
    }

    if (showMap) {
      WorldMapDialog(
        engine = engine,
        onDismiss = { showMap = false }
      )
    }

    if (showSettings) {
      SettingsDialog(
        engine = engine,
        onDismiss = { showSettings = false },
        onStartNewWorld = { seed ->
          engine.startNewWorld(seed)
          showSettings = false
        }
      )
    }

    // Chest storage dialog
    val chest = engine.activeChest
    if (chest != null) {
      ChestStorageDialog(
        engine = engine,
        chest = chest,
        onDismiss = { engine.activeChest = null }
      )
    }

    // Game Over dialog
    if (engine.isGameOver) {
      GameOverDialog(
        engine = engine,
        onRespawn = { engine.respawnPlayer() },
        onNewWorld = {
          engine.startNewWorld((10000L..999999L).random())
        }
      )
    }
  }
}
