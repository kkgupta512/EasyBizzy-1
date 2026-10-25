package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.MetricSummaryCard
import com.example.ui.components.RupeeColorStyle

/**
 * Digital Udhar Bahi-Khata ledger screen for customer and supplier credit tracking.
 */
@Composable
fun KhataScreen(
  onAddPartyClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Customers, 1: Suppliers
  var searchQuery by remember { mutableStateOf("") }

  val totalLena = 0.0
  val totalDena = 0.0

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("khata_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text(
      text = "Digital Bahi-Khata",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    // Summary Card depending on tab
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MetricSummaryCard(
        title = "You will get (Lena)",
        amount = totalLena,
        colorStyle = RupeeColorStyle.LENA_POSITIVE,
        subtitle = "From Customers",
        modifier = Modifier.weight(1f),
        testTag = "khata_metric_lena",
      )
      MetricSummaryCard(
        title = "You will give (Dena)",
        amount = totalDena,
        colorStyle = RupeeColorStyle.DENA_NEGATIVE,
        subtitle = "To Suppliers",
        modifier = Modifier.weight(1f),
        testTag = "khata_metric_dena",
      )
    }

    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Text(
            text = "Customers (Grahak)",
            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
          )
        },
        modifier = Modifier.testTag("tab_khata_customers"),
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text(
            text = "Suppliers (Vyapari)",
            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
          )
        },
        modifier = Modifier.testTag("tab_khata_suppliers"),
      )
    }

    EasyBizzyTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = if (selectedTab == 0) "Search customer name or phone" else "Search supplier name",
      leadingIcon = {
        androidx.compose.material3.Icon(imageVector = Icons.Default.Search, contentDescription = null)
      },
      testTag = "input_search_khata",
    )

    // Labeled empty state
    EasyBizzyEmptyState(
      title = if (selectedTab == 0) "No Customers Added" else "No Suppliers Added",
      description = if (selectedTab == 0) {
        "Record customer credit (Udhar) and payments. You can send free WhatsApp payment reminders with UPI links."
      } else {
        "Track payments owed to wholesale suppliers and distributors."
      },
      icon = Icons.Default.ReceiptLong,
      actionButtonText = if (selectedTab == 0) "+ Add Customer" else "+ Add Supplier",
      onActionClick = onAddPartyClick,
      testTag = "empty_state_khata",
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
