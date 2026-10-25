package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.theme.EasyBizzyThemeTokens

@Composable
fun AddCustomLineItemDialog(
  onAddItem: (name: String, unitPricePaise: Long, quantity: Double, unit: String) -> Unit,
  onDismiss: () -> Unit,
) {
  var itemName by remember { mutableStateOf("") }
  var unitPriceRupees by remember { mutableStateOf("") }
  var quantityStr by remember { mutableStateOf("1.0") }
  var unit by remember { mutableStateOf("PCS") }

  val isValid = itemName.isNotBlank() &&
    (unitPriceRupees.toDoubleOrNull() ?: 0.0) > 0.0 &&
    (quantityStr.toDoubleOrNull() ?: 0.0) > 0.0

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("dialog_add_custom_item"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Add Loose / Custom Item",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        EasyBizzyTextField(
          value = itemName,
          onValueChange = { itemName = it },
          label = "Item Description * (e.g. Loose Nails, Labor)",
          testTag = "input_custom_item_name",
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          EasyBizzyTextField(
            value = unitPriceRupees,
            onValueChange = { unitPriceRupees = it },
            label = "Rate (₹) *",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            testTag = "input_custom_item_price",
          )

          EasyBizzyTextField(
            value = quantityStr,
            onValueChange = { quantityStr = it },
            label = "Quantity",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(0.7f),
            testTag = "input_custom_item_qty",
          )

          EasyBizzyTextField(
            value = unit,
            onValueChange = { unit = it },
            label = "Unit",
            modifier = Modifier.weight(0.6f),
            testTag = "input_custom_item_unit",
          )
        }

        HorizontalDivider()

        EasyBizzyPrimaryButton(
          text = "Add to Bill",
          enabled = isValid,
          onClick = {
            val pricePaise = Money.toPaise(unitPriceRupees.toDouble())
            val qty = quantityStr.toDouble()
            onAddItem(itemName.trim(), pricePaise, qty, unit.trim().ifBlank { "PCS" })
            onDismiss()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_confirm_add_custom_item"),
        )
      }
    }
  }
}
