package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.game.model.*

@Composable
fun InventoryDialog(
  engine: GameEngine,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0 = Backpack, 1 = Crafting
  var selectedItem by remember { mutableStateOf<ItemStack?>(engine.inventory.firstOrNull()) }
  var craftCategoryFilter by remember { mutableStateOf<ItemCategory?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .testTag("inventory_dialog"),
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF132219),
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        // Header with Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF192F23),
            contentColor = Color(0xFF81C784),
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
          ) {
            Tab(
              selected = selectedTab == 0,
              onClick = { selectedTab = 0 },
              text = { Text("Backpack (${engine.inventory.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
              selected = selectedTab == 1,
              onClick = { selectedTab = 1 },
              text = { Text("Crafting", fontWeight = FontWeight.Bold) }
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_inventory_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
          // ----------------------------------------------------
          // BACKPACK TAB
          // ----------------------------------------------------
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Left: Grid of Items
            Column(modifier = Modifier.weight(1.3f)) {
              // Equipped items row
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF1E382A), RoundedCornerShape(8.dp))
                  .padding(8.dp)
              ) {
                EquipSlot(label = "Weapon", item = engine.player.equippedWeapon)
                EquipSlot(label = "Armor", item = engine.player.equippedArmor)
                EquipSlot(label = "Light", item = engine.player.equippedLight)
              }

              Spacer(modifier = Modifier.height(8.dp))

              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                items(engine.inventory) { stack ->
                  val isSelected = selectedItem == stack
                  val isEquipped = engine.player.equippedWeapon == stack.item ||
                    engine.player.equippedArmor == stack.item ||
                    engine.player.equippedLight == stack.item

                  Box(
                    modifier = Modifier
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isSelected) Color(0xFF2E6B47) else Color(0xFF1A3325))
                      .border(
                        width = if (isSelected) 2.dp else if (isEquipped) 1.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFFFFD54F) else if (isEquipped) Color(0xFF81C784) else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(8.dp)
                      )
                      .clickable { selectedItem = stack }
                      .padding(4.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Text(
                        text = stack.item.displayName.take(7),
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
                      if (isEquipped) {
                        Text(
                          text = "EQUIPPED",
                          color = Color(0xFF81C784),
                          fontSize = 8.sp,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }
                }
              }
            }

            // Right: Selected Item Details
            Column(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF192F23), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0x3381C784), RoundedCornerShape(10.dp))
                .padding(10.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              val current = selectedItem
              if (current != null) {
                Column {
                  Text(
                    text = current.item.displayName,
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  Text(
                    text = current.item.category.name,
                    color = Color(0xFFA5D6A7),
                    fontSize = 11.sp
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = current.item.description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                  )

                  Spacer(modifier = Modifier.height(10.dp))

                  // Stats preview
                  if (current.item.attackDamage > 0) {
                    Text("⚔ Attack: +${current.item.attackDamage}", color = Color(0xFFFF8A80), fontSize = 12.sp)
                  }
                  if (current.item.armorValue > 0) {
                    Text("🛡 Armor: +${current.item.armorValue}", color = Color(0xFF90CAF9), fontSize = 12.sp)
                  }
                  if (current.item.hungerRestore > 0) {
                    Text("🍖 Hunger: +${current.item.hungerRestore}", color = Color(0xFFFFCC80), fontSize = 12.sp)
                  }
                  if (current.item.thirstRestore > 0) {
                    Text("💧 Thirst: +${current.item.thirstRestore}", color = Color(0xFF81D4FA), fontSize = 12.sp)
                  }
                  if (current.item.healthRestore > 0) {
                    Text("❤ Health: +${current.item.healthRestore}", color = Color(0xFFE57373), fontSize = 12.sp)
                  }
                  if (current.item.warmthRestore > 0) {
                    Text("🔥 Warmth: +${current.item.warmthRestore}°C", color = Color(0xFFFFB74D), fontSize = 12.sp)
                  }
                }

                // Action buttons
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  val actionLabel = when (current.item.category) {
                    ItemCategory.CONSUMABLE -> "Use / Consume"
                    ItemCategory.WEAPON, ItemCategory.TOOL, ItemCategory.ARMOR -> {
                      val isEq = engine.player.equippedWeapon == current.item ||
                        engine.player.equippedArmor == current.item ||
                        engine.player.equippedLight == current.item
                      if (isEq) "Unequip" else "Equip"
                    }
                    ItemCategory.STRUCTURE -> "Place Structure"
                    else -> null
                  }

                  if (actionLabel != null) {
                    Button(
                      onClick = {
                        val used = engine.useItem(current.item)
                        if (used && current.item.category == ItemCategory.STRUCTURE) {
                          onDismiss()
                        }
                        if (current.count <= 0) {
                          selectedItem = engine.inventory.firstOrNull()
                        }
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                      modifier = Modifier.fillMaxWidth().testTag("use_item_button")
                    ) {
                      Text(actionLabel, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                  }

                  OutlinedButton(
                    onClick = {
                      if (engine.consumeItemFromInventory(current.item, 1)) {
                        engine.addDroppedItem(current.item, 1, engine.player.x, engine.player.y)
                        if (current.count <= 0) {
                          selectedItem = engine.inventory.firstOrNull()
                        }
                      }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
                    modifier = Modifier.fillMaxWidth().testTag("drop_item_button")
                  ) {
                    Text("Drop (1)")
                  }
                }
              } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  Text("Select an item to view details", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
              }
            }
          }
        } else {
          // ----------------------------------------------------
          // CRAFTING TAB
          // ----------------------------------------------------
          Column(modifier = Modifier.fillMaxSize()) {
            // Category filter chips
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
              FilterChip(
                selected = craftCategoryFilter == null,
                onClick = { craftCategoryFilter = null },
                label = { Text("All", fontSize = 11.sp) }
              )
              FilterChip(
                selected = craftCategoryFilter == ItemCategory.TOOL,
                onClick = { craftCategoryFilter = ItemCategory.TOOL },
                label = { Text("Tools", fontSize = 11.sp) }
              )
              FilterChip(
                selected = craftCategoryFilter == ItemCategory.WEAPON,
                onClick = { craftCategoryFilter = ItemCategory.WEAPON },
                label = { Text("Weapons", fontSize = 11.sp) }
              )
              FilterChip(
                selected = craftCategoryFilter == ItemCategory.CONSUMABLE,
                onClick = { craftCategoryFilter = ItemCategory.CONSUMABLE },
                label = { Text("Survival", fontSize = 11.sp) }
              )
              FilterChip(
                selected = craftCategoryFilter == ItemCategory.STRUCTURE,
                onClick = { craftCategoryFilter = ItemCategory.STRUCTURE },
                label = { Text("Build", fontSize = 11.sp) }
              )
            }

            val filteredRecipes = CraftingRecipes.allRecipes.filter {
              craftCategoryFilter == null || it.category == craftCategoryFilter
            }

            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(filteredRecipes) { recipe ->
                CraftingRecipeCard(
                  recipe = recipe,
                  engine = engine,
                  onCraft = { engine.craftRecipe(recipe) }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun EquipSlot(label: String, item: ItemType?) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(60.dp)
  ) {
    Text(label, color = Color(0xFFA5D6A7), fontSize = 10.sp)
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(Color(0xFF12241A))
        .border(1.dp, Color(0x4481C784), RoundedCornerShape(6.dp)),
      contentAlignment = Alignment.Center
    ) {
      if (item != null) {
        Text(
          text = item.displayName.take(5),
          color = Color(0xFFFFD54F),
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold
        )
      } else {
        Text("-", color = Color(0x44FFFFFF), fontSize = 14.sp)
      }
    }
  }
}

@Composable
fun CraftingRecipeCard(
  recipe: CraftingRecipe,
  engine: GameEngine,
  onCraft: () -> Unit
) {
  val canCraft = recipe.ingredients.all { ing ->
    engine.inventory.filter { it.item == ing.item }.sumOf { it.count } >= ing.amount
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color(0xFF1A3325), RoundedCornerShape(8.dp))
      .border(1.dp, if (canCraft) Color(0x6681C784) else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
      .padding(10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = recipe.result.displayName + if (recipe.resultCount > 1) " x${recipe.resultCount}" else "",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Text(
          text = recipe.category.name,
          color = Color(0xFFA5D6A7),
          fontSize = 10.sp
        )
      }

      Text(
        text = recipe.description,
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 11.sp
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Ingredients row
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        recipe.ingredients.forEach { ing ->
          val owned = engine.inventory.filter { it.item == ing.item }.sumOf { it.count }
          val hasEnough = owned >= ing.amount
          Text(
            text = "${ing.item.displayName}: $owned/${ing.amount}",
            color = if (hasEnough) Color(0xFFA5D6A7) else Color(0xFFFF8A80),
            fontSize = 11.sp,
            fontWeight = if (hasEnough) FontWeight.Normal else FontWeight.Bold
          )
        }
      }
    }

    Button(
      onClick = onCraft,
      enabled = canCraft,
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF2E7D32),
        disabledContainerColor = Color(0xFF263238)
      ),
      shape = RoundedCornerShape(6.dp),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
      modifier = Modifier.testTag("craft_${recipe.id}_button")
    ) {
      Text("Craft", color = if (canCraft) Color.White else Color(0x66FFFFFF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
  }
}
