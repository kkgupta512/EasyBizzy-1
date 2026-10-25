package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ShopSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopSettingsDao {
  @Query("SELECT * FROM shop_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<ShopSettingsEntity?>

  @Query("SELECT * FROM shop_settings WHERE id = 1 LIMIT 1")
  suspend fun getSettingsOnce(): ShopSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateSettings(settings: ShopSettingsEntity)
}
