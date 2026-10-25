package com.example.ui.screens.customer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.EasyBizzyTextField

/**
 * Screen for customer item search and local price comparison.
 */
@Composable
fun CustomerSearchScreen(
  onSelectProduct: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var searchQuery by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("customer_search_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text(
      text = "Search & Compare Prices",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    EasyBizzyTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = "Enter product name or brand",
      placeholder = "e.g., Aashirvaad Atta, Tata Salt, Maggi",
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = null)
      },
      testTag = "input_customer_search",
    )

    Text(
      text = "Popular Local Searches",
      style = MaterialTheme.typography.labelMedium.copy(
        color = MaterialTheme.colorScheme.outline,
        fontWeight = FontWeight.SemiBold,
      ),
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      listOf("Atta 10kg", "Mustard Oil 1L", "Tata Salt", "Amul Butter", "Sugar 5kg").forEach { term ->
        FilterChip(
          selected = searchQuery == term,
          onClick = { searchQuery = term },
          label = { Text(term) },
          modifier = Modifier.testTag("chip_search_$term"),
        )
      }
    }

    EasyBizzyEmptyState(
      title = if (searchQuery.isEmpty()) "Search Local Grocery Prices" else "No Published Stores Nearby for '$searchQuery'",
      description = if (searchQuery.isEmpty()) {
        "Compare prices across verified shops in your neighborhood to save money on daily groceries."
      } else {
        "Local merchants who have published '$searchQuery' to the marketplace will show their rates and distance."
      },
      icon = Icons.Default.Storefront,
      actionButtonText = if (searchQuery.isNotEmpty()) "Clear Search" else null,
      onActionClick = { searchQuery = "" },
      testTag = "empty_state_search",
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
