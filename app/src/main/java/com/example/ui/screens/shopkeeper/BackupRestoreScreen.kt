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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.EasyBizzyOutlinedButton
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzySecondaryButton
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Screen managing on-device encrypted database backups and optional Google Drive sync.
 */
@Composable
fun BackupRestoreScreen(
  onExportBackup: () -> Unit,
  onRestoreBackup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var backupExportedFeedback by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("backup_restore_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Backup & Restore",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    // Security Status Card
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = EasyBizzyThemeTokens.colors.khataLena,
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
              text = "Database Security",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
          }
          StatusPill(
            text = "SQLCIPHER AES-256",
            backgroundColor = EasyBizzyThemeTokens.colors.khataLenaContainer,
            contentColor = EasyBizzyThemeTokens.colors.khataLena,
          )
        }

        Text(
          text = "All customer phone numbers, credit ledgers, product costs, and billing receipts are encrypted locally on this device using Android KeyStore hardware keys.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Local Backup Section
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Local Encrypted Snapshot",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )

        Text(
          text = "Export an encrypted snapshot of your business database to your device Downloads folder. It cannot be opened without your merchant device key.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.outline,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          EasyBizzyPrimaryButton(
            text = "Export Backup",
            onClick = {
              backupExportedFeedback = true
              onExportBackup()
            },
            icon = { Icon(Icons.Default.Download, contentDescription = null) },
            modifier = Modifier.weight(1f),
            testTag = "btn_export_backup",
          )

          EasyBizzyOutlinedButton(
            text = "Restore File",
            onClick = onRestoreBackup,
            modifier = Modifier.weight(1f),
            testTag = "btn_restore_backup",
          )
        }
      }
    }

    // Google Drive Integration Section
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Google Drive Cloud Backup",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          StatusPill(
            text = "OPTIONAL SETUP",
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Text(
          text = "Encrypted backups can be synced to your private Google Drive AppData folder. This requires Google Cloud OAuth Client ID configuration with SHA-1 signing certificates.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.outline,
        )

        EasyBizzySecondaryButton(
          text = "Connect Google Drive",
          onClick = { /* OAuth flow */ },
          icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          testTag = "btn_connect_drive",
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
