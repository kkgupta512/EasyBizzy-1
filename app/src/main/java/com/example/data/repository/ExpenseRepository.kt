package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository tracking daily business operational expenses and cash drawer impact.
 */
class ExpenseRepository(
  private val expenseDao: ExpenseDao,
) {
  val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

  fun getTodayExpensesPaise(): Flow<Long> {
    val startOfDay = getStartOfTodayEpochMillis()
    return expenseDao.getTotalExpensesSince(startOfDay)
  }

  suspend fun addExpense(expense: ExpenseEntity) {
    expenseDao.insertExpense(expense)
  }

  suspend fun deleteExpense(expense: ExpenseEntity) {
    expenseDao.deleteExpense(expense)
  }

  companion object {
    fun getStartOfTodayEpochMillis(): Long {
      val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      return calendar.timeInMillis
    }
  }
}
