package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.GameEngine
import com.example.game.model.ItemStack
import com.example.game.model.PlacedStructureEntity

@Composable
fun ChestStorageDialog(
  engine: GameEngine,
  chest: PlacedStructureEntity,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .testTag("chest_storage_dialog"),
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
            text = "Storage Chest",
            color = Color(0xFFFFD54F),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_chest_button")) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chest Items
        Text("Chest Contents (Tap item to take)", color = Color(0xFFA5D6A7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
          columns = GridCells.Fixed(4),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(Color(0xFF1A3325), RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          items(chest.storageItems) { stack ->
            ItemGridSlot(
              stack = stack,
              onClick = {
                // Transfer from chest to inventory
                chest.storageItems.remove(stack)
                engine.addItemToInventory(stack.item, stack.count)
              }
            )
          }
          val emptySlots = (12 - chest.storageItems.size).coerceAtLeast(0)
          items(emptySlots) {
            EmptyGridSlot()
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Backpack Items
        Text("Your Backpack (Tap item to store)", color = Color(0xFFA5D6A7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
          columns = GridCells.Fixed(4),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(Color(0xFF1E382A), RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          items(engine.inventory) { stack ->
            ItemGridSlot(
              stack = stack,
              onClick = {
                // Transfer from inventory to chest
                if (chest.storageItems.size < 16) {
                  engine.consumeItemFromInventory(stack.item, stack.count)
                  val existingInChest = chest.storageItems.firstOrNull { it.item == stack.item }
                  if (existingInChest != null) {
                    existingInChest.count += stack.count
                  } else {
                    chest.storageItems.add(ItemStack(stack.item, stack.count))
                  }
                }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun ItemGridSlot(stack: ItemStack, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFF264D38))
      .border(1.dp, Color(0x4481C784), RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = stack.item.displayName.take(6),
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
  }
}

@Composable
fun EmptyGridSlot() {
  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFF12241A))
      .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(6.dp))
  )
}
