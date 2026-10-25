package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BackupMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupMetadataDao {
  @Query("SELECT * FROM backup_metadata ORDER BY timestamp DESC")
  fun getAllBackups(): Flow<List<BackupMetadataEntity>>

  @Query("SELECT * FROM backup_metadata WHERE status = 'SUCCESS' ORDER BY timestamp DESC LIMIT 1")
  fun getLatestSuccessfulBackup(): Flow<BackupMetadataEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBackupRecord(record: BackupMetadataEntity)
}
