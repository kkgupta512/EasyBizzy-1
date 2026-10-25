package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Primary high-contrast button for main counter tasks (e.g. "Save Bill", "Add Udhar", "Record Payment").
 * Features minimum 48dp height (default 52dp) for effortless tapping at a busy counter.
 */
@Composable
fun EasyBizzyPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  icon: (@Composable () -> Unit)? = null,
  minHeight: Dp = 52.dp,
  testTag: String = "primary_button",
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = minHeight)
      .testTag(testTag),
    enabled = enabled && !isLoading,
    shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.buttonRadius),
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
      disabledContentColor = MaterialTheme.colorScheme.outline,
    ),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        color = MaterialTheme.colorScheme.onPrimary,
        strokeWidth = 2.5.dp,
      )
    } else {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
          icon()
          Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
          text = text,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp,
          ),
        )
      }
    }
  }
}

/**
 * Secondary Button for alternate workflows (e.g. "Draft", "Add Another Item").
 */
@Composable
fun EasyBizzySecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: (@Composable () -> Unit)? = null,
  minHeight: Dp = 48.dp,
  testTag: String = "secondary_button",
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = minHeight)
      .testTag(testTag),
    enabled = enabled,
    shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.buttonRadius),
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
      )
    }
  }
}

/**
 * Outlined Button for secondary or dismissive actions (e.g. "Cancel", "Back", "Filters").
 */
@Composable
fun EasyBizzyOutlinedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: (@Composable () -> Unit)? = null,
  minHeight: Dp = 48.dp,
  testTag: String = "outlined_button",
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = minHeight)
      .testTag(testTag),
    enabled = enabled,
    shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.buttonRadius),
    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = MaterialTheme.colorScheme.primary,
    ),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
      )
    }
  }
}

/**
 * Destructive / Danger Button for critical settlements, account deletion or voiding bills.
 */
@Composable
fun EasyBizzyDangerButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  minHeight: Dp = 48.dp,
  testTag: String = "danger_button",
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = minHeight)
      .testTag(testTag),
    enabled = enabled && !isLoading,
    shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.buttonRadius),
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.error,
      contentColor = MaterialTheme.colorScheme.onError,
    ),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(18.dp),
        color = MaterialTheme.colorScheme.onError,
        strokeWidth = 2.dp,
      )
    } else {
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
      )
    }
  }
}
