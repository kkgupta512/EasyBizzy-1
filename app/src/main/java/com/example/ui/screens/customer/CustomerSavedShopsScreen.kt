package com.example.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EasyBizzyEmptyState

/**
 * Screen displaying the customer's favorite and bookmarked neighborhood stores.
 */
@Composable
fun CustomerSavedShopsScreen(
  onExploreShops: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("customer_saved_shops_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Saved Shops",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    EasyBizzyEmptyState(
      title = "No Saved Shops Yet",
      description = "Bookmark your trusted local kirana, medical stores, and dairy booths for quick price checking and 1-tap WhatsApp ordering.",
      icon = Icons.Default.Favorite,
      actionButtonText = "Explore Nearby Shops",
      onActionClick = onExploreShops,
      testTag = "empty_state_saved_shops",
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
