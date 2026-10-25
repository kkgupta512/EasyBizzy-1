package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Customer Khata Account.
 * currentBalancePaise:
 * Positive = Customer owes merchant (Lena / Udhar).
 * Negative = Merchant has advance from customer.
 */
@Entity(
  tableName = "customers",
  indices = [Index("phone")]
)
data class CustomerEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val name: String,
  val phone: String,
  val address: String? = null,
  val currentBalancePaise: Long = 0L,
  val creditLimitPaise: Long = 0L,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)
