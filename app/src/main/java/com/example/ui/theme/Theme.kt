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
    primary = AccentTeal,
    secondary = AmberGlow,
    tertiary = SanaaTerracotta,
    background = NightIndigo,
    surface = NightSurface,
    onPrimary = Color(0xFF0C131A),
    onSecondary = Color(0xFF0C131A),
    onBackground = GypsumWhite,
    onSurface = GypsumWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = YamanIndigo,
    secondary = SanaaTerracotta,
    tertiary = YemeniAmber,
    background = SandBackground,
    surface = GypsumWhite,
    onPrimary = GypsumWhite,
    onSecondary = GypsumWhite,
    onBackground = YamanIndigo,
    onSurface = YamanIndigo
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
