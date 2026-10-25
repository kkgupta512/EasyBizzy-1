package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Empty state card displayed when no records (bills, khata entries, products) exist yet.
 * Explains what to do in simple language and offers a primary action button.
 */
@Composable
fun EasyBizzyEmptyState(
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Default.Info,
  actionButtonText: String? = null,
  onActionClick: (() -> Unit)? = null,
  testTag: String = "empty_state",
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 32.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(32.dp),
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    if (actionButtonText != null && onActionClick != null) {
      Spacer(modifier = Modifier.height(20.dp))
      EasyBizzyPrimaryButton(
        text = actionButtonText,
        onClick = onActionClick,
        testTag = "${testTag}_action",
      )
    }
  }
}

/**
 * Centered Loading indicator with optional descriptive message.
 */
@Composable
fun EasyBizzyLoading(
  modifier: Modifier = Modifier,
  message: String = "Loading...",
  testTag: String = "loading_indicator",
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    CircularProgressIndicator(
      color = MaterialTheme.colorScheme.primary,
      strokeWidth = 3.dp,
      modifier = Modifier.size(36.dp),
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
      ),
      textAlign = TextAlign.Center,
    )
  }
}

/**
 * High-visibility Error message banner for network or validation failures.
 */
@Composable
fun EasyBizzyErrorMessage(
  message: String,
  modifier: Modifier = Modifier,
  title: String = "Something went wrong",
  onRetry: (() -> Unit)? = null,
  testTag: String = "error_message",
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Error icon",
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
          ),
        )
      }

      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onErrorContainer,
      )

      if (onRetry != null) {
        Spacer(modifier = Modifier.height(4.dp))
        EasyBizzySecondaryButton(
          text = "Try Again",
          onClick = onRetry,
          icon = {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          },
          testTag = "${testTag}_retry",
        )
      }
    }
  }
}
