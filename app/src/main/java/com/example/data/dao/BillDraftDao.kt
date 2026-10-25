package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BillDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDraftDao {
  @Query("SELECT * FROM bill_draft WHERE id = 1 LIMIT 1")
  fun getDraft(): Flow<BillDraftEntity?>

  @Query("SELECT * FROM bill_draft WHERE id = 1 LIMIT 1")
  suspend fun getDraftSnapshot(): BillDraftEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveDraft(draft: BillDraftEntity)

  @Query("DELETE FROM bill_draft WHERE id = 1")
  suspend fun clearDraft()
}
