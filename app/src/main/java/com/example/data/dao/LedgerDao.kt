package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
  @Query("SELECT * FROM ledger_entries WHERE partyId = :partyId ORDER BY timestamp DESC")
  fun getEntriesForParty(partyId: String): Flow<List<LedgerEntryEntity>>

  @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentEntries(limit: Int = 20): Flow<List<LedgerEntryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEntry(entry: LedgerEntryEntity)

  @Query("DELETE FROM ledger_entries WHERE id = :id")
  suspend fun deleteEntryById(id: String)
}
