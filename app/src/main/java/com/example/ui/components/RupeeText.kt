package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EasyBizzyThemeTokens
import java.text.NumberFormat
import java.util.Locale

enum class RupeeColorStyle {
  NEUTRAL,
  LENA_POSITIVE,  // Green (Customer owes money to merchant)
  DENA_NEGATIVE,  // Red (Merchant owes money to supplier)
  CASH_ACCENT,    // Amber (Cash in hand)
}

/**
 * Currency formatter following Indian numbering format (e.g., ₹ 1,25,000)
 * with robust tabular alignment for fast ledger calculations.
 */
@Composable
fun RupeeText(
  amount: Double,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.titleMedium,
  colorStyle: RupeeColorStyle = RupeeColorStyle.NEUTRAL,
  customColor: Color? = null,
  showDecimals: Boolean = true,
) {
  val targetColor = customColor ?: when (colorStyle) {
    RupeeColorStyle.NEUTRAL -> MaterialTheme.colorScheme.onSurface
    RupeeColorStyle.LENA_POSITIVE -> EasyBizzyThemeTokens.colors.khataLena
    RupeeColorStyle.DENA_NEGATIVE -> EasyBizzyThemeTokens.colors.khataDena
    RupeeColorStyle.CASH_ACCENT -> EasyBizzyThemeTokens.colors.cashAccent
  }

  val formattedNumber = formatToIndianCurrency(amount, showDecimals)

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "₹ $formattedNumber",
      style = style.copy(
        color = targetColor,
        fontWeight = FontWeight.Bold,
      ),
    )
  }
}

/**
 * Formats a double into the Indian currency numbering convention (Lakhs & Crores grouping).
 */
fun formatToIndianCurrency(amount: Double, showDecimals: Boolean): String {
  return try {
    val locale = Locale("en", "IN")
    val formatter = NumberFormat.getNumberInstance(locale).apply {
      minimumFractionDigits = if (showDecimals && amount % 1.0 != 0.0) 2 else 0
      maximumFractionDigits = if (showDecimals) 2 else 0
    }
    formatter.format(amount)
  } catch (_: Exception) {
    if (showDecimals) String.format(Locale.ROOT, "%.2f", amount) else amount.toLong().toString()
  }
}
