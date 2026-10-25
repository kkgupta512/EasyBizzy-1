package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Business operational expense record.
 * category: "RENT", "STAFF_WAGE", "ELECTRICITY", "TEA_SNACKS", "TRANSPORT", "MAINTENANCE", "OTHER"
 */
@Entity(
  tableName = "expenses",
  indices = [
    Index("category"),
    Index("timestamp"),
  ]
)
data class ExpenseEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val category: String,
  val amountPaise: Long,
  val paymentMode: String = "CASH", // "CASH", "UPI"
  val note: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
)
