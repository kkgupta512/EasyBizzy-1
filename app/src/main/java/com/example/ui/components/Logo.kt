package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Text-based EasyBizzy logo treatment designed for high brand recognition and professional simplicity.
 * Combines "Easy" (neutral) + "Bizzy" (primary accent) with a styled Rupee "₹" emblem.
 */
@Composable
fun EasyBizzyLogo(
  modifier: Modifier = Modifier,
  showSubtitle: Boolean = true,
  subtitleText: String = "Digital Bahi-Khata & Billing",
) {
  Row(
    modifier = modifier.testTag("easybizzy_logo"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // Emblem Badge with Indian Rupee symbol
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.primary),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "₹",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimary,
        ),
      )
    }

    Column {
      // "EasyBizzy" Logo Text
      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
          ) {
            append("Easy")
          }
          withStyle(
            SpanStyle(
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.ExtraBold,
            )
          ) {
            append("Bizzy")
          }
        },
        style = MaterialTheme.typography.titleLarge.copy(
          fontSize = 20.sp,
          letterSpacing = 0.sp,
        ),
      )

      if (showSubtitle) {
        Text(
          text = subtitleText,
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Medium,
          ),
        )
      }
    }
  }
}

/**
 * Compact Pill Badge for Mode or Verification indication.
 */
@Composable
fun StatusPill(
  text: String,
  modifier: Modifier = Modifier,
  backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
  contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(backgroundColor)
      .padding(horizontal = 10.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        color = contentColor,
      ),
    )
  }
}
