package com.example.data.repository

import com.example.data.dao.ProductDao
import com.example.data.entity.ProductEntity
import com.example.data.util.CsvImportResult
import com.example.data.util.CsvProductImporter
import kotlinx.coroutines.flow.Flow

class DuplicateProductException(message: String) : Exception(message)

/**
 * Repository managing catalog inventory, stock alerts, duplicate detection,
 * archival/unarchival, and CSV bulk import.
 */
class InventoryRepository(
  private val productDao: ProductDao,
) {
  val activeProducts: Flow<List<ProductEntity>> = productDao.getActiveProducts()
  val archivedProducts: Flow<List<ProductEntity>> = productDao.getArchivedProducts()
  val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
  val marketplaceProducts: Flow<List<ProductEntity>> = productDao.getMarketplacePublishedProducts()
  val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

  /**
   * Adds a product after verifying that no duplicate SKU or equivalent specification exists.
   */
  suspend fun addProduct(product: ProductEntity) {
    validateProduct(product)
    checkDuplicates(product, excludeId = null)
    productDao.insertProduct(product)
  }

  /**
   * Updates an existing product while preventing collision with other existing products.
   */
  suspend fun updateProduct(product: ProductEntity) {
    validateProduct(product)
    checkDuplicates(product, excludeId = product.id)
    productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
  }

  /**
   * Archives or restores a product.
   */
  suspend fun setArchived(productId: String, isArchived: Boolean) {
    productDao.setArchivedStatus(productId, isArchived)
  }

  suspend fun deleteProduct(productId: String) {
    productDao.deleteProductById(productId)
  }

  suspend fun toggleMarketplacePublication(productId: String, isPublished: Boolean) {
    val existing = productDao.getProductById(productId)
    if (existing != null) {
      productDao.updateProduct(
        existing.copy(
          isMarketplacePublished = isPublished,
          updatedAt = System.currentTimeMillis(),
        )
      )
    }
  }

  /**
   * Imports products in bulk from validated CSV content.
   */
  suspend fun bulkImportCsv(csvContent: String): CsvImportResult {
    val existing = productDao.getActiveProductsSnapshot()
    val result = CsvProductImporter.parseAndValidateCsv(csvContent, existing)
    if (result.validProducts.isNotEmpty()) {
      productDao.insertProducts(result.validProducts)
    }
    return result
  }

  private fun validateProduct(product: ProductEntity) {
    if (product.name.isBlank()) {
      throw IllegalArgumentException("Product name cannot be empty.")
    }
    if (product.sellingPricePaise < 0L) {
      throw IllegalArgumentException("Selling price cannot be negative.")
    }
    if (product.purchasePricePaise < 0L) {
      throw IllegalArgumentException("Purchase price cannot be negative.")
    }
    if (product.unit == "CUSTOM" && product.customUnit.isNullOrBlank()) {
      throw IllegalArgumentException("Custom unit name must be specified.")
    }
  }

  private suspend fun checkDuplicates(product: ProductEntity, excludeId: String?) {
    // 1. Check unique SKU if present
    val sku = product.sku?.trim()
    if (!sku.isNullOrBlank()) {
      val existingBySku = if (excludeId != null) {
        productDao.getProductBySkuExcluding(sku, excludeId)
      } else {
        productDao.getProductBySku(sku)
      }
      if (existingBySku != null) {
        throw DuplicateProductException("A product with SKU '$sku' already exists: '${existingBySku.name}'.")
      }
    }

    // 2. Check unique Barcode if present
    val barcode = product.barcode?.trim()
    if (!barcode.isNullOrBlank()) {
      val existingByBarcode = if (excludeId != null) {
        productDao.getProductByBarcodeExcluding(barcode, excludeId)
      } else {
        productDao.getProductByBarcode(barcode)
      }
      if (existingByBarcode != null) {
        throw DuplicateProductException("A product with barcode '$barcode' already exists: '${existingByBarcode.name}'.")
      }
    }

    // 3. Check equivalent identity: Name + Brand + Unit + Specification
    val allActive = productDao.getActiveProductsSnapshot()
    val candidateKey = product.toIdentityKey()
    val duplicate = allActive.firstOrNull { it.id != excludeId && it.toIdentityKey() == candidateKey }
    if (duplicate != null) {
      val brandText = if (!product.brand.isNullOrBlank()) " (${product.brand})" else ""
      val specText = if (!product.specification.isNullOrBlank()) " - ${product.specification}" else ""
      val unitText = if (product.unit == "CUSTOM") product.customUnit ?: "Custom" else product.unit
      throw DuplicateProductException(
        "Duplicate product detected: '${product.name}$brandText$specText [$unitText]' already exists in your inventory."
      )
    }
  }
}
