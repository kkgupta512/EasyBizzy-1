package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard 8dp grid spacing system optimized for rapid shopkeeper interaction.
 */
@Immutable
data class EasyBizzySpacing(
  val xxs: Dp = 2.dp,
  val xs: Dp = 4.dp,
  val sm: Dp = 8.dp,
  val md: Dp = 16.dp,
  val lg: Dp = 24.dp,
  val xl: Dp = 32.dp,
  val xxl: Dp = 48.dp,
  
  // Touch Targets (minimum 48dp for accessibility, 56dp for primary counter buttons)
  val minTouchTarget: Dp = 48.dp,
  val counterButtonHeight: Dp = 56.dp,
  val inputFieldHeight: Dp = 56.dp,
  
  // Card and dialog corner radius
  val cardRadius: Dp = 16.dp,
  val chipRadius: Dp = 8.dp,
  val buttonRadius: Dp = 12.dp,
  val dialogRadius: Dp = 20.dp,
)

val LocalEasyBizzySpacing = staticCompositionLocalOf { EasyBizzySpacing() }
