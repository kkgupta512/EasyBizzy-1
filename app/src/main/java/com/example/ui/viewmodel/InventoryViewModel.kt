package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.ProductEntity
import com.example.data.repository.DuplicateProductException
import com.example.data.repository.InventoryRepository
import com.example.data.util.CsvImportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProductSortOrder(val label: String) {
  NAME_ASC("Name (A to Z)"),
  NAME_DESC("Name (Z to A)"),
  PRICE_LOW_HIGH("Price (Low to High)"),
  PRICE_HIGH_LOW("Price (High to Low)"),
  STOCK_LOW_HIGH("Stock (Lowest First)"),
}

data class ProductFilterState(
  val searchQuery: String = "",
  val selectedCategory: String = "ALL",
  val sortOrder: ProductSortOrder = ProductSortOrder.NAME_ASC,
  val showArchived: Boolean = false,
)

data class InventoryUiState(
  val products: List<ProductEntity> = emptyList(),
  val lowStockCount: Int = 0,
  val filterState: ProductFilterState = ProductFilterState(),
  val errorMessage: String? = null,
  val successMessage: String? = null,
  val lastImportResult: CsvImportResult? = null,
  val isImporting: Boolean = false,
)

/**
 * ViewModel managing inventory products with search, sorting, filtering,
 * duplicate detection, archival, and bulk CSV import.
 */
class InventoryViewModel(
  private val inventoryRepository: InventoryRepository,
) : ViewModel() {

  private val _filterState = MutableStateFlow(ProductFilterState())
  private val _errorMessage = MutableStateFlow<String?>(null)
  private val _successMessage = MutableStateFlow<String?>(null)
  private val _lastImportResult = MutableStateFlow<CsvImportResult?>(null)
  private val _isImporting = MutableStateFlow(false)

  private val _feedbackState = combine(
    _errorMessage,
    _successMessage,
    _lastImportResult,
  ) { error, success, importRes ->
    Triple(error, success, importRes)
  }

  val uiState: StateFlow<InventoryUiState> = combine(
    inventoryRepository.activeProducts,
    inventoryRepository.archivedProducts,
    _filterState,
    _feedbackState,
  ) { activeList, archivedList, filters, (error, success, importRes) ->
    val baseList = if (filters.showArchived) archivedList else activeList

    val filtered = baseList.filter { product ->
      val matchesCategory = filters.selectedCategory == "ALL" ||
        product.category.equals(filters.selectedCategory, ignoreCase = true)

      val query = filters.searchQuery.trim().lowercase()
      val matchesSearch = query.isBlank() ||
        product.name.lowercase().contains(query) ||
        (product.brand?.lowercase()?.contains(query) == true) ||
        (product.sku?.lowercase()?.contains(query) == true) ||
        (product.specification?.lowercase()?.contains(query) == true) ||
        (product.barcode?.lowercase()?.contains(query) == true)

      matchesCategory && matchesSearch
    }.let { list ->
      when (filters.sortOrder) {
        ProductSortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
        ProductSortOrder.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
        ProductSortOrder.PRICE_LOW_HIGH -> list.sortedBy { it.sellingPricePaise }
        ProductSortOrder.PRICE_HIGH_LOW -> list.sortedByDescending { it.sellingPricePaise }
        ProductSortOrder.STOCK_LOW_HIGH -> list.sortedBy { it.currentStock ?: Double.MAX_VALUE }
      }
    }

    val lowStock = activeList.count { it.currentStock != null && it.currentStock <= it.minStockAlert }

    InventoryUiState(
      products = filtered,
      lowStockCount = lowStock,
      filterState = filters,
      errorMessage = error,
      successMessage = success,
      lastImportResult = importRes,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = InventoryUiState(),
  )

  fun setSearchQuery(query: String) {
    _filterState.update { it.copy(searchQuery = query) }
  }

  fun setCategoryFilter(category: String) {
    _filterState.update { it.copy(selectedCategory = category) }
  }

  fun setSortOrder(order: ProductSortOrder) {
    _filterState.update { it.copy(sortOrder = order) }
  }

  fun setShowArchived(show: Boolean) {
    _filterState.update { it.copy(showArchived = show) }
  }

  fun clearMessages() {
    _errorMessage.value = null
    _successMessage.value = null
    _lastImportResult.value = null
  }

  fun addProduct(
    product: ProductEntity,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      try {
        clearMessages()
        inventoryRepository.addProduct(product)
        _successMessage.value = "Product '${product.name}' added successfully."
        onSuccess()
      } catch (e: DuplicateProductException) {
        _errorMessage.value = e.message
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "Failed to add product."
      }
    }
  }

  fun updateProduct(
    product: ProductEntity,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      try {
        clearMessages()
        inventoryRepository.updateProduct(product)
        _successMessage.value = "Product '${product.name}' updated successfully."
        onSuccess()
      } catch (e: DuplicateProductException) {
        _errorMessage.value = e.message
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "Failed to update product."
      }
    }
  }

  fun setArchived(productId: String, isArchived: Boolean) {
    viewModelScope.launch {
      try {
        clearMessages()
        inventoryRepository.setArchived(productId, isArchived)
        _successMessage.value = if (isArchived) "Product archived." else "Product restored to active inventory."
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "Failed to change archive status."
      }
    }
  }

  fun deleteProduct(productId: String) {
    viewModelScope.launch {
      try {
        clearMessages()
        inventoryRepository.deleteProduct(productId)
        _successMessage.value = "Product permanently deleted."
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "Failed to delete product."
      }
    }
  }

  fun toggleMarketplacePublication(productId: String, isPublished: Boolean) {
    viewModelScope.launch {
      inventoryRepository.toggleMarketplacePublication(productId, isPublished)
    }
  }

  fun importFromCsv(csvContent: String) {
    viewModelScope.launch {
      _isImporting.value = true
      clearMessages()
      try {
        val result = inventoryRepository.bulkImportCsv(csvContent)
        _lastImportResult.value = result
        if (result.validProducts.isNotEmpty()) {
          _successMessage.value = "Successfully imported ${result.validProducts.size} products."
        }
        if (result.skippedDuplicatesCount > 0) {
          _errorMessage.value = "Skipped ${result.skippedDuplicatesCount} duplicate products."
        }
      } catch (e: Exception) {
        _errorMessage.value = "CSV Import Error: ${e.message}"
      } finally {
        _isImporting.value = false
      }
    }
  }

  class Factory(private val inventoryRepository: InventoryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return InventoryViewModel(inventoryRepository) as T
    }
  }
}
