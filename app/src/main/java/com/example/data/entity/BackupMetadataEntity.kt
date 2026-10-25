package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Metadata record for encrypted backups exported locally or to Google Drive.
 */
@Entity(
  tableName = "backup_metadata",
  indices = [Index("timestamp")]
)
data class BackupMetadataEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val backupType: String, // "LOCAL_FILE", "GOOGLE_DRIVE"
  val targetPathOrDriveId: String,
  val recordCount: Int,
  val fileSizeBytes: Long,
  val encryptionAlgorithm: String = "AES-256-GCM",
  val status: String = "SUCCESS", // "SUCCESS", "FAILED"
  val errorMessage: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
)
