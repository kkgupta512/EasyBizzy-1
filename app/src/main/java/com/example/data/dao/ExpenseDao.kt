package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp DESC")
  fun getExpensesSince(startOfDayTimestamp: Long): Flow<List<ExpenseEntity>>

  @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE timestamp >= :startOfDayTimestamp")
  fun getTotalExpensesSince(startOfDayTimestamp: Long): Flow<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity)

  @Delete
  suspend fun deleteExpense(expense: ExpenseEntity)
}
