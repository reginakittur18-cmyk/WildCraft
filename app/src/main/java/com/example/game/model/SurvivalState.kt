package com.example.game.model

enum class WeatherType(val displayName: String, val tempModifier: Float, val thirstDrainModifier: Float) {
  SUNNY("Clear Skies", tempModifier = 2f, thirstDrainModifier = 1.0f),
  RAINY("Rainfall", tempModifier = -5f, thirstDrainModifier = 0.5f),
  FOGGY("Dense Mist", tempModifier = -2f, thirstDrainModifier = 0.8f),
  SNOWSTORM("Snowstorm", tempModifier = -12f, thirstDrainModifier = 0.9f)
}

data class SurvivalState(
  var health: Float = 100f,
  var maxHealth: Float = 100f,
  var hunger: Float = 90f,
  var maxHunger: Float = 100f,
  var thirst: Float = 90f,
  var maxThirst: Float = 100f,
  var warmth: Float = 22f, // in Celsius
  var stamina: Float = 100f,
  var maxStamina: Float = 100f,
  var timeOfDay: Float = 8.0f, // 8:00 AM start
  var dayCount: Int = 1,
  var weather: WeatherType = WeatherType.SUNNY,
  var weatherTimer: Float = 180f, // seconds until weather changes
  var isResting: Boolean = false,
  var isNearFire: Boolean = false,
  var isWet: Boolean = false,
  var wetTimer: Float = 0f
) {
  val isNight: Boolean
    get() = timeOfDay >= 20.0f || timeOfDay < 5.5f

  val isDusk: Boolean
    get() = timeOfDay >= 18.0f && timeOfDay < 20.0f

  val isDawn: Boolean
    get() = timeOfDay >= 5.5f && timeOfDay < 7.0f

  val ambientLightLevel: Float
    get() {
      return when {
        timeOfDay in 8.0f..17.0f -> 1.0f
        timeOfDay in 17.0f..20.0f -> 1.0f - ((timeOfDay - 17.0f) / 3.0f) * 0.80f
        timeOfDay in 20.0f..24.0f -> 0.20f
        timeOfDay in 0.0f..5.0f -> 0.20f
        else -> 0.20f + ((timeOfDay - 5.0f) / 3.0f) * 0.80f
      }
    }

  val isStarving: Boolean get() = hunger <= 0f
  val isDehydrated: Boolean get() = thirst <= 0f
  val isFreezing: Boolean get() = warmth < 0f
  val isOverheating: Boolean get() = warmth > 42f
  val isDead: Boolean get() = health <= 0f
}
