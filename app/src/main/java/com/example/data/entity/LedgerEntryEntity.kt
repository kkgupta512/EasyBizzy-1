package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Khata ledger transaction entry.
 * partyType: "CUSTOMER" or "SUPPLIER"
 * entryType:
 * "GAVE" -> Merchant gave goods or money (Customer Udhar / Supplier advance)
 * "GOT"  -> Merchant received payment (Customer Jama / Supplier settlement)
 */
@Entity(
  tableName = "ledger_entries",
  indices = [
    Index("partyId"),
    Index("partyType"),
    Index("timestamp"),
  ]
)
data class LedgerEntryEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val partyId: String,
  val partyType: String, // "CUSTOMER" or "SUPPLIER"
  val entryType: String, // "GAVE" or "GOT"
  val amountPaise: Long,
  val billId: String? = null,
  val note: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
)
