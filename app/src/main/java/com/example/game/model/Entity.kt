package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class WildlifeType(
  val displayName: String,
  val maxHp: Int,
  val moveSpeed: Float,
  val drops: List<ResourceDrop>
) {
  RABBIT(
    displayName = "Wild Hare",
    maxHp = 10,
    moveSpeed = 3.2f,
    drops = listOf(
      ResourceDrop(ItemType.RAW_MEAT, 1, 1),
      ResourceDrop(ItemType.LEATHER, 1, 2)
    )
  ),
  DEER(
    displayName = "Forest Stag",
    maxHp = 25,
    moveSpeed = 2.8f,
    drops = listOf(
      ResourceDrop(ItemType.RAW_MEAT, 2, 3),
      ResourceDrop(ItemType.LEATHER, 2, 4),
      ResourceDrop(ItemType.BONE, 1, 2)
    )
  ),
  SHEEP(
    displayName = "Mountain Sheep",
    maxHp = 18,
    moveSpeed = 1.8f,
    drops = listOf(
      ResourceDrop(ItemType.RAW_MEAT, 1, 2),
      ResourceDrop(ItemType.FIBER, 2, 4)
    )
  )
}

enum class MonsterType(
  val displayName: String,
  val maxHp: Int,
  val damage: Int,
  val moveSpeed: Float,
  val aggroRange: Float,
  val drops: List<ResourceDrop>,
  val spawnsAtNightOnly: Boolean = false,
  val preferredBiome: BiomeType? = null
) {
  WOLF(
    displayName = "Timber Wolf",
    maxHp = 30,
    damage = 12,
    moveSpeed = 2.4f,
    aggroRange = 220f,
    drops = listOf(
      ResourceDrop(ItemType.RAW_MEAT, 1, 2),
      ResourceDrop(ItemType.LEATHER, 2, 3),
      ResourceDrop(ItemType.BONE, 1, 2)
    ),
    spawnsAtNightOnly = false
  ),
  SKELETON(
    displayName = "Ancient Skeleton",
    maxHp = 45,
    damage = 16,
    moveSpeed = 1.6f,
    aggroRange = 260f,
    drops = listOf(
      ResourceDrop(ItemType.BONE, 2, 4),
      ResourceDrop(ItemType.IRON_ORE, 1, 2)
    ),
    spawnsAtNightOnly = true
  ),
  SCORPION(
    displayName = "Dune Scorpion",
    maxHp = 35,
    damage = 14,
    moveSpeed = 2.1f,
    aggroRange = 180f,
    drops = listOf(
      ResourceDrop(ItemType.BONE, 1, 2),
      ResourceDrop(ItemType.FIBER, 1, 3)
    ),
    preferredBiome = BiomeType.DESERT
  ),
  FROST_YETI(
    displayName = "Frost Yeti",
    maxHp = 80,
    damage = 25,
    moveSpeed = 1.3f,
    aggroRange = 240f,
    drops = listOf(
      ResourceDrop(ItemType.LEATHER, 4, 6),
      ResourceDrop(ItemType.RAW_MEAT, 3, 5),
      ResourceDrop(ItemType.BONE, 3, 5)
    ),
    preferredBiome = BiomeType.SNOW_PEAKS
  )
}

data class PlayerEntity(
  var x: Float,
  var y: Float,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var facingAngle: Float = 0f,
  var isWalking: Boolean = false,
  var walkCycleTime: Float = 0f,
  var isAttacking: Boolean = false,
  var attackTimer: Float = 0f,
  var attackCooldown: Float = 0f,
  var isDodging: Boolean = false,
  var dodgeTimer: Float = 0f,
  var dodgeCooldown: Float = 0f,
  var dodgeDirX: Float = 0f,
  var dodgeDirY: Float = 0f,
  var equippedWeapon: ItemType? = null,
  var equippedArmor: ItemType? = null,
  var equippedLight: ItemType? = null
)

enum class CreatureState {
  IDLE,
  WANDER,
  FLEE,
  CHASE,
  ATTACK
}

data class WildlifeEntity(
  val id: String,
  val type: WildlifeType,
  var x: Float,
  var y: Float,
  var hp: Int,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var facingAngle: Float = 0f,
  var state: CreatureState = CreatureState.IDLE,
  var stateTimer: Float = 0f,
  var hurtFlashTimer: Float = 0f
)

data class MonsterEntity(
  val id: String,
  val type: MonsterType,
  var x: Float,
  var y: Float,
  var hp: Int,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var facingAngle: Float = 0f,
  var state: CreatureState = CreatureState.IDLE,
  var stateTimer: Float = 0f,
  var attackCooldown: Float = 0f,
  var hurtFlashTimer: Float = 0f
)

data class DroppedItemEntity(
  val id: String,
  val item: ItemType,
  var count: Int,
  var x: Float,
  var y: Float,
  var pickupDelay: Float = 0.5f,
  var bobTimer: Float = 0f
)

data class PlacedStructureEntity(
  val id: String,
  val type: ItemType,
  val tileX: Int,
  val tileY: Int,
  var hp: Int,
  val maxHp: Int = 100,
  var fuelRemaining: Float = 400f,
  val storageItems: MutableList<ItemStack> = mutableListOf(),
  var farmGrowthProgress: Float = 0f
)

data class FloatingText(
  val id: String,
  val text: String,
  val color: Color,
  var x: Float,
  var y: Float,
  var lifetime: Float = 0.9f,
  val maxLifetime: Float = 0.9f
)

data class Particle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  val color: Color,
  val size: Float,
  var alpha: Float = 1f,
  var lifetime: Float = 0.6f,
  val maxLifetime: Float = 0.6f
)
