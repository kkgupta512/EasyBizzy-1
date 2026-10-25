package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.BillEntity
import com.example.data.repository.BillingRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.KhataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
  val salesTodayPaise: Long = 0L,
  val billsTodayCount: Int = 0,
  val receivablesLenaPaise: Long = 0L,
  val payablesDenaPaise: Long = 0L,
  val expensesTodayPaise: Long = 0L,
  val recentBills: List<BillEntity> = emptyList(),
)

/**
 * ViewModel feeding real reactive metrics from the Room database to the Shopkeeper Dashboard.
 * When no records exist, values evaluate to zero/empty, cleanly triggering labeled zero-states.
 */
class ShopkeeperDashboardViewModel(
  billingRepository: BillingRepository,
  khataRepository: KhataRepository,
  expenseRepository: ExpenseRepository,
) : ViewModel() {

  private val billingFlow = combine(
    billingRepository.getTodaySalesPaise(),
    billingRepository.getTodayBillsCount(),
    billingRepository.allBills,
  ) { sales, count, bills ->
    Triple(sales, count, bills)
  }

  private val ledgerAndExpenseFlow = combine(
    khataRepository.totalReceivablesPaise,
    khataRepository.totalPayablesPaise,
    expenseRepository.getTodayExpensesPaise(),
  ) { receivables, payables, expenses ->
    Triple(receivables, payables, expenses)
  }

  val uiState: StateFlow<DashboardUiState> = combine(
    billingFlow,
    ledgerAndExpenseFlow,
  ) { (sales, count, bills), (receivables, payables, expenses) ->
    DashboardUiState(
      salesTodayPaise = sales,
      billsTodayCount = count,
      receivablesLenaPaise = receivables,
      payablesDenaPaise = payables,
      expensesTodayPaise = expenses,
      recentBills = bills.take(5),
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DashboardUiState(),
  )

  class Factory(
    private val billingRepository: BillingRepository,
    private val khataRepository: KhataRepository,
    private val expenseRepository: ExpenseRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return ShopkeeperDashboardViewModel(billingRepository, khataRepository, expenseRepository) as T
    }
  }
}
