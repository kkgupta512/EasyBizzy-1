package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.BillWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
  @Query("SELECT * FROM bills ORDER BY createdAt DESC")
  fun getAllBills(): Flow<List<BillEntity>>

  @Transaction
  @Query("SELECT * FROM bills ORDER BY createdAt DESC")
  fun getAllBillsWithItems(): Flow<List<BillWithItems>>

  @Transaction
  @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
  suspend fun getBillWithItemsById(id: String): BillWithItems?

  @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
  fun getBillById(id: String): Flow<BillEntity?>

  @Query("SELECT * FROM bills WHERE createdAt >= :startOfDayTimestamp ORDER BY createdAt DESC")
  fun getBillsSince(startOfDayTimestamp: Long): Flow<List<BillEntity>>

  @Query("SELECT COUNT(*) FROM bills WHERE createdAt >= :startOfDayTimestamp")
  fun getBillsCountSince(startOfDayTimestamp: Long): Flow<Int>

  @Query("SELECT COALESCE(SUM(netAmountPaise), 0) FROM bills WHERE createdAt >= :startOfDayTimestamp")
  fun getTotalSalesSince(startOfDayTimestamp: Long): Flow<Long>

  @Query("SELECT billNumber FROM bills WHERE billNumber LIKE :prefixPattern ORDER BY billNumber DESC LIMIT 1")
  suspend fun getHighestBillNumberWithPrefix(prefixPattern: String): String?

  @Query("SELECT COUNT(*) FROM bills WHERE billNumber LIKE :prefixPattern")
  suspend fun getBillCountWithPrefix(prefixPattern: String): Int

  @Query("SELECT * FROM bills WHERE billNumber = :billNumber LIMIT 1")
  suspend fun getBillByNumber(billNumber: String): BillEntity?

  @Query("SELECT * FROM bills WHERE paymentStatus = :status ORDER BY createdAt DESC")
  fun getBillsByPaymentStatus(status: String): Flow<List<BillEntity>>

  @Query("""
    SELECT * FROM bills 
    WHERE billNumber LIKE '%' || :query || '%' 
       OR customerName LIKE '%' || :query || '%' 
       OR customerPhone LIKE '%' || :query || '%'
    ORDER BY createdAt DESC
  """)
  fun searchBills(query: String): Flow<List<BillEntity>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertBill(bill: BillEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBillItems(items: List<BillItemEntity>)

  @Update
  suspend fun updateBill(bill: BillEntity)

  @Transaction
  suspend fun insertBillWithItems(bill: BillEntity, items: List<BillItemEntity>) {
    insertBill(bill)
    insertBillItems(items)
  }

  @Query("DELETE FROM bills WHERE id = :id")
  suspend fun deleteBillById(id: String)
}
