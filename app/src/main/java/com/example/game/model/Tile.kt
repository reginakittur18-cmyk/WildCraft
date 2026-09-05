package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class TileType(val isWalkable: Boolean, val slowsMovement: Boolean) {
  DEEP_WATER(isWalkable = false, slowsMovement = false),
  SHALLOW_WATER(isWalkable = true, slowsMovement = true),
  SAND(isWalkable = true, slowsMovement = false),
  GRASS(isWalkable = true, slowsMovement = false),
  DIRT(isWalkable = true, slowsMovement = false),
  SNOW(isWalkable = true, slowsMovement = true),
  ROCK_GROUND(isWalkable = true, slowsMovement = false),
  SWAMP_MUD(isWalkable = true, slowsMovement = true)
}

enum class ResourceDeposit(
  val displayName: String,
  val maxHealth: Int,
  val requiredTool: ToolType,
  val drops: List<ResourceDrop>
) {
  NONE("None", 0, ToolType.NONE, emptyList()),
  TREE_OAK(
    displayName = "Oak Tree",
    maxHealth = 4,
    requiredTool = ToolType.AXE,
    drops = listOf(
      ResourceDrop(ItemType.WOOD, min = 2, max = 4),
      ResourceDrop(ItemType.SAPLING, min = 0, max = 1),
      ResourceDrop(ItemType.LEAVES, min = 1, max = 2)
    )
  ),
  TREE_PINE(
    displayName = "Pine Tree",
    maxHealth = 4,
    requiredTool = ToolType.AXE,
    drops = listOf(
      ResourceDrop(ItemType.WOOD, min = 3, max = 5),
      ResourceDrop(ItemType.RESIN, min = 1, max = 2)
    )
  ),
  TREE_PALM(
    displayName = "Palm Tree",
    maxHealth = 3,
    requiredTool = ToolType.AXE,
    drops = listOf(
      ResourceDrop(ItemType.WOOD, min = 2, max = 3),
      ResourceDrop(ItemType.COCONUT, min = 1, max = 2)
    )
  ),
  TREE_CACTUS(
    displayName = "Cactus",
    maxHealth = 2,
    requiredTool = ToolType.NONE,
    drops = listOf(
      ResourceDrop(ItemType.CACTUS_FLESH, min = 1, max = 3),
      ResourceDrop(ItemType.FIBER, min = 1, max = 2)
    )
  ),
  ROCK_STONE(
    displayName = "Stone Boulder",
    maxHealth = 5,
    requiredTool = ToolType.PICKAXE,
    drops = listOf(
      ResourceDrop(ItemType.STONE, min = 2, max = 4),
      ResourceDrop(ItemType.FLINT, min = 0, max = 2)
    )
  ),
  ROCK_IRON(
    displayName = "Iron Ore Vein",
    maxHealth = 6,
    requiredTool = ToolType.PICKAXE,
    drops = listOf(
      ResourceDrop(ItemType.STONE, min = 1, max = 2),
      ResourceDrop(ItemType.IRON_ORE, min = 1, max = 3)
    )
  ),
  ROCK_COAL(
    displayName = "Coal Deposit",
    maxHealth = 5,
    requiredTool = ToolType.PICKAXE,
    drops = listOf(
      ResourceDrop(ItemType.STONE, min = 1, max = 2),
      ResourceDrop(ItemType.COAL, min = 2, max = 4)
    )
  ),
  ROCK_GOLD(
    displayName = "Gold Vein",
    maxHealth = 7,
    requiredTool = ToolType.PICKAXE,
    drops = listOf(
      ResourceDrop(ItemType.GOLD_NUGGET, min = 1, max = 2),
      ResourceDrop(ItemType.STONE, min = 1, max = 2)
    )
  ),
  BUSH_BERRY(
    displayName = "Wild Berry Bush",
    maxHealth = 1,
    requiredTool = ToolType.NONE,
    drops = listOf(
      ResourceDrop(ItemType.BERRIES, min = 2, max = 4),
      ResourceDrop(ItemType.SEEDS, min = 0, max = 2)
    )
  ),
  BUSH_FIBER(
    displayName = "Wild Hemp Bush",
    maxHealth = 1,
    requiredTool = ToolType.NONE,
    drops = listOf(
      ResourceDrop(ItemType.FIBER, min = 2, max = 4)
    )
  ),
  BUSH_HERB(
    displayName = "Medicinal Herb",
    maxHealth = 1,
    requiredTool = ToolType.NONE,
    drops = listOf(
      ResourceDrop(ItemType.HERBS, min = 1, max = 3)
    )
  ),
  MUSHROOMS(
    displayName = "Forest Mushrooms",
    maxHealth = 1,
    requiredTool = ToolType.NONE,
    drops = listOf(
      ResourceDrop(ItemType.MUSHROOM, min = 1, max = 3)
    )
  )
}

data class ResourceDrop(
  val itemType: ItemType,
  val min: Int,
  val max: Int
)

enum class ToolType {
  NONE,
  AXE,
  PICKAXE,
  WEAPON
}

data class WorldTile(
  val x: Int,
  val y: Int,
  val biome: BiomeType,
  val tileType: TileType,
  var resource: ResourceDeposit,
  var resourceHp: Int
) {
  val hasResource: Boolean
    get() = resource != ResourceDeposit.NONE && resourceHp > 0

  val isSolidObstacle: Boolean
    get() = (!tileType.isWalkable) || (hasResource && resource != ResourceDeposit.BUSH_BERRY && resource != ResourceDeposit.BUSH_FIBER && resource != ResourceDeposit.BUSH_HERB && resource != ResourceDeposit.MUSHROOMS)
}
