package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Payment transaction record for cash, UPI, or bank settlements.
 */
@Entity(
  tableName = "payments",
  indices = [
    Index("billId"),
    Index("partyId"),
    Index("timestamp"),
  ]
)
data class PaymentEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val billId: String? = null,
  val partyId: String? = null,
  val amountPaise: Long,
  val paymentMode: String = "CASH", // "CASH", "UPI", "BANK_TRANSFER"
  val transactionReference: String? = null, // UPI UTR or bank ref
  val note: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
)
