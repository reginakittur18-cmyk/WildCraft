package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ForestGreen80,
    secondary = EarthBrown80,
    tertiary = EmberAmber80,
    background = DarkWildernessBackground,
    surface = DarkWildernessSurface,
    onPrimary = Color(0xFF003914),
    onSecondary = Color(0xFF3B2923),
    onTertiary = Color(0xFF452B00),
    onBackground = Color(0xFFE1E8E1),
    onSurface = Color(0xFFE1E8E1)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ForestGreen40,
    secondary = EarthBrown40,
    tertiary = EmberAmber40,
    background = Color(0xFFF1F6F2),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF132219),
    onSurface = Color(0xFF132219)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For immersive game experience, default to the rich dark wilderness theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      else -> DarkColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
