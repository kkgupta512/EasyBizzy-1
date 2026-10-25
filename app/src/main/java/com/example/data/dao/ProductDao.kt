package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
  @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY name ASC")
  fun getActiveProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isArchived = 1 ORDER BY updatedAt DESC")
  fun getArchivedProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products ORDER BY name ASC")
  fun getAllProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isArchived = 0")
  suspend fun getActiveProductsSnapshot(): List<ProductEntity>

  @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
  suspend fun getProductById(id: String): ProductEntity?

  @Query("SELECT * FROM products WHERE sku = :sku AND id != :excludeId LIMIT 1")
  suspend fun getProductBySkuExcluding(sku: String, excludeId: String): ProductEntity?

  @Query("SELECT * FROM products WHERE barcode = :barcode AND id != :excludeId LIMIT 1")
  suspend fun getProductByBarcodeExcluding(barcode: String, excludeId: String): ProductEntity?

  @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
  suspend fun getProductBySku(sku: String): ProductEntity?

  @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
  suspend fun getProductByBarcode(barcode: String): ProductEntity?

  @Query("SELECT * FROM products WHERE isMarketplacePublished = 1 AND isArchived = 0")
  fun getMarketplacePublishedProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isArchived = 0 AND currentStock IS NOT NULL AND currentStock <= minStockAlert")
  fun getLowStockProducts(): Flow<List<ProductEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<ProductEntity>)

  @Update
  suspend fun updateProduct(product: ProductEntity)

  @Query("UPDATE products SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
  suspend fun setArchivedStatus(id: String, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())

  @Delete
  suspend fun deleteProduct(product: ProductEntity)

  @Query("DELETE FROM products WHERE id = :id")
  suspend fun deleteProductById(id: String)

  @Query("UPDATE products SET currentStock = currentStock - :quantitySold, updatedAt = :updatedAt WHERE id = :id AND currentStock IS NOT NULL")
  suspend fun deductStock(id: String, quantitySold: Double, updatedAt: Long = System.currentTimeMillis())
}
