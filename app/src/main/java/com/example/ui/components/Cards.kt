package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Standard Surface Card for grouping related shop items, lists, or forms.
 */
@Composable
fun EasyBizzyCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  border: BorderStroke? = null,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  contentColor: Color = MaterialTheme.colorScheme.onSurface,
  elevation: Dp = 1.dp,
  testTag: String = "easybizzy_card",
  content: @Composable () -> Unit,
) {
  val shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius)
  Card(
    modifier = modifier
      .testTag(testTag)
      .then(
        if (onClick != null) Modifier.clip(shape).clickable { onClick() } else Modifier
      ),
    shape = shape,
    colors = CardDefaults.cardColors(
      containerColor = containerColor,
      contentColor = contentColor,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    border = border ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
  ) {
    Box(modifier = Modifier.padding(EasyBizzyThemeTokens.spacing.md)) {
      content()
    }
  }
}

/**
 * High-visibility metric card tailored for quick status glances on the shop counter.
 * Distinctive colored pill/border highlights Lena (Green) or Dena (Red).
 */
@Composable
fun MetricSummaryCard(
  title: String,
  amount: Double,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  icon: ImageVector? = null,
  colorStyle: RupeeColorStyle = RupeeColorStyle.NEUTRAL,
  onClick: (() -> Unit)? = null,
  testTag: String = "metric_card",
) {
  val (cardBg, accentColor) = when (colorStyle) {
    RupeeColorStyle.LENA_POSITIVE -> Pair(
      EasyBizzyThemeTokens.colors.khataLenaContainer.copy(alpha = 0.35f),
      EasyBizzyThemeTokens.colors.khataLena,
    )
    RupeeColorStyle.DENA_NEGATIVE -> Pair(
      EasyBizzyThemeTokens.colors.khataDenaContainer.copy(alpha = 0.35f),
      EasyBizzyThemeTokens.colors.khataDena,
    )
    RupeeColorStyle.CASH_ACCENT -> Pair(
      EasyBizzyThemeTokens.colors.cashAccentContainer.copy(alpha = 0.35f),
      EasyBizzyThemeTokens.colors.cashAccent,
    )
    RupeeColorStyle.NEUTRAL -> Pair(
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
      MaterialTheme.colorScheme.primary,
    )
  }

  val shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius)

  Surface(
    modifier = modifier
      .testTag(testTag)
      .then(
        if (onClick != null) Modifier.clip(shape).clickable { onClick() } else Modifier
      ),
    shape = shape,
    color = cardBg,
    border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.4f)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
          ),
        )

        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      RupeeText(
        amount = amount,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        colorStyle = colorStyle,
      )

      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.outline,
          ),
        )
      }
    }
  }
}
