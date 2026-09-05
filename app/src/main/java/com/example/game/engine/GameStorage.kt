package com.example.game.engine

import android.content.Context
import com.example.game.model.*
import org.json.JSONArray
import org.json.JSONObject

class GameStorage(private val context: Context) {
  private val prefs = context.getSharedPreferences("wildcraft_save_data", Context.MODE_PRIVATE)

  fun saveGame(
    seed: Long,
    player: PlayerEntity,
    survival: SurvivalState,
    inventory: List<ItemStack>,
    placedStructures: List<PlacedStructureEntity>,
    modifiedTiles: Map<Long, WorldTile>,
    exploredChunks: Set<Long>
  ) {
    val editor = prefs.edit()

    editor.putLong("seed", seed)
    editor.putFloat("player_x", player.x)
    editor.putFloat("player_y", player.y)
    editor.putString("equipped_weapon", player.equippedWeapon?.name)
    editor.putString("equipped_armor", player.equippedArmor?.name)
    editor.putString("equipped_light", player.equippedLight?.name)

    editor.putFloat("health", survival.health)
    editor.putFloat("hunger", survival.hunger)
    editor.putFloat("thirst", survival.thirst)
    editor.putFloat("warmth", survival.warmth)
    editor.putFloat("stamina", survival.stamina)
    editor.putFloat("time_of_day", survival.timeOfDay)
    editor.putInt("day_count", survival.dayCount)
    editor.putString("weather", survival.weather.name)

    // Inventory JSON
    val invArray = JSONArray()
    for (stack in inventory) {
      val obj = JSONObject()
      obj.put("item", stack.item.name)
      obj.put("count", stack.count)
      invArray.put(obj)
    }
    editor.putString("inventory_json", invArray.toString())

    // Placed structures JSON
    val structArray = JSONArray()
    for (struct in placedStructures) {
      val obj = JSONObject()
      obj.put("id", struct.id)
      obj.put("type", struct.type.name)
      obj.put("tx", struct.tileX)
      obj.put("ty", struct.tileY)
      obj.put("hp", struct.hp)
      obj.put("fuel", struct.fuelRemaining.toDouble())

      val chestArray = JSONArray()
      for (item in struct.storageItems) {
        val sObj = JSONObject()
        sObj.put("item", item.item.name)
        sObj.put("count", item.count)
        chestArray.put(sObj)
      }
      obj.put("storage", chestArray)
      structArray.put(obj)
    }
    editor.putString("structures_json", structArray.toString())

    // Modified tiles JSON (stores up to recent 500 modifications)
    val tileArray = JSONArray()
    for ((key, tile) in modifiedTiles.entries.take(500)) {
      val obj = JSONObject()
      obj.put("key", key)
      obj.put("tx", tile.x)
      obj.put("ty", tile.y)
      obj.put("biome", tile.biome.name)
      obj.put("tileType", tile.tileType.name)
      obj.put("resource", tile.resource.name)
      obj.put("resourceHp", tile.resourceHp)
      tileArray.put(obj)
    }
    editor.putString("modified_tiles_json", tileArray.toString())

    // Explored chunks
    val chunksArray = JSONArray()
    for (chunk in exploredChunks.take(300)) {
      chunksArray.put(chunk)
    }
    editor.putString("explored_chunks_json", chunksArray.toString())

    editor.putBoolean("has_save", true)
    editor.apply()
  }

  fun hasSave(): Boolean = prefs.getBoolean("has_save", false)

  fun loadSeed(defaultSeed: Long): Long = prefs.getLong("seed", defaultSeed)

  fun loadPlayer(defaultX: Float, defaultY: Float): PlayerEntity {
    val x = prefs.getFloat("player_x", defaultX)
    val y = prefs.getFloat("player_y", defaultY)
    val eqWeapon = prefs.getString("equipped_weapon", null)?.let { runCatching { ItemType.valueOf(it) }.getOrNull() }
    val eqArmor = prefs.getString("equipped_armor", null)?.let { runCatching { ItemType.valueOf(it) }.getOrNull() }
    val eqLight = prefs.getString("equipped_light", null)?.let { runCatching { ItemType.valueOf(it) }.getOrNull() }

    return PlayerEntity(
      x = x,
      y = y,
      equippedWeapon = eqWeapon,
      equippedArmor = eqArmor,
      equippedLight = eqLight
    )
  }

  fun loadSurvival(): SurvivalState {
    val health = prefs.getFloat("health", 100f)
    val hunger = prefs.getFloat("hunger", 90f)
    val thirst = prefs.getFloat("thirst", 90f)
    val warmth = prefs.getFloat("warmth", 22f)
    val stamina = prefs.getFloat("stamina", 100f)
    val timeOfDay = prefs.getFloat("time_of_day", 8.0f)
    val dayCount = prefs.getInt("day_count", 1)
    val weatherName = prefs.getString("weather", WeatherType.SUNNY.name) ?: WeatherType.SUNNY.name
    val weather = runCatching { WeatherType.valueOf(weatherName) }.getOrDefault(WeatherType.SUNNY)

    return SurvivalState(
      health = health,
      hunger = hunger,
      thirst = thirst,
      warmth = warmth,
      stamina = stamina,
      timeOfDay = timeOfDay,
      dayCount = dayCount,
      weather = weather
    )
  }

  fun loadInventory(): MutableList<ItemStack> {
    val list = mutableListOf<ItemStack>()
    val jsonStr = prefs.getString("inventory_json", null) ?: return defaultStarterInventory()
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val item = runCatching { ItemType.valueOf(obj.getString("item")) }.getOrNull()
        val count = obj.getInt("count")
        if (item != null && count > 0) {
          list.add(ItemStack(item, count))
        }
      }
    } catch (_: Exception) {
      return defaultStarterInventory()
    }
    return if (list.isEmpty()) defaultStarterInventory() else list
  }

  fun defaultStarterInventory(): MutableList<ItemStack> {
    return mutableListOf(
      ItemStack(ItemType.WOODEN_CLUB, 1),
      ItemStack(ItemType.BERRIES, 6),
      ItemStack(ItemType.WOOD, 4),
      ItemStack(ItemType.STONE, 3),
      ItemStack(ItemType.TORCH, 2)
    )
  }

  fun loadStructures(): MutableList<PlacedStructureEntity> {
    val list = mutableListOf<PlacedStructureEntity>()
    val jsonStr = prefs.getString("structures_json", null) ?: return list
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val id = obj.getString("id")
        val type = runCatching { ItemType.valueOf(obj.getString("type")) }.getOrNull() ?: continue
        val tx = obj.getInt("tx")
        val ty = obj.getInt("ty")
        val hp = obj.getInt("hp")
        val fuel = obj.optDouble("fuel", 300.0).toFloat()
        val storageList = mutableListOf<ItemStack>()

        if (obj.has("storage")) {
          val sArray = obj.getJSONArray("storage")
          for (j in 0 until sArray.length()) {
            val sObj = sArray.getJSONObject(j)
            val sItem = runCatching { ItemType.valueOf(sObj.getString("item")) }.getOrNull()
            val sCount = sObj.getInt("count")
            if (sItem != null && sCount > 0) {
              storageList.add(ItemStack(sItem, sCount))
            }
          }
        }

        list.add(
          PlacedStructureEntity(
            id = id,
            type = type,
            tileX = tx,
            tileY = ty,
            hp = hp,
            fuelRemaining = fuel,
            storageItems = storageList
          )
        )
      }
    } catch (_: Exception) {}
    return list
  }

  fun loadModifiedTiles(): MutableMap<Long, WorldTile> {
    val map = mutableMapOf<Long, WorldTile>()
    val jsonStr = prefs.getString("modified_tiles_json", null) ?: return map
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val key = obj.getLong("key")
        val tx = obj.getInt("tx")
        val ty = obj.getInt("ty")
        val biome = runCatching { BiomeType.valueOf(obj.getString("biome")) }.getOrDefault(BiomeType.PLAINS)
        val tileType = runCatching { TileType.valueOf(obj.getString("tileType")) }.getOrDefault(TileType.GRASS)
        val resource = runCatching { ResourceDeposit.valueOf(obj.getString("resource")) }.getOrDefault(ResourceDeposit.NONE)
        val resourceHp = obj.getInt("resourceHp")

        map[key] = WorldTile(tx, ty, biome, tileType, resource, resourceHp)
      }
    } catch (_: Exception) {}
    return map
  }

  fun loadExploredChunks(): MutableSet<Long> {
    val set = mutableSetOf<Long>()
    val jsonStr = prefs.getString("explored_chunks_json", null) ?: return set
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        set.add(array.getLong(i))
      }
    } catch (_: Exception) {}
    return set
  }

  fun clearSave() {
    prefs.edit().clear().apply()
  }
}
