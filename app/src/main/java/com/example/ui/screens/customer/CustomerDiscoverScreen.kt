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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Customer Marketplace Discovery Screen for finding local shops within walking distance.
 */
@Composable
fun CustomerDiscoverScreen(
  onShopClick: (String) -> Unit,
  onSearchClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var userLocation by remember { mutableStateOf("Sector 21, New Delhi (110001)") }
  var selectedCategory by remember { mutableStateOf("ALL") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("customer_discover_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Hyperlocal Location Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Column {
          Text(
            text = "Your Neighborhood",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.outline,
          )
          Text(
            text = userLocation,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      StatusPill(
        text = "0% COMMISSION",
        backgroundColor = EasyBizzyThemeTokens.colors.khataLenaContainer,
        contentColor = EasyBizzyThemeTokens.colors.khataLena,
      )
    }

    // Quick Search Trigger
    Surface(
      onClick = onSearchClick,
      shape = MaterialTheme.shapes.medium,
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trigger_customer_search"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.outline,
        )
        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
        Text(
          text = "Search staple items (Atta, Dal, Milk, Medicines)...",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.outline,
        )
      }
    }

    // Category Quick Filters
    Text(
      text = "Browse by Store Type",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      listOf("ALL" to "All Shops", "KIRANA" to "Kirana & Grocery", "PHARMACY" to "Chemist", "DAIRY" to "Dairy & Sweets", "VEG" to "Sabzi Mandi").forEach { (code, label) ->
        FilterChip(
          selected = selectedCategory == code,
          onClick = { selectedCategory = code },
          label = { Text(label) },
          modifier = Modifier.testTag("cust_filter_$code"),
        )
      }
    }

    // Real Marketplace Empty / Discovery State (Spring Boot server connection pending)
    EasyBizzyEmptyState(
      title = "Searching Nearby Shops...",
      description = "Connecting to the local marketplace server. Local shopkeepers who publish their live storefronts will appear here with direct contact and price lists.",
      icon = Icons.Default.Storefront,
      actionButtonText = "Check Saved Shops",
      onActionClick = { /* Navigate to saved shops */ },
      testTag = "empty_state_customer_discover",
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
