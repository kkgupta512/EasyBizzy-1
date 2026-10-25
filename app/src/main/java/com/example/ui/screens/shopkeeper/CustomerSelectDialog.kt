package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.CustomerEntity
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyOrange
import com.example.ui.theme.EasyBizzyThemeTokens

@Composable
fun CustomerSelectDialog(
  customers: List<CustomerEntity>,
  onCustomerSelected: (CustomerEntity) -> Unit,
  onManualCustomerEntered: (name: String, phone: String, gstin: String) -> Unit,
  onDismiss: () -> Unit,
) {
  var searchQuery by remember { mutableStateOf("") }
  var isManualMode by remember { mutableStateOf(false) }

  var manualName by remember { mutableStateOf("") }
  var manualPhone by remember { mutableStateOf("") }
  var manualGstin by remember { mutableStateOf("") }

  val filteredCustomers = remember(customers, searchQuery) {
    customers.filter { c ->
      val q = searchQuery.trim().lowercase()
      q.isBlank() || c.name.lowercase().contains(q) || (c.phone?.contains(q) == true)
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("dialog_customer_select"),
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
            text = if (isManualMode) "Enter Customer Details" else "Select Customer / Khata",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        if (!isManualMode) {
          EasyBizzyTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Search customer name or phone",
            leadingIcon = {
              Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            testTag = "input_customer_search",
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(text = "Existing Customers", style = MaterialTheme.typography.titleSmall)
            Text(
              text = "+ New Customer",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier
                .clickable { isManualMode = true }
                .padding(4.dp)
                .testTag("btn_switch_new_customer"),
            )
          }

          HorizontalDivider()

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(filteredCustomers, key = { it.id }) { customer ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onCustomerSelected(customer)
                    onDismiss()
                  }
                  .testTag("customer_item_${customer.id}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column {
                    Text(text = customer.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    if (!customer.phone.isNullOrBlank()) {
                      Text(text = customer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
                  if (customer.currentBalancePaise > 0L) {
                    StatusPill(
                      label = "Due: ${Money.format(customer.currentBalancePaise)}",
                      color = EasyBizzyOrange,
                    )
                  }
                }
              }
            }
          }
        } else {
          // Manual input form
          EasyBizzyTextField(
            value = manualName,
            onValueChange = { manualName = it },
            label = "Customer Name *",
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            testTag = "input_manual_customer_name",
          )

          EasyBizzyTextField(
            value = manualPhone,
            onValueChange = { manualPhone = it },
            label = "Phone Number (Optional)",
            testTag = "input_manual_customer_phone",
          )

          EasyBizzyTextField(
            value = manualGstin,
            onValueChange = { manualGstin = it },
            label = "Buyer GSTIN (Optional B2B)",
            testTag = "input_manual_customer_gstin",
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            EasyBizzyPrimaryButton(
              text = "Use Customer",
              onClick = {
                if (manualName.isNotBlank()) {
                  onManualCustomerEntered(manualName.trim(), manualPhone.trim(), manualGstin.trim())
                  onDismiss()
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_confirm_manual_customer"),
            )
          }
        }
      }
    }
  }
}
