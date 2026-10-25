package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.data.entity.ProductEntity
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyGreen
import com.example.ui.theme.EasyBizzyOrange
import com.example.ui.theme.EasyBizzyThemeTokens

@Composable
fun QuickProductSearchDialog(
  products: List<ProductEntity>,
  onProductSelected: (ProductEntity) -> Unit,
  onDismiss: () -> Unit,
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("ALL") }

  val categories = remember(products) {
    listOf("ALL") + products.map { it.category }.distinct().filter { it.isNotBlank() }
  }

  val filteredProducts = remember(products, searchQuery, selectedCategory) {
    products.filter { p ->
      val matchesCategory = selectedCategory == "ALL" || p.category.equals(selectedCategory, ignoreCase = true)
      val query = searchQuery.trim().lowercase()
      val matchesSearch = query.isBlank() ||
        p.name.lowercase().contains(query) ||
        (p.brand?.lowercase()?.contains(query) == true) ||
        (p.sku?.lowercase()?.contains(query) == true) ||
        (p.specification?.lowercase()?.contains(query) == true) ||
        (p.barcode?.lowercase()?.contains(query) == true)
      matchesCategory && matchesSearch
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
        .testTag("dialog_quick_product_search"),
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
            text = "Select Product",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        EasyBizzyTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = "Search by item name, brand, SKU or size",
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
          },
          testTag = "input_quick_search_query",
        )

        // Category Chips
        if (categories.size > 2) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            categories.forEach { category ->
              FilterChip(
                selected = selectedCategory == category,
                onClick = { selectedCategory = category },
                label = { Text(category) },
                modifier = Modifier.testTag("filter_cat_$category"),
              )
            }
          }
        }

        HorizontalDivider()

        // Products List
        if (filteredProducts.isEmpty()) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Default.Inventory2,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
              text = if (searchQuery.isBlank()) "No products in inventory yet" else "No matching products found",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(filteredProducts, key = { it.id }) { product ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onProductSelected(product)
                    onDismiss()
                  }
                  .testTag("product_item_${product.id}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = product.name,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    val detailLine = buildString {
                      if (!product.brand.isNullOrBlank()) append("${product.brand} • ")
                      if (!product.specification.isNullOrBlank()) append("${product.specification} • ")
                      val unitStr = if (product.unit == "CUSTOM") product.customUnit ?: "Custom" else product.unit
                      append(unitStr)
                    }
                    Text(
                      text = detailLine,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (product.currentStock != null) {
                      val stockLabel = "Stock: ${product.currentStock} ${if (product.unit == "CUSTOM") product.customUnit ?: "" else product.unit}"
                      val stockColor = if (product.currentStock <= product.minStockAlert) EasyBizzyOrange else EasyBizzyGreen
                      StatusPill(label = stockLabel, color = stockColor, modifier = Modifier.padding(top = 4.dp))
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = Money.format(product.sellingPricePaise),
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.padding(end = 8.dp),
                    )
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
