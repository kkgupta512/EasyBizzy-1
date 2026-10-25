package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

/**
 * EasyBizzy theme token provider for extended retail semantic elements.
 */
object EasyBizzyThemeTokens {
  val colors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

  val spacing: EasyBizzySpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalEasyBizzySpacing.current

  val shapes: Shapes
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes

  val typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.typography
}

/**
 * Primary Material 3 Theme for EasyBizzy.
 * Configures the Material 3 ColorScheme, Typography, and Shapes.
 */
@Composable
fun EasyBizzyTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Defaults to false to maintain EasyBizzy's distinct, accessible brand palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme: ColorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> EasyBizzyDarkColorScheme
    else -> EasyBizzyLightColorScheme
  }

  val extendedColors = if (darkTheme) {
    ExtendedColors(
      khataLena = KhataLenaGreenDark,
      khataLenaContainer = KhataLenaContainerDark,
      khataDena = KhataDenaRedDark,
      khataDenaContainer = KhataDenaContainerDark,
      cashAccent = CashAmberDark,
      cashAccentContainer = CashAmberContainerDark,
      dividerColor = OutlineVariantDark,
    )
  } else {
    ExtendedColors(
      khataLena = KhataLenaGreenLight,
      khataLenaContainer = KhataLenaContainerLight,
      khataDena = KhataDenaRedLight,
      khataDenaContainer = KhataDenaContainerLight,
      cashAccent = CashAmberLight,
      cashAccentContainer = CashAmberContainerLight,
      dividerColor = OutlineVariantLight,
    )
  }

  val spacing = EasyBizzySpacing()

  CompositionLocalProvider(
    LocalExtendedColors provides extendedColors,
    LocalEasyBizzySpacing provides spacing,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      shapes = Shapes,
      content = content,
    )
  }
}

/**
 * Backward compatibility alias for template references.
 */
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = EasyBizzyTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
