package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton entity storing merchant preferences and counter setup.
 */
@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val shopName: String = "My Store",
  val ownerName: String = "Store Owner",
  val phone: String = "",
  val address: String = "",
  val pincode: String = "",
  val upiId: String? = null,
  val gstin: String? = null,
  val defaultGstRate: Double = 0.0,
  val receiptFooter: String = "Thank you for shopping with us! Visit again.",
  val biometricLockEnabled: Boolean = false,
  val updatedAt: Long = System.currentTimeMillis(),
)
