package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Invoice / Sales Bill Entity.
 * Contains invoice summary, seller & customer snapshots, tax breakdown, and payment status in paise.
 */
@Entity(
  tableName = "bills",
  foreignKeys = [
    ForeignKey(
      entity = CustomerEntity::class,
      parentColumns = ["id"],
      childColumns = ["customerId"],
      onDelete = ForeignKey.SET_NULL,
    )
  ],
  indices = [
    Index("billNumber", unique = true),
    Index("customerId"),
    Index("createdAt"),
    Index("paymentStatus"),
  ]
)
data class BillEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val billNumber: String,
  val customerId: String? = null,
  val customerName: String? = null,
  val customerPhone: String? = null,
  val customerGstin: String? = null,
  val subtotalPaise: Long = 0L,
  val discountPaise: Long = 0L,
  val taxPaise: Long = 0L,
  val roundOffPaise: Long = 0L,
  val netAmountPaise: Long = 0L,
  val paymentMode: String = "CASH", // "CASH", "UPI", "KHATA", "SPLIT"
  val paymentStatus: String = "PAID", // "PAID", "PARTIALLY_PAID", "UNPAID"
  val cashReceivedPaise: Long = 0L,
  val upiReceivedPaise: Long = 0L,
  val khataDebitedPaise: Long = 0L,
  val paidAmountPaise: Long = 0L,
  val balanceDuePaise: Long = 0L,
  val isGstBill: Boolean = false,
  val sellerGstin: String? = null,
  val notes: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
)
