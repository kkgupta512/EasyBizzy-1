package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// Primary Merchant Green / Teal (Trust, Positive Commerce, Brand)
// =========================================================================
val EasyBizzyTealPrimaryLight = Color(0xFF006A58)
val EasyBizzyTealOnPrimaryLight = Color(0xFFFFFFFF)
val EasyBizzyTealContainerLight = Color(0xFF8CF5D9)
val EasyBizzyTealOnContainerLight = Color(0xFF002019)

val EasyBizzyTealPrimaryDark = Color(0xFF6ED8BE)
val EasyBizzyTealOnPrimaryDark = Color(0xFF00382D)
val EasyBizzyTealContainerDark = Color(0xFF005142)
val EasyBizzyTealOnContainerDark = Color(0xFF8CF5D9)

// =========================================================================
// Secondary Indian Business Navy (Structure, Reliability, Reports)
// =========================================================================
val EasyBizzyNavySecondaryLight = Color(0xFF2C5E8A)
val EasyBizzyNavyOnSecondaryLight = Color(0xFFFFFFFF)
val EasyBizzyNavyContainerLight = Color(0xFFCCE5FF)
val EasyBizzyNavyOnContainerLight = Color(0xFF001D33)

val EasyBizzyNavySecondaryDark = Color(0xFF98CCF9)
val EasyBizzyNavyOnSecondaryDark = Color(0xFF003355)
val EasyBizzyNavyContainerDark = Color(0xFF0C4871)
val EasyBizzyNavyOnContainerDark = Color(0xFFCCE5FF)

// =========================================================================
// Tertiary Warm Ochre (Quick Alerts, Chillar, Highlights)
// =========================================================================
val EasyBizzyOchreTertiaryLight = Color(0xFF785900)
val EasyBizzyOchreOnTertiaryLight = Color(0xFFFFFFFF)
val EasyBizzyOchreContainerLight = Color(0xFFFFDF9B)
val EasyBizzyOchreOnTertiaryLightContainer = Color(0xFF251A00)

val EasyBizzyOchreTertiaryDark = Color(0xFFF3BF48)
val EasyBizzyOchreOnTertiaryDark = Color(0xFF3F2E00)
val EasyBizzyOchreContainerDark = Color(0xFF5B4300)
val EasyBizzyOchreOnTertiaryDarkContainer = Color(0xFFFFDF9B)

// =========================================================================
// Error & Settlement Warning
// =========================================================================
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// =========================================================================
// Neutral Surfaces & High-Contrast Backgrounds
// =========================================================================
val BackgroundLight = Color(0xFFF6FBF7)
val OnBackgroundLight = Color(0xFF181D1A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF181D1A)
val SurfaceVariantLight = Color(0xFFDAE5E0)
val OnSurfaceVariantLight = Color(0xFF3F4945)
val OutlineLight = Color(0xFF6F7975)
val OutlineVariantLight = Color(0xFFBFC9C4)

val BackgroundDark = Color(0xFF101513)
val OnBackgroundDark = Color(0xFFE0E3E0)
val SurfaceDark = Color(0xFF181D1B)
val OnSurfaceDark = Color(0xFFE0E3E0)
val SurfaceVariantDark = Color(0xFF3F4945)
val OnSurfaceVariantDark = Color(0xFFBFC9C4)
val OutlineDark = Color(0xFF89938E)
val OutlineVariantDark = Color(0xFF3F4945)

// =========================================================================
// Domain-Specific Semantic Ledger Colors (Khata Lena / Dena & Cash)
// =========================================================================
// Green for "Lena / You will get" (Customer dues / positive cash flow)
val KhataLenaGreenLight = Color(0xFF0A7E39)
val KhataLenaContainerLight = Color(0xFFD4F7DC)
val KhataLenaGreenDark = Color(0xFF65DF87)
val KhataLenaContainerDark = Color(0xFF005322)

// Red for "Dena / You will give" (Supplier dues / Udhar liabilities)
val KhataDenaRedLight = Color(0xFFC5221F)
val KhataDenaContainerLight = Color(0xFFFFDAD6)
val KhataDenaRedDark = Color(0xFFFFB4AB)
val KhataDenaContainerDark = Color(0xFF93000A)

// Amber for Cash Register & Drawer Balance
val CashAmberLight = Color(0xFFB45309)
val CashAmberContainerLight = Color(0xFFFEF3C7)
val CashAmberDark = Color(0xFFFBBF24)
val CashAmberContainerDark = Color(0xFF78350F)

/**
 * Standard Material 3 Light ColorScheme for EasyBizzy.
 */
val EasyBizzyLightColorScheme: ColorScheme = lightColorScheme(
  primary = EasyBizzyTealPrimaryLight,
  onPrimary = EasyBizzyTealOnPrimaryLight,
  primaryContainer = EasyBizzyTealContainerLight,
  onPrimaryContainer = EasyBizzyTealOnContainerLight,

  secondary = EasyBizzyNavySecondaryLight,
  onSecondary = EasyBizzyNavyOnSecondaryLight,
  secondaryContainer = EasyBizzyNavyContainerLight,
  onSecondaryContainer = EasyBizzyNavyOnContainerLight,

  tertiary = EasyBizzyOchreTertiaryLight,
  onTertiary = EasyBizzyOchreOnTertiaryLight,
  tertiaryContainer = EasyBizzyOchreContainerLight,
  onTertiaryContainer = EasyBizzyOchreOnTertiaryLightContainer,

  error = ErrorLight,
  onError = OnErrorLight,
  errorContainer = ErrorContainerLight,
  onErrorContainer = OnErrorContainerLight,

  background = BackgroundLight,
  onBackground = OnBackgroundLight,
  surface = SurfaceLight,
  onSurface = OnSurfaceLight,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = OnSurfaceVariantLight,
  outline = OutlineLight,
  outlineVariant = OutlineVariantLight,
)

/**
 * Standard Material 3 Dark ColorScheme for EasyBizzy.
 */
val EasyBizzyDarkColorScheme: ColorScheme = darkColorScheme(
  primary = EasyBizzyTealPrimaryDark,
  onPrimary = EasyBizzyTealOnPrimaryDark,
  primaryContainer = EasyBizzyTealContainerDark,
  onPrimaryContainer = EasyBizzyTealOnContainerDark,

  secondary = EasyBizzyNavySecondaryDark,
  onSecondary = EasyBizzyNavyOnSecondaryDark,
  secondaryContainer = EasyBizzyNavyContainerDark,
  onSecondaryContainer = EasyBizzyNavyOnContainerDark,

  tertiary = EasyBizzyOchreTertiaryDark,
  onTertiary = EasyBizzyOchreOnTertiaryDark,
  tertiaryContainer = EasyBizzyOchreContainerDark,
  onTertiaryContainer = EasyBizzyOchreOnTertiaryDarkContainer,

  error = ErrorDark,
  onError = OnErrorDark,
  errorContainer = ErrorContainerDark,
  onErrorContainer = OnErrorContainerDark,

  background = BackgroundDark,
  onBackground = OnBackgroundDark,
  surface = SurfaceDark,
  onSurface = OnSurfaceDark,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = OnSurfaceVariantDark,
  outline = OutlineDark,
  outlineVariant = OutlineVariantDark,
)

/**
 * Extended semantic colors for Indian retail ledger workflows.
 */
@Immutable
data class ExtendedColors(
  val khataLena: Color,
  val khataLenaContainer: Color,
  val khataDena: Color,
  val khataDenaContainer: Color,
  val cashAccent: Color,
  val cashAccentContainer: Color,
  val dividerColor: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
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
