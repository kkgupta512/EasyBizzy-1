package com.example.data.repository

import com.example.data.dao.PublishingPreferencesDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.entity.PublicPublishingPreferencesEntity
import com.example.data.entity.ShopSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository managing single-row merchant settings and decoupled public marketplace parameters.
 */
class ShopSettingsRepository(
  private val shopSettingsDao: ShopSettingsDao,
  private val publishingPreferencesDao: PublishingPreferencesDao,
) {
  val settings: Flow<ShopSettingsEntity?> = shopSettingsDao.getSettings()
  val publishingPreferences: Flow<PublicPublishingPreferencesEntity?> = publishingPreferencesDao.getPreferences()

  suspend fun saveSettings(settings: ShopSettingsEntity) {
    shopSettingsDao.insertOrUpdateSettings(settings.copy(updatedAt = System.currentTimeMillis()))
  }

  suspend fun savePublishingPreferences(preferences: PublicPublishingPreferencesEntity) {
    publishingPreferencesDao.insertOrUpdatePreferences(preferences)
  }

  suspend fun getSettingsSnapshot(): ShopSettingsEntity {
    return shopSettingsDao.getSettingsOnce() ?: ShopSettingsEntity()
  }
}
