package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.util.CsvProductImporter
import com.example.ui.components.EasyBizzyOutlinedButton
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzySecondaryButton

/**
 * Dialog for importing products in bulk via validated CSV format.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportDialog(
  onDismiss: () -> Unit,
  onImport: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var csvContent by remember { mutableStateOf("") }

  BasicAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = modifier.padding(16.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("csv_import_dialog"),
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
              text = "Bulk Product Import (CSV)",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider()

        Text(
          text = "Paste comma-separated (CSV) records below. Duplicate SKUs and equivalent specifications will be safely detected and skipped.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        EasyBizzySecondaryButton(
          text = "Load Sample Hardware & Electrical CSV",
          onClick = {
            csvContent = CsvProductImporter.getSampleCsvTemplate()
          },
          modifier = Modifier.fillMaxWidth(),
          testTag = "btn_load_sample_csv",
        )

        OutlinedTextField(
          value = csvContent,
          onValueChange = { csvContent = it },
          label = { Text("CSV Data") },
          placeholder = { Text("name,brand,sku,category,unit,specification,purchase_price,selling_price,stock...") },
          textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("input_csv_content"),
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          EasyBizzyOutlinedButton(
            text = "Cancel",
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            testTag = "btn_cancel_csv",
          )

          EasyBizzyPrimaryButton(
            text = "Import Products",
            onClick = {
              if (csvContent.isNotBlank()) {
                onImport(csvContent)
                onDismiss()
              }
            },
            enabled = csvContent.isNotBlank(),
            modifier = Modifier.weight(1f),
            testTag = "btn_submit_csv_import",
          )
        }
      }
    }
  }
}
