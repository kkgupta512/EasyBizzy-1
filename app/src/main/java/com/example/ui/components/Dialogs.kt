package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Reusable Confirmation & Decision Dialog for shopkeeper actions.
 * Explicitly designed with large distinct touch targets to prevent accidental taps.
 */
@Composable
fun EasyBizzyConfirmDialog(
  title: String,
  message: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  confirmButtonText: String = "Confirm",
  dismissButtonText: String = "Cancel",
  isDestructive: Boolean = false,
  icon: ImageVector? = null,
  testTag: String = "confirm_dialog",
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    modifier = Modifier.testTag(testTag),
    shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.dialogRadius),
    containerColor = MaterialTheme.colorScheme.surface,
    icon = if (icon != null) {
      {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
      }
    } else null,
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
      )
    },
    text = {
      Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    confirmButton = {
      if (isDestructive) {
        EasyBizzyDangerButton(
          text = confirmButtonText,
          onClick = onConfirm,
          testTag = "${testTag}_confirm",
        )
      } else {
        EasyBizzyPrimaryButton(
          text = confirmButtonText,
          onClick = onConfirm,
          testTag = "${testTag}_confirm",
        )
      }
    },
    dismissButton = {
      EasyBizzyOutlinedButton(
        text = dismissButtonText,
        onClick = onDismiss,
        testTag = "${testTag}_dismiss",
      )
    },
  )
}
