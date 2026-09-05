package com.example.game.model

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

class WorldGen(val seed: Long) {

  // Deterministic 2D value noise with cubic interpolation
  private fun hash2D(x: Int, y: Int, offsetSeed: Long): Float {
    var h = (x * 374761393L + y * 668265263L + seed + offsetSeed)
    h = (h xor (h shr 13)) * 1274126177L
    h = h xor (h shr 16)
    return ((h and 0x7fffffffL).toFloat() / 0x7fffffffL.toFloat())
  }

  private fun smoothNoise2D(x: Float, y: Float, offsetSeed: Long): Float {
    val ix = floor(x).toInt()
    val iy = floor(y).toInt()
    val fx = x - ix
    val fy = y - iy

    // Smoothstep interpolation
    val sx = fx * fx * (3f - 2f * fx)
    val sy = fy * fy * (3f - 2f * fy)

    val n00 = hash2D(ix, iy, offsetSeed)
    val n10 = hash2D(ix + 1, iy, offsetSeed)
    val n01 = hash2D(ix, iy + 1, offsetSeed)
    val n11 = hash2D(ix + 1, iy + 1, offsetSeed)

    val nx0 = n00 * (1f - sx) + n10 * sx
    val nx1 = n01 * (1f - sx) + n11 * sx

    return nx0 * (1f - sy) + nx1 * sy
  }

  private fun octaveNoise2D(x: Float, y: Float, octaves: Int, persistence: Float, scale: Float, offsetSeed: Long): Float {
    var total = 0f
    var frequency = scale
    var amplitude = 1f
    var maxValue = 0f

    for (i in 0 until octaves) {
      total += smoothNoise2D(x * frequency, y * frequency, offsetSeed + i * 9973L) * amplitude
      maxValue += amplitude
      amplitude *= persistence
      frequency *= 2f
    }
    return total / maxValue
  }

  fun getBiomeAt(tileX: Int, tileY: Int): BiomeType {
    val elev = octaveNoise2D(tileX.toFloat(), tileY.toFloat(), octaves = 3, persistence = 0.5f, scale = 0.025f, offsetSeed = 101L)
    val moist = octaveNoise2D(tileX.toFloat(), tileY.toFloat(), octaves = 2, persistence = 0.5f, scale = 0.018f, offsetSeed = 503L)
    val temp = octaveNoise2D(tileX.toFloat(), tileY.toFloat(), octaves = 2, persistence = 0.5f, scale = 0.015f, offsetSeed = 907L)

    return when {
      elev < 0.28f -> BiomeType.DEEP_OCEAN
      elev < 0.35f -> BiomeType.SHALLOW_WATER
      elev < 0.40f -> BiomeType.SAND_BEACH
      elev > 0.82f -> BiomeType.SNOW_PEAKS
      elev > 0.72f -> BiomeType.ROCKY_HIGHLAND
      // Inland biomes
      temp > 0.65f && moist < 0.38f -> BiomeType.DESERT
      temp < 0.32f -> BiomeType.PINE_TAIGA
      moist > 0.62f && temp > 0.45f -> BiomeType.SWAMP
      moist > 0.42f -> BiomeType.FOREST
      else -> BiomeType.PLAINS
    }
  }

  fun generateTile(tileX: Int, tileY: Int): WorldTile {
    val biome = getBiomeAt(tileX, tileY)
    val tileType = when (biome) {
      BiomeType.DEEP_OCEAN -> TileType.DEEP_WATER
      BiomeType.SHALLOW_WATER -> TileType.SHALLOW_WATER
      BiomeType.SAND_BEACH -> TileType.SAND
      BiomeType.DESERT -> TileType.SAND
      BiomeType.SNOW_PEAKS -> TileType.SNOW
      BiomeType.ROCKY_HIGHLAND -> TileType.ROCK_GROUND
      BiomeType.SWAMP -> TileType.SWAMP_MUD
      BiomeType.FOREST, BiomeType.PINE_TAIGA, BiomeType.PLAINS -> {
        val detail = hash2D(tileX, tileY, 331L)
        if (detail < 0.85f) TileType.GRASS else TileType.DIRT
      }
    }

    // Resource roll
    val resourceRoll = hash2D(tileX, tileY, 773L)
    val resourceTypeRoll = hash2D(tileX, tileY, 889L)

    var resource = ResourceDeposit.NONE

    // Prevent resources in water
    if (tileType != TileType.DEEP_WATER && tileType != TileType.SHALLOW_WATER) {
      when (biome) {
        BiomeType.FOREST -> {
          if (resourceRoll < 0.35f) {
            resource = if (resourceTypeRoll < 0.80f) ResourceDeposit.TREE_OAK else ResourceDeposit.TREE_PINE
          } else if (resourceRoll < 0.45f) {
            resource = when {
              resourceTypeRoll < 0.40f -> ResourceDeposit.BUSH_BERRY
              resourceTypeRoll < 0.70f -> ResourceDeposit.MUSHROOMS
              else -> ResourceDeposit.ROCK_STONE
            }
          }
        }
        BiomeType.PINE_TAIGA -> {
          if (resourceRoll < 0.32f) {
            resource = ResourceDeposit.TREE_PINE
          } else if (resourceRoll < 0.42f) {
            resource = when {
              resourceTypeRoll < 0.50f -> ResourceDeposit.ROCK_STONE
              resourceTypeRoll < 0.80f -> ResourceDeposit.ROCK_COAL
              else -> ResourceDeposit.ROCK_IRON
            }
          }
        }
        BiomeType.PLAINS -> {
          if (resourceRoll < 0.10f) {
            resource = ResourceDeposit.TREE_OAK
          } else if (resourceRoll < 0.28f) {
            resource = when {
              resourceTypeRoll < 0.45f -> ResourceDeposit.BUSH_BERRY
              resourceTypeRoll < 0.75f -> ResourceDeposit.BUSH_FIBER
              resourceTypeRoll < 0.90f -> ResourceDeposit.BUSH_HERB
              else -> ResourceDeposit.ROCK_STONE
            }
          }
        }
        BiomeType.DESERT -> {
          if (resourceRoll < 0.08f) {
            resource = ResourceDeposit.TREE_CACTUS
          } else if (resourceRoll < 0.18f) {
            resource = if (resourceTypeRoll < 0.70f) ResourceDeposit.ROCK_STONE else ResourceDeposit.ROCK_GOLD
          }
        }
        BiomeType.SAND_BEACH -> {
          if (resourceRoll < 0.07f) {
            resource = ResourceDeposit.TREE_PALM
          } else if (resourceRoll < 0.14f) {
            resource = ResourceDeposit.ROCK_STONE
          }
        }
        BiomeType.ROCKY_HIGHLAND -> {
          if (resourceRoll < 0.28f) {
            resource = when {
              resourceTypeRoll < 0.40f -> ResourceDeposit.ROCK_STONE
              resourceTypeRoll < 0.70f -> ResourceDeposit.ROCK_IRON
              resourceTypeRoll < 0.90f -> ResourceDeposit.ROCK_COAL
              else -> ResourceDeposit.ROCK_GOLD
            }
          }
        }
        BiomeType.SNOW_PEAKS -> {
          if (resourceRoll < 0.12f) {
            resource = ResourceDeposit.TREE_PINE
          } else if (resourceRoll < 0.26f) {
            resource = if (resourceTypeRoll < 0.60f) ResourceDeposit.ROCK_STONE else ResourceDeposit.ROCK_IRON
          }
        }
        BiomeType.SWAMP -> {
          if (resourceRoll < 0.20f) {
            resource = ResourceDeposit.TREE_OAK
          } else if (resourceRoll < 0.38f) {
            resource = when {
              resourceTypeRoll < 0.40f -> ResourceDeposit.BUSH_HERB
              resourceTypeRoll < 0.70f -> ResourceDeposit.MUSHROOMS
              else -> ResourceDeposit.BUSH_FIBER
            }
          }
        }
        else -> Unit
      }
    }

    return WorldTile(
      x = tileX,
      y = tileY,
      biome = biome,
      tileType = tileType,
      resource = resource,
      resourceHp = resource.maxHealth
    )
  }
}
