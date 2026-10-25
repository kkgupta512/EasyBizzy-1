package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Line item in a sales bill.
 * Preserves historical descriptions, brands, specifications, units, quantities, prices, discounts, and taxes
 * even if the original product is later modified or deleted.
 */
@Entity(
  tableName = "bill_items",
  foreignKeys = [
    ForeignKey(
      entity = BillEntity::class,
      parentColumns = ["id"],
      childColumns = ["billId"],
      onDelete = ForeignKey.CASCADE,
    ),
    ForeignKey(
      entity = ProductEntity::class,
      parentColumns = ["id"],
      childColumns = ["productId"],
      onDelete = ForeignKey.SET_NULL,
    )
  ],
  indices = [
    Index("billId"),
    Index("productId"),
  ]
)
data class BillItemEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val billId: String,
  val productId: String? = null,
  // Historical snapshot fields - immutable once billed
  val productName: String,
  val brand: String? = null,
  val specification: String? = null,
  val unit: String = "PCS",
  val quantity: Double,
  val unitPricePaise: Long,
  val discountPaise: Long = 0L,
  val taxRatePercent: Double = 0.0,
  val taxPaise: Long = 0L,
  val totalPricePaise: Long,
)
