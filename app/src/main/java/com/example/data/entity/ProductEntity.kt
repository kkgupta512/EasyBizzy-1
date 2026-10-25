package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Product inventory entity supporting retail, hardware, electrical, and building materials.
 * Differentiates products by specification/size and unit to prevent false equivalence.
 */
@Entity(
  tableName = "products",
  indices = [
    Index("sku"),
    Index("barcode"),
    Index("category"),
    Index("isMarketplacePublished"),
    Index("isArchived"),
  ]
)
data class ProductEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val name: String,
  val brand: String? = null,              // e.g. "Havells", "UltraTech", "Finolex", "Tata Tiscon"
  val sku: String? = null,                // SKU / Product / Part code
  val barcode: String? = null,
  val specification: String? = null,      // e.g. "1.5 sq mm 90m", "12mm TMT 40ft", "Grade 53 50kg"
  val category: String = "General",       // "Hardware", "Electrical", "Building Material", "Plumbing", "Paint", "Kirana"
  val unit: String = "PIECE",             // "PIECE", "BOX", "PACKET", "BAG", "METRE", "KILOGRAM", "CUSTOM"
  val customUnit: String? = null,         // when unit == "CUSTOM", e.g. "Litre", "Bundle", "Roll"
  val purchasePricePaise: Long = 0L,      // Wholesale purchase cost in paise (strictly private)
  val sellingPricePaise: Long = 0L,       // Counter retail price in paise
  val currentStock: Double? = 0.0,        // Optional current inventory stock
  val minStockAlert: Double = 5.0,        // Low stock warning threshold
  val isArchived: Boolean = false,        // Soft-delete / Archival flag
  val isMarketplacePublished: Boolean = false,
  val marketplacePricePaise: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
) {
  /**
   * Unique identity key for duplicate detection:
   * Considers Name + Brand + Unit + Specification.
   */
  fun toIdentityKey(): String {
    val cleanName = name.trim().lowercase()
    val cleanBrand = (brand ?: "").trim().lowercase()
    val cleanUnit = (if (unit == "CUSTOM") customUnit ?: "custom" else unit).trim().lowercase()
    val cleanSpec = (specification ?: "").trim().lowercase()
    return "$cleanName|$cleanBrand|$cleanUnit|$cleanSpec"
  }
}
