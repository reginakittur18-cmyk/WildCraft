package com.example.game.engine

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.game.model.*
import java.util.UUID
import kotlin.math.*

class GameEngine(private val context: Context) {
  companion object {
    const val TILE_SIZE = 48f
    const val CHUNK_SIZE = 16
  }

  private val storage = GameStorage(context)
  var worldSeed: Long = 42891L
    private set
  var worldGen: WorldGen = WorldGen(worldSeed)
    private set

  // World tile caches
  private val loadedChunks = mutableMapOf<Long, Array<WorldTile>>()
  val modifiedTiles = mutableMapOf<Long, WorldTile>()
  val exploredChunks = mutableSetOf<Long>()

  // Game state
  val player = PlayerEntity(x = 0f, y = 0f)
  val survival = SurvivalState()
  val inventory = mutableListOf<ItemStack>()
  val placedStructures = mutableListOf<PlacedStructureEntity>()

  // Entities
  val wildlife = mutableListOf<WildlifeEntity>()
  val monsters = mutableListOf<MonsterEntity>()
  val droppedItems = mutableListOf<DroppedItemEntity>()
  val floatingTexts = mutableListOf<FloatingText>()
  val particles = mutableListOf<Particle>()

  // Active interaction target
  var activeChest: PlacedStructureEntity? = null
  var isGameOver: Boolean = false

  private var autosaveTimer: Float = 0f
  private var mobSpawnTimer: Float = 2.0f

  init {
    loadOrInitGame()
  }

  fun loadOrInitGame() {
    if (storage.hasSave()) {
      worldSeed = storage.loadSeed(42891L)
      worldGen = WorldGen(worldSeed)

      val savedPlayer = storage.loadPlayer(0f, 0f)
      player.x = savedPlayer.x
      player.y = savedPlayer.y
      player.equippedWeapon = savedPlayer.equippedWeapon
      player.equippedArmor = savedPlayer.equippedArmor
      player.equippedLight = savedPlayer.equippedLight

      val savedSurvival = storage.loadSurvival()
      survival.health = savedSurvival.health
      survival.hunger = savedSurvival.hunger
      survival.thirst = savedSurvival.thirst
      survival.warmth = savedSurvival.warmth
      survival.stamina = savedSurvival.stamina
      survival.timeOfDay = savedSurvival.timeOfDay
      survival.dayCount = savedSurvival.dayCount
      survival.weather = savedSurvival.weather

      inventory.clear()
      inventory.addAll(storage.loadInventory())

      placedStructures.clear()
      placedStructures.addAll(storage.loadStructures())

      modifiedTiles.clear()
      modifiedTiles.putAll(storage.loadModifiedTiles())

      exploredChunks.clear()
      exploredChunks.addAll(storage.loadExploredChunks())
    } else {
      startNewWorld(worldSeed)
    }

    // Ensure spawn tile is walkable
    ensureSafePlayerSpawn()
  }

  fun startNewWorld(seed: Long) {
    worldSeed = seed
    worldGen = WorldGen(seed)
    loadedChunks.clear()
    modifiedTiles.clear()
    exploredChunks.clear()
    wildlife.clear()
    monsters.clear()
    droppedItems.clear()
    placedStructures.clear()
    floatingTexts.clear()
    particles.clear()
    activeChest = null
    isGameOver = false

    player.x = 0f
    player.y = 0f
    player.equippedWeapon = ItemType.WOODEN_CLUB
    player.equippedArmor = null
    player.equippedLight = ItemType.TORCH

    survival.health = 100f
    survival.hunger = 90f
    survival.thirst = 90f
    survival.warmth = 22f
    survival.stamina = 100f
    survival.timeOfDay = 8.0f
    survival.dayCount = 1
    survival.weather = WeatherType.SUNNY

    inventory.clear()
    inventory.addAll(storage.defaultStarterInventory())

    // Initial campfire near player
    placedStructures.add(
      PlacedStructureEntity(
        id = UUID.randomUUID().toString(),
        type = ItemType.CAMPFIRE,
        tileX = 2,
        tileY = 1,
        hp = 100,
        fuelRemaining = 500f
      )
    )

    ensureSafePlayerSpawn()
    storage.saveGame(worldSeed, player, survival, inventory, placedStructures, modifiedTiles, exploredChunks)
  }

  private fun ensureSafePlayerSpawn() {
    var tx = floor(player.x / TILE_SIZE).toInt()
    var ty = floor(player.y / TILE_SIZE).toInt()

    var foundSafe = false
    for (radius in 0..15) {
      for (dx in -radius..radius) {
        for (dy in -radius..radius) {
          val tile = getTile(tx + dx, ty + dy)
          if (tile.tileType.isWalkable && !tile.isSolidObstacle) {
            player.x = (tx + dx) * TILE_SIZE + TILE_SIZE / 2f
            player.y = (ty + dy) * TILE_SIZE + TILE_SIZE / 2f
            foundSafe = true
            break
          }
        }
        if (foundSafe) break
      }
      if (foundSafe) break
    }
  }

  fun chunkKey(cx: Int, cy: Int): Long {
    return (cx.toLong() shl 32) or (cy.toLong() and 0xFFFFFFFFL)
  }

  fun tileKey(tx: Int, ty: Int): Long {
    return (tx.toLong() shl 32) or (ty.toLong() and 0xFFFFFFFFL)
  }

  fun getTile(tx: Int, ty: Int): WorldTile {
    val tKey = tileKey(tx, ty)
    modifiedTiles[tKey]?.let { return it }

    val cx = floor(tx.toFloat() / CHUNK_SIZE).toInt()
    val cy = floor(ty.toFloat() / CHUNK_SIZE).toInt()
    val cKey = chunkKey(cx, cy)

    var chunk = loadedChunks[cKey]
    if (chunk == null) {
      chunk = generateChunk(cx, cy)
      loadedChunks[cKey] = chunk
      exploredChunks.add(cKey)
    }

    val localX = (tx % CHUNK_SIZE + CHUNK_SIZE) % CHUNK_SIZE
    val localY = (ty % CHUNK_SIZE + CHUNK_SIZE) % CHUNK_SIZE
    val index = localY * CHUNK_SIZE + localX

    return chunk[index]
  }

  private fun generateChunk(cx: Int, cy: Int): Array<WorldTile> {
    val tiles = Array(CHUNK_SIZE * CHUNK_SIZE) { i ->
      val lx = i % CHUNK_SIZE
      val ly = i / CHUNK_SIZE
      val wx = cx * CHUNK_SIZE + lx
      val wy = cy * CHUNK_SIZE + ly
      worldGen.generateTile(wx, wy)
    }
    return tiles
  }

  fun update(dt: Float, joyX: Float, joyY: Float) {
    if (isGameOver) return

    val clampedDt = dt.coerceIn(0.001f, 0.1f)

    updateSurvival(clampedDt)
    updatePlayerMovement(clampedDt, joyX, joyY)
    updateCombatAndTimers(clampedDt)
    updateMobs(clampedDt)
    updateStructures(clampedDt)
    updateDroppedItems(clampedDt)
    updateParticlesAndText(clampedDt)

    // Autosave every 12 seconds
    autosaveTimer += clampedDt
    if (autosaveTimer >= 12f) {
      autosaveTimer = 0f
      storage.saveGame(worldSeed, player, survival, inventory, placedStructures, modifiedTiles, exploredChunks)
    }
  }

  private fun updateSurvival(dt: Float) {
    // Clock progression: 1 in-game hour = 12 real seconds
    val hoursPassed = dt / 12f
    survival.timeOfDay = (survival.timeOfDay + hoursPassed) % 24.0f
    if (survival.timeOfDay < hoursPassed) {
      survival.dayCount++
      addFloatingText("Dawn of Day ${survival.dayCount}", Color(0xFFFFD54F), player.x, player.y - 40f)
    }

    // Weather change progression
    survival.weatherTimer -= dt
    if (survival.weatherTimer <= 0f) {
      survival.weatherTimer = (120..240).random().toFloat()
      val weathers = WeatherType.values()
      survival.weather = weathers.random()
      addFloatingText(survival.weather.displayName, Color(0xFF81D4FA), player.x, player.y - 50f)
    }

    // Biome and temperature calculation
    val pTx = floor(player.x / TILE_SIZE).toInt()
    val pTy = floor(player.y / TILE_SIZE).toInt()
    val currentTile = getTile(pTx, pTy)
    val baseTemp = currentTile.biome.ambientTemp

    // Time cooling: night is colder by 12C
    val nightCooling = if (survival.isNight) -12f else if (survival.isDusk) -5f else 0f
    val weatherCooling = survival.weather.tempModifier

    // Check proximity to campfires
    var nearFire = false
    for (struct in placedStructures) {
      if (struct.type == ItemType.CAMPFIRE && struct.fuelRemaining > 0f) {
        val sDist = hypot(player.x - (struct.tileX * TILE_SIZE + TILE_SIZE / 2f), player.y - (struct.tileY * TILE_SIZE + TILE_SIZE / 2f))
        if (sDist < 140f) {
          nearFire = true
          break
        }
      }
    }
    survival.isNearFire = nearFire
    val fireWarmth = if (nearFire) 26f else 0f
    val torchWarmth = if (player.equippedLight == ItemType.TORCH) 10f else 0f
    val furArmorWarmth = if (player.equippedArmor == ItemType.FUR_CLOAK) 24f else 0f

    val targetWarmth = (baseTemp + nightCooling + weatherCooling + fireWarmth + torchWarmth + furArmorWarmth).coerceIn(-25f, 45f)
    // Smooth warmth shift
    survival.warmth += (targetWarmth - survival.warmth) * (dt * 0.4f)

    // Hunger drain (100% lasts ~600 seconds = 10 mins)
    val hungerDrainRate = if (player.isWalking) 0.18f else 0.12f
    survival.hunger = (survival.hunger - hungerDrainRate * dt).coerceAtLeast(0f)

    // Thirst drain
    val heatFactor = if (survival.warmth > 32f) 1.8f else 1.0f
    val thirstDrainRate = (if (player.isWalking) 0.24f else 0.16f) * heatFactor * survival.weather.thirstDrainModifier
    survival.thirst = (survival.thirst - thirstDrainRate * dt).coerceAtLeast(0f)

    // Stamina recovery
    if (!player.isDodging && player.dodgeTimer <= 0f) {
      val staminaRecoveryRate = if (survival.hunger > 30f && survival.thirst > 30f) 22f else 10f
      survival.stamina = (survival.stamina + staminaRecoveryRate * dt).coerceAtMost(survival.maxStamina)
    }

    // Health damage or regen
    if (survival.isFreezing) {
      survival.health = (survival.health - 2.5f * dt).coerceAtLeast(0f)
      if (Math.random() < 0.05) {
        addFloatingText("Freezing!", Color(0xFF81D4FA), player.x, player.y - 20f)
      }
    }
    if (survival.isStarving) {
      survival.health = (survival.health - 2.0f * dt).coerceAtLeast(0f)
    }
    if (survival.isDehydrated) {
      survival.health = (survival.health - 2.5f * dt).coerceAtLeast(0f)
    }

    // Passive health regeneration when well fed and hydrated
    if (survival.hunger > 70f && survival.thirst > 70f && !survival.isFreezing && survival.health < survival.maxHealth) {
      survival.health = (survival.health + 1.2f * dt).coerceAtMost(survival.maxHealth)
    }

    if (survival.isDead && !isGameOver) {
      isGameOver = true
      addFloatingText("YOU DIED", Color(0xFFE53935), player.x, player.y - 30f)
    }
  }

  private fun updatePlayerMovement(dt: Float, joyX: Float, joyY: Float) {
    // Cooldowns
    if (player.dodgeCooldown > 0f) player.dodgeCooldown -= dt
    if (player.dodgeTimer > 0f) {
      player.dodgeTimer -= dt
      val dodgeSpeed = 280f
      val nextX = player.x + player.dodgeDirX * dodgeSpeed * dt
      val nextY = player.y + player.dodgeDirY * dodgeSpeed * dt
      if (!isCollidingAt(nextX, nextY)) {
        player.x = nextX
        player.y = nextY
      }
      if (player.dodgeTimer <= 0f) {
        player.isDodging = false
      }
      return
    }

    val inputMag = hypot(joyX, joyY)
    if (inputMag > 0.15f) {
      val dirX = joyX / inputMag
      val dirY = joyY / inputMag
      player.facingAngle = atan2(dirY, dirX)
      player.isWalking = true
      player.walkCycleTime += dt * 8f

      val pTx = floor(player.x / TILE_SIZE).toInt()
      val pTy = floor(player.y / TILE_SIZE).toInt()
      val currentTile = getTile(pTx, pTy)
      val biomeSpeed = currentTile.biome.moveSpeedMultiplier
      val tileSpeed = if (currentTile.tileType.slowsMovement) 0.70f else 1.0f

      val baseSpeed = 135f
      val speed = baseSpeed * biomeSpeed * tileSpeed * inputMag.coerceAtMost(1f)

      val targetVx = dirX * speed
      val targetVy = dirY * speed

      // Smooth acceleration
      player.vx += (targetVx - player.vx) * (dt * 15f)
      player.vy += (targetVy - player.vy) * (dt * 15f)

      // Try move with collision
      val nextX = player.x + player.vx * dt
      val nextY = player.y + player.vy * dt

      if (!isCollidingAt(nextX, player.y)) {
        player.x = nextX
      }
      if (!isCollidingAt(player.x, nextY)) {
        player.y = nextY
      }
    } else {
      player.isWalking = false
      player.vx *= (1f - dt * 10f)
      player.vy *= (1f - dt * 10f)
    }
  }

  private fun isCollidingAt(px: Float, py: Float): Boolean {
    val radius = 12f
    val minTx = floor((px - radius) / TILE_SIZE).toInt()
    val maxTx = floor((px + radius) / TILE_SIZE).toInt()
    val minTy = floor((py - radius) / TILE_SIZE).toInt()
    val maxTy = floor((py + radius) / TILE_SIZE).toInt()

    for (tx in minTx..maxTx) {
      for (ty in minTy..maxTy) {
        val tile = getTile(tx, ty)
        if (tile.isSolidObstacle) {
          val tileLeft = tx * TILE_SIZE
          val tileTop = ty * TILE_SIZE
          val closestX = px.coerceIn(tileLeft, tileLeft + TILE_SIZE)
          val closestY = py.coerceIn(tileTop, tileTop + TILE_SIZE)
          val distSq = (px - closestX) * (px - closestX) + (py - closestY) * (py - closestY)
          if (distSq < radius * radius) {
            return true
          }
        }
      }
    }

    // Check placed structures collision (e.g. walls)
    for (struct in placedStructures) {
      if (struct.type == ItemType.WOODEN_WALL) {
        val sLeft = struct.tileX * TILE_SIZE
        val sTop = struct.tileY * TILE_SIZE
        val closestX = px.coerceIn(sLeft, sLeft + TILE_SIZE)
        val closestY = py.coerceIn(sTop, sTop + TILE_SIZE)
        val distSq = (px - closestX) * (px - closestX) + (py - closestY) * (py - closestY)
        if (distSq < radius * radius) {
          return true
        }
      }
    }

    return false
  }

  private fun updateCombatAndTimers(dt: Float) {
    if (player.attackCooldown > 0f) player.attackCooldown -= dt
    if (player.isAttacking) {
      player.attackTimer += dt * 5f
      if (player.attackTimer >= 1f) {
        player.isAttacking = false
        player.attackTimer = 0f
      }
    }
  }

  fun onAttackAction() {
    if (player.attackCooldown > 0f || survival.stamina < 8f) return
    player.isAttacking = true
    player.attackTimer = 0f
    player.attackCooldown = 0.35f
    survival.stamina = (survival.stamina - 8f).coerceAtLeast(0f)

    val attackReach = 58f
    val aimX = player.x + cos(player.facingAngle) * attackReach
    val aimY = player.y + sin(player.facingAngle) * attackReach

    var hitSomething = false

    // Check monster hit
    for (mob in monsters) {
      val dist = hypot(mob.x - aimX, mob.y - aimY)
      if (dist < 38f) {
        val weaponDmg = player.equippedWeapon?.attackDamage ?: 6
        mob.hp -= weaponDmg
        mob.hurtFlashTimer = 0.25f
        mob.vx += cos(player.facingAngle) * 80f
        mob.vy += sin(player.facingAngle) * 80f
        addFloatingText("-$weaponDmg", Color(0xFFFF5252), mob.x, mob.y - 20f)
        spawnSparks(mob.x, mob.y, Color(0xFFFF8A80))
        hitSomething = true

        if (mob.hp <= 0) {
          dropMobLoot(mob.x, mob.y, mob.type.drops)
        }
        break
      }
    }

    // Check wildlife hit
    if (!hitSomething) {
      for (animal in wildlife) {
        val dist = hypot(animal.x - aimX, animal.y - aimY)
        if (dist < 36f) {
          val weaponDmg = player.equippedWeapon?.attackDamage ?: 6
          animal.hp -= weaponDmg
          animal.hurtFlashTimer = 0.25f
          animal.state = CreatureState.FLEE
          animal.stateTimer = 4.0f
          addFloatingText("-$weaponDmg", Color(0xFFFF5252), animal.x, animal.y - 20f)
          spawnSparks(animal.x, animal.y, Color(0xFFFFD180))
          hitSomething = true

          if (animal.hp <= 0) {
            dropMobLoot(animal.x, animal.y, animal.type.drops)
          }
          break
        }
      }
    }

    // Check resource hit (Tree / Rock)
    if (!hitSomething) {
      val targetTx = floor(aimX / TILE_SIZE).toInt()
      val targetTy = floor(aimY / TILE_SIZE).toInt()
      val targetTile = getTile(targetTx, targetTy)

      if (targetTile.hasResource) {
        val eqTool = player.equippedWeapon?.toolType ?: ToolType.NONE
        val toolMultiplier = if (eqTool == targetTile.resource.requiredTool) 2 else 1
        targetTile.resourceHp -= toolMultiplier

        val sparkColor = if (targetTile.resource.requiredTool == ToolType.AXE) Color(0xFF8D6E63) else Color(0xFFB0BEC5)
        spawnSparks(aimX, aimY, sparkColor)

        if (targetTile.resourceHp <= 0) {
          // Harvested resource
          addFloatingText("+${targetTile.resource.displayName}", Color(0xFF81C784), aimX, aimY - 20f)
          for (drop in targetTile.resource.drops) {
            val count = (drop.min..drop.max).random()
            if (count > 0) {
              addDroppedItem(drop.itemType, count, targetTx * TILE_SIZE + TILE_SIZE / 2f, targetTy * TILE_SIZE + TILE_SIZE / 2f)
            }
          }
          targetTile.resource = ResourceDeposit.NONE
          modifiedTiles[tileKey(targetTx, targetTy)] = targetTile
        } else {
          addFloatingText("-1 Hit", Color(0xFFFFF9C4), aimX, aimY - 20f)
          modifiedTiles[tileKey(targetTx, targetTy)] = targetTile
        }
        hitSomething = true
      }
    }

    // Slash particle arc
    spawnSlashParticle(aimX, aimY, player.facingAngle)
  }

  fun onInteractAction(): String? {
    val interactRadius = 60f

    // 1. Check nearby placed chest
    for (struct in placedStructures) {
      if (struct.type == ItemType.STORAGE_CHEST) {
        val dist = hypot(player.x - (struct.tileX * TILE_SIZE + TILE_SIZE / 2f), player.y - (struct.tileY * TILE_SIZE + TILE_SIZE / 2f))
        if (dist < interactRadius) {
          activeChest = struct
          return "Opened Chest"
        }
      }
      if (struct.type == ItemType.BEDROLL) {
        val dist = hypot(player.x - (struct.tileX * TILE_SIZE + TILE_SIZE / 2f), player.y - (struct.tileY * TILE_SIZE + TILE_SIZE / 2f))
        if (dist < interactRadius) {
          if (survival.isNight) {
            survival.timeOfDay = 6.0f
            survival.dayCount++
            survival.stamina = survival.maxStamina
            survival.warmth = 25f
            addFloatingText("Rested until dawn...", Color(0xFFFFD54F), player.x, player.y - 30f)
            return "Rested until dawn"
          } else {
            return "You can only sleep at night!"
          }
        }
      }
    }

    // 2. Check for bush forage (berries, fiber, herbs, mushrooms)
    val pTx = floor(player.x / TILE_SIZE).toInt()
    val pTy = floor(player.y / TILE_SIZE).toInt()
    for (dx in -1..1) {
      for (dy in -1..1) {
        val tile = getTile(pTx + dx, pTy + dy)
        if (tile.hasResource && (tile.resource == ResourceDeposit.BUSH_BERRY || tile.resource == ResourceDeposit.BUSH_FIBER || tile.resource == ResourceDeposit.BUSH_HERB || tile.resource == ResourceDeposit.MUSHROOMS)) {
          for (drop in tile.resource.drops) {
            val count = (drop.min..drop.max).random()
            if (count > 0) {
              addItemToInventory(drop.itemType, count)
              addFloatingText("+${count} ${drop.itemType.displayName}", Color(0xFF81C784), player.x, player.y - 25f)
            }
          }
          tile.resource = ResourceDeposit.NONE
          modifiedTiles[tileKey(pTx + dx, pTy + dy)] = tile
          return "Gathered plants"
        }
      }
    }

    // 3. Check for drinking fresh water
    for (dx in -1..1) {
      for (dy in -1..1) {
        val tile = getTile(pTx + dx, pTy + dy)
        if (tile.tileType == TileType.SHALLOW_WATER) {
          survival.thirst = (survival.thirst + 25f).coerceAtMost(survival.maxThirst)
          addFloatingText("+25 Thirst (Clean Water)", Color(0xFF4FC3F7), player.x, player.y - 25f)
          spawnSparks(player.x, player.y, Color(0xFF81D4FA))
          return "Drank water"
        }
      }
    }

    // 4. Pickup dropped items
    var pickedCount = 0
    val it = droppedItems.iterator()
    while (it.hasNext()) {
      val item = it.next()
      if (hypot(player.x - item.x, player.y - item.y) < interactRadius && item.pickupDelay <= 0f) {
        addItemToInventory(item.item, item.count)
        addFloatingText("+${item.count} ${item.item.displayName}", Color(0xFFAED581), player.x, player.y - 30f)
        it.remove()
        pickedCount++
      }
    }
    if (pickedCount > 0) return "Picked up items"

    return null
  }

  fun onDodgeAction() {
    if (player.dodgeCooldown > 0f || survival.stamina < 20f || player.isDodging) return
    survival.stamina = (survival.stamina - 20f).coerceAtLeast(0f)
    player.isDodging = true
    player.dodgeTimer = 0.22f
    player.dodgeCooldown = 0.8f

    if (hypot(player.vx, player.vy) > 10f) {
      val mag = hypot(player.vx, player.vy)
      player.dodgeDirX = player.vx / mag
      player.dodgeDirY = player.vy / mag
    } else {
      player.dodgeDirX = cos(player.facingAngle)
      player.dodgeDirY = sin(player.facingAngle)
    }

    // Spawn dust particles
    for (i in 0..5) {
      particles.add(
        Particle(
          x = player.x,
          y = player.y,
          vx = (Math.random().toFloat() - 0.5f) * 40f,
          vy = (Math.random().toFloat() - 0.5f) * 40f,
          color = Color(0xFFBCAAA4),
          size = 6f
        )
      )
    }
  }

  fun placeStructure(item: ItemType): Boolean {
    // Target 1 tile in front of player
    val targetTx = floor((player.x + cos(player.facingAngle) * TILE_SIZE) / TILE_SIZE).toInt()
    val targetTy = floor((player.y + sin(player.facingAngle) * TILE_SIZE) / TILE_SIZE).toInt()

    val tile = getTile(targetTx, targetTy)
    if (!tile.tileType.isWalkable || tile.hasResource) {
      addFloatingText("Cannot build here!", Color(0xFFFF8A80), player.x, player.y - 20f)
      return false
    }

    // Check if structure already there
    if (placedStructures.any { it.tileX == targetTx && it.tileY == targetTy }) {
      addFloatingText("Spot occupied!", Color(0xFFFF8A80), player.x, player.y - 20f)
      return false
    }

    if (consumeItemFromInventory(item, 1)) {
      placedStructures.add(
        PlacedStructureEntity(
          id = UUID.randomUUID().toString(),
          type = item,
          tileX = targetTx,
          tileY = targetTy,
          hp = 100,
          fuelRemaining = if (item == ItemType.CAMPFIRE) 500f else 0f
        )
      )
      addFloatingText("Constructed ${item.displayName}!", Color(0xFFFFD54F), targetTx * TILE_SIZE + TILE_SIZE / 2f, targetTy * TILE_SIZE)
      spawnSparks(targetTx * TILE_SIZE + TILE_SIZE / 2f, targetTy * TILE_SIZE + TILE_SIZE / 2f, Color(0xFFFFCA28))
      return true
    }
    return false
  }

  fun craftRecipe(recipe: CraftingRecipe): Boolean {
    // Check ingredients
    for (ing in recipe.ingredients) {
      val total = inventory.filter { it.item == ing.item }.sumOf { it.count }
      if (total < ing.amount) {
        addFloatingText("Missing materials!", Color(0xFFFF8A80), player.x, player.y - 20f)
        return false
      }
    }

    // Consume
    for (ing in recipe.ingredients) {
      consumeItemFromInventory(ing.item, ing.amount)
    }

    // Add result
    addItemToInventory(recipe.result, recipe.resultCount)
    addFloatingText("Crafted ${recipe.result.displayName}!", Color(0xFF81C784), player.x, player.y - 30f)
    return true
  }

  fun useItem(item: ItemType): Boolean {
    when (item.category) {
      ItemCategory.CONSUMABLE -> {
        if (consumeItemFromInventory(item, 1)) {
          survival.hunger = (survival.hunger + item.hungerRestore).coerceIn(0f, survival.maxHunger)
          survival.thirst = (survival.thirst + item.thirstRestore).coerceIn(0f, survival.maxThirst)
          survival.health = (survival.health + item.healthRestore).coerceIn(0f, survival.maxHealth)
          survival.warmth = (survival.warmth + item.warmthRestore).coerceIn(-30f, 50f)
          survival.stamina = (survival.stamina + item.staminaRestore).coerceIn(0f, survival.maxStamina)
          addFloatingText("Used ${item.displayName}", Color(0xFFAED581), player.x, player.y - 20f)
          return true
        }
      }
      ItemCategory.TOOL, ItemCategory.WEAPON -> {
        player.equippedWeapon = if (player.equippedWeapon == item) null else item
        addFloatingText(if (player.equippedWeapon == item) "Equipped ${item.displayName}" else "Unequipped", Color(0xFFFFF176), player.x, player.y - 20f)
        return true
      }
      ItemCategory.ARMOR -> {
        player.equippedArmor = if (player.equippedArmor == item) null else item
        addFloatingText(if (player.equippedArmor == item) "Equipped ${item.displayName}" else "Unequipped", Color(0xFFFFF176), player.x, player.y - 20f)
        return true
      }
      ItemCategory.STRUCTURE -> {
        return placeStructure(item)
      }
      else -> Unit
    }
    return false
  }

  fun addItemToInventory(item: ItemType, count: Int) {
    val existing = inventory.firstOrNull { it.item == item && it.count < item.maxStack }
    if (existing != null) {
      val canAdd = (item.maxStack - existing.count).coerceAtMost(count)
      existing.count += canAdd
      val remainder = count - canAdd
      if (remainder > 0) {
        inventory.add(ItemStack(item, remainder))
      }
    } else {
      inventory.add(ItemStack(item, count))
    }
  }

  fun consumeItemFromInventory(item: ItemType, count: Int): Boolean {
    var remaining = count
    val it = inventory.iterator()
    while (it.hasNext() && remaining > 0) {
      val stack = it.next()
      if (stack.item == item) {
        if (stack.count <= remaining) {
          remaining -= stack.count
          it.remove()
        } else {
          stack.count -= remaining
          remaining = 0
        }
      }
    }
    return remaining == 0
  }

  fun addDroppedItem(item: ItemType, count: Int, x: Float, y: Float) {
    droppedItems.add(
      DroppedItemEntity(
        id = UUID.randomUUID().toString(),
        item = item,
        count = count,
        x = x + (Math.random().toFloat() - 0.5f) * 16f,
        y = y + (Math.random().toFloat() - 0.5f) * 16f
      )
    )
  }

  private fun dropMobLoot(x: Float, y: Float, drops: List<ResourceDrop>) {
    for (drop in drops) {
      val count = (drop.min..drop.max).random()
      if (count > 0) {
        addDroppedItem(drop.itemType, count, x, y)
      }
    }
  }

  private fun updateMobs(dt: Float) {
    // Mob spawning around player
    mobSpawnTimer -= dt
    if (mobSpawnTimer <= 0f) {
      mobSpawnTimer = 3.5f
      spawnNearbyMobs()
    }

    // Update wildlife
    val wIt = wildlife.iterator()
    while (wIt.hasNext()) {
      val animal = wIt.next()
      if (animal.hp <= 0) {
        wIt.remove()
        continue
      }
      if (animal.hurtFlashTimer > 0f) animal.hurtFlashTimer -= dt

      val distToPlayer = hypot(player.x - animal.x, player.y - animal.y)
      if (distToPlayer > 1000f) {
        wIt.remove()
        continue
      }

      animal.stateTimer -= dt
      if (distToPlayer < 110f) {
        animal.state = CreatureState.FLEE
        animal.stateTimer = 3.0f
      }

      when (animal.state) {
        CreatureState.IDLE -> {
          if (animal.stateTimer <= 0f) {
            animal.state = CreatureState.WANDER
            animal.stateTimer = (2..5).random().toFloat()
            val angle = (Math.random() * 2 * PI).toFloat()
            animal.vx = cos(angle) * animal.type.moveSpeed * 30f
            animal.vy = sin(angle) * animal.type.moveSpeed * 30f
            animal.facingAngle = angle
          }
        }
        CreatureState.WANDER -> {
          val nextX = animal.x + animal.vx * dt
          val nextY = animal.y + animal.vy * dt
          if (!isCollidingAt(nextX, nextY)) {
            animal.x = nextX
            animal.y = nextY
          } else {
            animal.vx = -animal.vx
            animal.vy = -animal.vy
          }
          if (animal.stateTimer <= 0f) {
            animal.state = CreatureState.IDLE
            animal.stateTimer = (1..3).random().toFloat()
            animal.vx = 0f
            animal.vy = 0f
          }
        }
        CreatureState.FLEE -> {
          val fleeAngle = atan2(animal.y - player.y, animal.x - player.x)
          animal.vx = cos(fleeAngle) * animal.type.moveSpeed * 48f
          animal.vy = sin(fleeAngle) * animal.type.moveSpeed * 48f
          animal.facingAngle = fleeAngle
          val nextX = animal.x + animal.vx * dt
          val nextY = animal.y + animal.vy * dt
          if (!isCollidingAt(nextX, nextY)) {
            animal.x = nextX
            animal.y = nextY
          }
          if (animal.stateTimer <= 0f && distToPlayer > 200f) {
            animal.state = CreatureState.IDLE
            animal.stateTimer = 2.0f
          }
        }
        else -> Unit
      }
    }

    // Update monsters
    val mIt = monsters.iterator()
    while (mIt.hasNext()) {
      val mob = mIt.next()
      if (mob.hp <= 0) {
        mIt.remove()
        continue
      }
      if (mob.hurtFlashTimer > 0f) mob.hurtFlashTimer -= dt
      if (mob.attackCooldown > 0f) mob.attackCooldown -= dt

      val distToPlayer = hypot(player.x - mob.x, player.y - mob.y)
      if (distToPlayer > 1100f) {
        mIt.remove()
        continue
      }

      // Aggro logic
      if (distToPlayer < mob.type.aggroRange) {
        mob.state = CreatureState.CHASE
      }

      when (mob.state) {
        CreatureState.IDLE -> {
          mob.stateTimer -= dt
          if (mob.stateTimer <= 0f) {
            mob.state = CreatureState.WANDER
            mob.stateTimer = (2..4).random().toFloat()
            val angle = (Math.random() * 2 * PI).toFloat()
            mob.vx = cos(angle) * mob.type.moveSpeed * 24f
            mob.vy = sin(angle) * mob.type.moveSpeed * 24f
            mob.facingAngle = angle
          }
        }
        CreatureState.WANDER -> {
          mob.stateTimer -= dt
          val nextX = mob.x + mob.vx * dt
          val nextY = mob.y + mob.vy * dt
          if (!isCollidingAt(nextX, nextY)) {
            mob.x = nextX
            mob.y = nextY
          }
          if (mob.stateTimer <= 0f) {
            mob.state = CreatureState.IDLE
            mob.stateTimer = (1..3).random().toFloat()
            mob.vx = 0f
            mob.vy = 0f
          }
        }
        CreatureState.CHASE -> {
          val chaseAngle = atan2(player.y - mob.y, player.x - mob.x)
          mob.facingAngle = chaseAngle
          val speed = mob.type.moveSpeed * 42f
          mob.vx = cos(chaseAngle) * speed
          mob.vy = sin(chaseAngle) * speed

          val nextX = mob.x + mob.vx * dt
          val nextY = mob.y + mob.vy * dt
          if (!isCollidingAt(nextX, nextY)) {
            mob.x = nextX
            mob.y = nextY
          }

          // Attack player
          if (distToPlayer < 32f && mob.attackCooldown <= 0f && !player.isDodging) {
            mob.attackCooldown = 1.2f
            val armorValue = player.equippedArmor?.armorValue ?: 0
            val reducedDamage = (mob.type.damage * (1f - (armorValue / 100f).coerceAtMost(0.75f))).roundToInt().coerceAtLeast(3)
            survival.health = (survival.health - reducedDamage).coerceAtLeast(0f)
            addFloatingText("-$reducedDamage", Color(0xFFFF1744), player.x, player.y - 30f)
            spawnSparks(player.x, player.y, Color(0xFFFF5252))
          }

          if (distToPlayer > mob.type.aggroRange * 1.5f) {
            mob.state = CreatureState.IDLE
            mob.stateTimer = 2.0f
          }
        }
        else -> Unit
      }
    }
  }

  private fun spawnNearbyMobs() {
    if (wildlife.size < 6) {
      val angle = (Math.random() * 2 * PI).toFloat()
      val dist = (350..700).random().toFloat()
      val spawnX = player.x + cos(angle) * dist
      val spawnY = player.y + sin(angle) * dist
      if (!isCollidingAt(spawnX, spawnY)) {
        val types = WildlifeType.values()
        val type = types.random()
        wildlife.add(
          WildlifeEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            x = spawnX,
            y = spawnY,
            hp = type.maxHp
          )
        )
      }
    }

    val maxMonsters = if (survival.isNight) 7 else 3
    if (monsters.size < maxMonsters) {
      val angle = (Math.random() * 2 * PI).toFloat()
      val dist = (400..750).random().toFloat()
      val spawnX = player.x + cos(angle) * dist
      val spawnY = player.y + sin(angle) * dist
      if (!isCollidingAt(spawnX, spawnY)) {
        val type = if (survival.isNight && Math.random() < 0.6) {
          MonsterType.SKELETON
        } else {
          MonsterType.WOLF
        }
        monsters.add(
          MonsterEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            x = spawnX,
            y = spawnY,
            hp = type.maxHp
          )
        )
      }
    }
  }

  private fun updateStructures(dt: Float) {
    val it = placedStructures.iterator()
    while (it.hasNext()) {
      val struct = it.next()
      if (struct.type == ItemType.CAMPFIRE) {
        if (struct.fuelRemaining > 0f) {
          struct.fuelRemaining -= dt
          // Fire spark particles
          if (Math.random() < 0.15) {
            particles.add(
              Particle(
                x = struct.tileX * TILE_SIZE + TILE_SIZE / 2f + (Math.random().toFloat() - 0.5f) * 16f,
                y = struct.tileY * TILE_SIZE + TILE_SIZE / 2f - 6f,
                vx = (Math.random().toFloat() - 0.5f) * 10f,
                vy = -20f - Math.random().toFloat() * 20f,
                color = if (Math.random() < 0.5) Color(0xFFFF6D00) else Color(0xFFFFD600),
                size = 4f,
                lifetime = 0.5f,
                maxLifetime = 0.5f
              )
            )
          }
        }
      }
      if (struct.type == ItemType.FARM_PLOT) {
        struct.farmGrowthProgress += dt / 120f // grows every 2 minutes
        if (struct.farmGrowthProgress >= 1f) {
          struct.farmGrowthProgress = 0f
          addDroppedItem(ItemType.BERRIES, (2..3).random(), struct.tileX * TILE_SIZE + TILE_SIZE / 2f, struct.tileY * TILE_SIZE + TILE_SIZE / 2f)
          addFloatingText("Crops Harvested!", Color(0xFF81C784), struct.tileX * TILE_SIZE + TILE_SIZE / 2f, struct.tileY * TILE_SIZE)
        }
      }
    }
  }

  private fun updateDroppedItems(dt: Float) {
    for (item in droppedItems) {
      if (item.pickupDelay > 0f) item.pickupDelay -= dt
      item.bobTimer += dt * 3f

      // Magnetic pull when player is close
      val dist = hypot(player.x - item.x, player.y - item.y)
      if (dist < 48f && item.pickupDelay <= 0f) {
        val angle = atan2(player.y - item.y, player.x - item.x)
        item.x += cos(angle) * 120f * dt
        item.y += sin(angle) * 120f * dt
      }
    }
  }

  private fun updateParticlesAndText(dt: Float) {
    val pIt = particles.iterator()
    while (pIt.hasNext()) {
      val p = pIt.next()
      p.lifetime -= dt
      p.x += p.vx * dt
      p.y += p.vy * dt
      p.alpha = (p.lifetime / p.maxLifetime).coerceIn(0f, 1f)
      if (p.lifetime <= 0f) pIt.remove()
    }

    val tIt = floatingTexts.iterator()
    while (tIt.hasNext()) {
      val t = tIt.next()
      t.lifetime -= dt
      t.y -= dt * 25f
      if (t.lifetime <= 0f) tIt.remove()
    }
  }

  fun addFloatingText(text: String, color: Color, x: Float, y: Float) {
    floatingTexts.add(FloatingText(UUID.randomUUID().toString(), text, color, x, y))
  }

  private fun spawnSparks(x: Float, y: Float, color: Color) {
    for (i in 0..6) {
      val angle = (Math.random() * 2 * PI).toFloat()
      val speed = (30..80).random().toFloat()
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          color = color,
          size = (3..5).random().toFloat(),
          lifetime = 0.4f,
          maxLifetime = 0.4f
        )
      )
    }
  }

  private fun spawnSlashParticle(x: Float, y: Float, angle: Float) {
    for (i in 0..4) {
      val offset = (i - 2) * 8f
      particles.add(
        Particle(
          x = x + cos(angle + PI.toFloat() / 2f) * offset,
          y = y + sin(angle + PI.toFloat() / 2f) * offset,
          vx = cos(angle) * 50f,
          vy = sin(angle) * 50f,
          color = Color(0xFFFFFFFF),
          size = 3.5f,
          lifetime = 0.2f,
          maxLifetime = 0.2f
        )
      )
    }
  }

  fun respawnPlayer() {
    isGameOver = false
    survival.health = 80f
    survival.hunger = 70f
    survival.thirst = 70f
    survival.warmth = 22f
    survival.stamina = 100f
    ensureSafePlayerSpawn()
    addFloatingText("Reborn", Color(0xFF81C784), player.x, player.y - 30f)
  }
}
