package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class BiomeType(
  val displayName: String,
  val baseColor: Color,
  val secondaryColor: Color,
  val ambientTemp: Float, // in Celsius (-20 to 45)
  val moveSpeedMultiplier: Float,
  val treeDensity: Float,
  val rockDensity: Float,
  val forageDensity: Float
) {
  DEEP_OCEAN(
    displayName = "Deep Ocean",
    baseColor = Color(0xFF154360),
    secondaryColor = Color(0xFF1B4F72),
    ambientTemp = 18f,
    moveSpeedMultiplier = 0.35f,
    treeDensity = 0f,
    rockDensity = 0f,
    forageDensity = 0.02f
  ),
  SHALLOW_WATER(
    displayName = "Coastline",
    baseColor = Color(0xFF2980B9),
    secondaryColor = Color(0xFF3498DB),
    ambientTemp = 20f,
    moveSpeedMultiplier = 0.65f,
    treeDensity = 0f,
    rockDensity = 0.05f,
    forageDensity = 0.08f
  ),
  SAND_BEACH(
    displayName = "Sandy Shores",
    baseColor = Color(0xFFE5C17D),
    secondaryColor = Color(0xFFD8B268),
    ambientTemp = 26f,
    moveSpeedMultiplier = 0.95f,
    treeDensity = 0.08f,
    rockDensity = 0.15f,
    forageDensity = 0.12f
  ),
  PLAINS(
    displayName = "Verdant Meadows",
    baseColor = Color(0xFF5CA647),
    secondaryColor = Color(0xFF4E8E3C),
    ambientTemp = 22f,
    moveSpeedMultiplier = 1.0f,
    treeDensity = 0.12f,
    rockDensity = 0.10f,
    forageDensity = 0.35f
  ),
  FOREST(
    displayName = "Ancient Woodland",
    baseColor = Color(0xFF2E6B34),
    secondaryColor = Color(0xFF25572A),
    ambientTemp = 19f,
    moveSpeedMultiplier = 0.95f,
    treeDensity = 0.48f,
    rockDensity = 0.15f,
    forageDensity = 0.30f
  ),
  PINE_TAIGA(
    displayName = "Pine Taiga",
    baseColor = Color(0xFF1D4A3A),
    secondaryColor = Color(0xFF163B2E),
    ambientTemp = 8f,
    moveSpeedMultiplier = 0.92f,
    treeDensity = 0.42f,
    rockDensity = 0.22f,
    forageDensity = 0.15f
  ),
  SNOW_PEAKS(
    displayName = "Frozen Tundra",
    baseColor = Color(0xFFE2EBF0),
    secondaryColor = Color(0xFFCDD9E1),
    ambientTemp = -8f,
    moveSpeedMultiplier = 0.85f,
    treeDensity = 0.10f,
    rockDensity = 0.32f,
    forageDensity = 0.05f
  ),
  DESERT(
    displayName = "Arid Dunes",
    baseColor = Color(0xFFDBA858),
    secondaryColor = Color(0xFFC79545),
    ambientTemp = 38f,
    moveSpeedMultiplier = 0.90f,
    treeDensity = 0.04f,
    rockDensity = 0.20f,
    forageDensity = 0.06f
  ),
  SWAMP(
    displayName = "Murky Mire",
    baseColor = Color(0xFF3F4E36),
    secondaryColor = Color(0xFF33402B),
    ambientTemp = 24f,
    moveSpeedMultiplier = 0.75f,
    treeDensity = 0.28f,
    rockDensity = 0.08f,
    forageDensity = 0.38f
  ),
  ROCKY_HIGHLAND(
    displayName = "Craggy Highlands",
    baseColor = Color(0xFF6E787E),
    secondaryColor = Color(0xFF5B646A),
    ambientTemp = 12f,
    moveSpeedMultiplier = 0.90f,
    treeDensity = 0.08f,
    rockDensity = 0.50f,
    forageDensity = 0.10f
  )
}
