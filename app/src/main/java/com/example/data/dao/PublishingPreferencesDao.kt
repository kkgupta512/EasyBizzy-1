package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.PublicPublishingPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PublishingPreferencesDao {
  @Query("SELECT * FROM public_publishing_preferences WHERE id = 1 LIMIT 1")
  fun getPreferences(): Flow<PublicPublishingPreferencesEntity?>

  @Query("SELECT * FROM public_publishing_preferences WHERE id = 1 LIMIT 1")
  suspend fun getPreferencesOnce(): PublicPublishingPreferencesEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdatePreferences(preferences: PublicPublishingPreferencesEntity)
}
