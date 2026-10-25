package com.example.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * High-readability Form Input Field designed for shop counter workflows.
 * Features large font, clear borders, prefix support (+91, ₹), and validation error handling.
 */
@Composable
fun EasyBizzyTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  prefix: String? = null,
  suffix: String? = null,
  leadingIcon: (@Composable () -> Unit)? = null,
  errorMessage: String? = null,
  supportingText: String? = null,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  singleLine: Boolean = true,
  maxLines: Int = 1,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  showClearButton: Boolean = true,
  testTag: String = "easybizzy_textfield",
) {
  val isError = !errorMessage.isNullOrBlank()
  val interactionSource = remember { MutableInteractionSource() }

  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag(testTag),
      enabled = enabled,
      readOnly = readOnly,
      label = {
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        )
      },
      placeholder = if (placeholder != null) {
        {
          Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyLarge.copy(
              color = MaterialTheme.colorScheme.outline,
            ),
          )
        }
      } else null,
      prefix = if (prefix != null) {
        {
          Text(
            text = prefix,
            style = MaterialTheme.typography.titleMedium.copy(
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            ),
          )
        }
      } else null,
      suffix = if (suffix != null) {
        {
          Text(
            text = suffix,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.outline,
            ),
          )
        }
      } else null,
      leadingIcon = leadingIcon,
      trailingIcon = {
        if (isError) {
          Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
          )
        } else if (showClearButton && value.isNotEmpty() && enabled && !readOnly) {
          IconButton(
            onClick = { onValueChange("") },
            modifier = Modifier.size(40.dp).testTag("${testTag}_clear"),
          ) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Clear input",
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      },
      isError = isError,
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      singleLine = singleLine,
      maxLines = maxLines,
      interactionSource = interactionSource,
      shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.buttonRadius),
      textStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
      ),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorContainerColor = MaterialTheme.colorScheme.surface,
      ),
    )

    // Helper / Error Label
    if (isError) {
      Text(
        text = errorMessage.orEmpty(),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        modifier = Modifier.padding(start = 12.dp, top = 4.dp),
      )
    } else if (!supportingText.isNullOrBlank()) {
      Text(
        text = supportingText,
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 12.dp, top = 4.dp),
      )
    }
  }
}
