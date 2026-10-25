package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Public marketplace broadcasting preferences.
 * Strictly decoupled from private ledger and accounting tables.
 */
@Entity(tableName = "public_publishing_preferences")
data class PublicPublishingPreferencesEntity(
  @PrimaryKey val id: Int = 1,
  val isMarketplacePublished: Boolean = false,
  val publicShopName: String = "",
  val publicCategory: String = "Kirana & Grocery",
  val publicAddress: String = "",
  val publicPincode: String = "",
  val publicPhone: String = "",
  val publicUpiId: String? = null,
  val autoSyncPublishedProducts: Boolean = true,
  val lastSyncTimestamp: Long? = null,
)
