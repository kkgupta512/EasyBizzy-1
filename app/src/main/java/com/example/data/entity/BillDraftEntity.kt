package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton entity storing the in-progress bill draft in Room.
 * Protects against data loss if the app closes or crashes unexpectedly during checkout entry.
 */
@Entity(tableName = "bill_draft")
data class BillDraftEntity(
  @PrimaryKey val id: Int = 1,
  val customerName: String = "",
  val customerPhone: String = "",
  val customerId: String? = null,
  val customerGstin: String = "",
  val paymentMode: String = "CASH",
  val billDiscountRupees: String = "",
  val cashReceivedRupees: String = "",
  val upiReceivedRupees: String = "",
  val isGstBill: Boolean = false,
  val applyRoundOff: Boolean = true,
  val notes: String = "",
  val itemsJson: String = "[]",
  val updatedAt: Long = System.currentTimeMillis(),
)
