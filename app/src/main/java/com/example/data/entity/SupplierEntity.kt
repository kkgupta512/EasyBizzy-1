package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Supplier / Distributor Khata Account.
 * currentBalancePaise:
 * Positive = Merchant owes supplier (Dena / Liability).
 * Negative = Merchant has advance with supplier.
 */
@Entity(
  tableName = "suppliers",
  indices = [Index("phone")]
)
data class SupplierEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val name: String,
  val phone: String,
  val companyName: String? = null,
  val address: String? = null,
  val gstin: String? = null,
  val currentBalancePaise: Long = 0L,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)
