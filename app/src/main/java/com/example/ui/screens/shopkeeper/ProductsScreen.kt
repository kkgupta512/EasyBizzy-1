package com.example.ui.screens.shopkeeper

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ProductEntity
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.EasyBizzyErrorMessage
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzySecondaryButton
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.RupeeColorStyle
import com.example.ui.components.RupeeText
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyThemeTokens
import com.example.ui.viewmodel.InventoryUiState
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.ProductSortOrder

val PRODUCT_CATEGORIES = listOf(
  "ALL" to "All Categories",
  "Hardware" to "Hardware",
  "Electrical" to "Electrical",
  "Building Material" to "Building Material",
  "Plumbing" to "Plumbing",
  "Paint" to "Paint",
  "Kirana" to "Kirana",
  "General" to "General",
)

/**
 * Screen for full product inventory management, specification categorization,
 * duplicate detection, archival, and bulk CSV import.
 */
@Composable
fun ProductsScreen(
  onAddProductClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: InventoryViewModel? = null,
) {
  val uiState by viewModel?.uiState?.collectAsStateWithLifecycle() ?: remember {
    mutableStateOf(InventoryUiState())
  }

  var showAddEditDialog by remember { mutableStateOf(false) }
  var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
  var showCsvImportDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("products_screen"),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Header & Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = "Product Inventory",
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        )
        Text(
          text = "${uiState.products.size} items • ${uiState.lowStockCount} low stock alerts",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.outline,
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
          onClick = { showCsvImportDialog = true },
          modifier = Modifier.testTag("btn_open_csv_import"),
        ) {
          Icon(Icons.Default.FileUpload, contentDescription = "Import CSV", tint = MaterialTheme.colorScheme.primary)
        }

        EasyBizzyPrimaryButton(
          text = "+ Add",
          onClick = {
            productToEdit = null
            showAddEditDialog = true
          },
          minHeight = 44.dp,
          testTag = "btn_add_product_top",
        )
      }
    }

    // Active vs Archived Tabs
    TabRow(
      selectedTabIndex = if (uiState.filterState.showArchived) 1 else 0,
      containerColor = MaterialTheme.colorScheme.surface,
    ) {
      Tab(
        selected = !uiState.filterState.showArchived,
        onClick = { viewModel?.setShowArchived(false) },
        text = { Text("Active Catalog", fontWeight = FontWeight.Bold) },
        modifier = Modifier.testTag("tab_active_products"),
      )
      Tab(
        selected = uiState.filterState.showArchived,
        onClick = { viewModel?.setShowArchived(true) },
        text = { Text("Archived Items", fontWeight = FontWeight.Bold) },
        modifier = Modifier.testTag("tab_archived_products"),
      )
    }

    // Search Field
    EasyBizzyTextField(
      value = uiState.filterState.searchQuery,
      onValueChange = { viewModel?.setSearchQuery(it) },
      label = "Search by name, brand, SKU, size, or barcode",
      placeholder = "e.g., Havells, 1.5 sq mm, TMT, UltraTech",
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = null)
      },
      testTag = "input_search_products",
    )

    // Category Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      PRODUCT_CATEGORIES.forEach { (catKey, label) ->
        FilterChip(
          selected = uiState.filterState.selectedCategory == catKey,
          onClick = { viewModel?.setCategoryFilter(catKey) },
          label = { Text(label) },
          modifier = Modifier.testTag("filter_cat_$catKey"),
        )
      }
    }

    // Sort Order Selection Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Icons.Default.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
      ProductSortOrder.entries.forEach { order ->
        FilterChip(
          selected = uiState.filterState.sortOrder == order,
          onClick = { viewModel?.setSortOrder(order) },
          label = { Text(order.label, fontSize = 12.sp) },
          modifier = Modifier.testTag("sort_${order.name}"),
        )
      }
    }

    // Success / Error Feedback Banners (e.g. for duplicates or successful imports)
    if (uiState.errorMessage != null) {
      EasyBizzyErrorMessage(
        title = "Duplicate / Validation Alert",
        message = uiState.errorMessage.orEmpty(),
        testTag = "product_error_banner",
      )
    }

    if (uiState.successMessage != null) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = EasyBizzyThemeTokens.colors.khataLenaContainer,
        modifier = Modifier.fillMaxWidth().testTag("product_success_banner"),
      ) {
        Text(
          text = uiState.successMessage.orEmpty(),
          style = MaterialTheme.typography.bodyMedium.copy(
            color = EasyBizzyThemeTokens.colors.khataLena,
            fontWeight = FontWeight.Bold,
          ),
          modifier = Modifier.padding(12.dp),
        )
      }
    }

    // Products List or Empty State
    if (uiState.products.isEmpty()) {
      EasyBizzyEmptyState(
        title = if (uiState.filterState.showArchived) "No Archived Products" else "No Products in Catalog",
        description = if (uiState.filterState.showArchived) {
          "Products you archive will be stored here safely without cluttering active counter billing."
        } else {
          "Add hardware, electrical, building materials, or general store items. You can also import in bulk via CSV."
        },
        icon = Icons.Default.Inventory2,
        actionButtonText = if (!uiState.filterState.showArchived) "+ Add First Product" else null,
        onActionClick = {
          productToEdit = null
          showAddEditDialog = true
        },
        testTag = "empty_state_products",
      )
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        items(uiState.products, key = { it.id }) { product ->
          ProductCard(
            product = product,
            onEdit = {
              productToEdit = product
              showAddEditDialog = true
            },
            onToggleArchive = {
              viewModel?.setArchived(product.id, !product.isArchived)
            },
            onDelete = {
              viewModel?.deleteProduct(product.id)
            },
            onToggleMarketplace = {
              viewModel?.toggleMarketplacePublication(product.id, !product.isMarketplacePublished)
            },
          )
        }
      }
    }
  }

  // Add / Edit Modal Dialog
  if (showAddEditDialog) {
    AddEditProductDialog(
      existingProduct = productToEdit,
      onDismiss = { showAddEditDialog = false },
      onSave = { savedProduct ->
        if (productToEdit == null) {
          viewModel?.addProduct(savedProduct) {
            showAddEditDialog = false
          }
        } else {
          viewModel?.updateProduct(savedProduct) {
            showAddEditDialog = false
          }
        }
      },
    )
  }

  // Bulk CSV Import Dialog
  if (showCsvImportDialog) {
    CsvImportDialog(
      onDismiss = { showCsvImportDialog = false },
      onImport = { csvContent ->
        viewModel?.importFromCsv(csvContent)
      },
    )
  }
}

@Composable
private fun ProductCard(
  product: ProductEntity,
  onEdit: () -> Unit,
  onToggleArchive: () -> Unit,
  onDelete: () -> Unit,
  onToggleMarketplace: () -> Unit,
) {
  val isLowStock = product.currentStock != null && product.currentStock <= product.minStockAlert
  val unitLabel = if (product.unit == "CUSTOM") product.customUnit ?: "Custom" else product.unit

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("product_card_${product.id}"),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          // Brand tag if available
          if (!product.brand.isNullOrBlank()) {
            Text(
              text = product.brand.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              ),
            )
          }

          Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )

          // Size / Specification highlight
          if (!product.specification.isNullOrBlank()) {
            Text(
              text = "Spec: ${product.specification}",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
            )
          }
        }

        // Price in ₹
        Column(horizontalAlignment = Alignment.End) {
          RupeeText(
            amount = Money.toRupees(product.sellingPricePaise),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          Text(
            text = "per $unitLabel",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.outline,
              fontSize = 11.sp,
            ),
          )
        }
      }

      // Metadata Row (Category, SKU, Stock, Marketplace)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        StatusPill(
          text = product.category,
          backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!product.sku.isNullOrBlank()) {
          StatusPill(
            text = "SKU: ${product.sku}",
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }

        if (product.currentStock != null) {
          StatusPill(
            text = "Stock: ${product.currentStock} $unitLabel",
            backgroundColor = if (isLowStock) {
              EasyBizzyThemeTokens.colors.khataDenaContainer
            } else {
              EasyBizzyThemeTokens.colors.khataLenaContainer
            },
            contentColor = if (isLowStock) {
              EasyBizzyThemeTokens.colors.khataDena
            } else {
              EasyBizzyThemeTokens.colors.khataLena
            },
          )
        }

        if (product.isMarketplacePublished) {
          StatusPill(
            text = "MARKETPLACE LIVE",
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // Card Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onEdit, modifier = Modifier.testTag("btn_edit_${product.id}")) {
          Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
        }

        IconButton(onClick = onToggleArchive, modifier = Modifier.testTag("btn_archive_${product.id}")) {
          Icon(
            imageVector = if (product.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
            contentDescription = if (product.isArchived) "Unarchive" else "Archive",
            modifier = Modifier.size(18.dp),
          )
        }

        IconButton(onClick = onDelete, modifier = Modifier.testTag("btn_delete_${product.id}")) {
          Icon(
            Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}
