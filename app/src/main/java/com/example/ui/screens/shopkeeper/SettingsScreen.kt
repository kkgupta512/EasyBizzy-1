package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EasyBizzyCard
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzyTextField

/**
 * Screen for merchant counter preferences, app lock security, and printer configuration.
 */
@Composable
fun SettingsScreen(
  onSaveSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var biometricLockEnabled by remember { mutableStateOf(false) }
  var defaultGstRate by remember { mutableStateOf("0") }
  var receiptFooter by remember { mutableStateOf("Thank you for shopping with us! Visit again.") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("settings_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "App Settings",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    // Security & App Lock
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Security & PIN Lock",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Require Biometric / PIN Lock",
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            )
            Text(
              text = "Protects counter records when app is minimized",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline,
            )
          }
          Switch(
            checked = biometricLockEnabled,
            onCheckedChange = { biometricLockEnabled = it },
            modifier = Modifier.testTag("switch_biometric_lock"),
          )
        }
      }
    }

    // Receipt Customization
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Receipt & Invoicing Preferences",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )

        EasyBizzyTextField(
          value = defaultGstRate,
          onValueChange = { defaultGstRate = it },
          label = "Default GST Rate (%)",
          placeholder = "e.g., 0, 5, 12, 18",
          testTag = "input_default_gst",
        )

        EasyBizzyTextField(
          value = receiptFooter,
          onValueChange = { receiptFooter = it },
          label = "Receipt Footer Message",
          placeholder = "Thank you message on printed/WhatsApp bill",
          testTag = "input_receipt_footer",
        )
      }
    }

    // About & Build Info
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "About EasyBizzy",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Text(
          text = "Version 1.0.0 (Offline-First Edition)",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "Built for Indian retail merchants. Encrypted local persistence, zero commission fees.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.outline,
        )
      }
    }

    EasyBizzyPrimaryButton(
      text = "Save Settings",
      onClick = onSaveSettings,
      modifier = Modifier.fillMaxWidth(),
      testTag = "btn_save_settings",
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
