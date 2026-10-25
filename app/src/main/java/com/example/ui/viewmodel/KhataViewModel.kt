package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.CustomerEntity
import com.example.data.entity.SupplierEntity
import com.example.data.repository.KhataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KhataUiState(
  val customers: List<CustomerEntity> = emptyList(),
  val suppliers: List<SupplierEntity> = emptyList(),
  val totalReceivablesPaise: Long = 0L,
  val totalPayablesPaise: Long = 0L,
)

/**
 * ViewModel managing Customer and Supplier credit ledgers.
 */
class KhataViewModel(
  private val khataRepository: KhataRepository,
) : ViewModel() {

  val uiState: StateFlow<KhataUiState> = combine(
    khataRepository.allCustomers,
    khataRepository.allSuppliers,
    khataRepository.totalReceivablesPaise,
    khataRepository.totalPayablesPaise,
  ) { customers, suppliers, lena, dena ->
    KhataUiState(
      customers = customers,
      suppliers = suppliers,
      totalReceivablesPaise = lena,
      totalPayablesPaise = dena,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = KhataUiState(),
  )

  fun addCustomer(name: String, phone: String, initialUdharPaise: Long = 0L) {
    viewModelScope.launch {
      val customer = CustomerEntity(
        name = name,
        phone = phone,
        currentBalancePaise = initialUdharPaise,
      )
      khataRepository.addCustomer(customer)
      if (initialUdharPaise > 0L) {
        khataRepository.recordCustomerTransaction(
          customerId = customer.id,
          entryType = "GAVE",
          amountPaise = initialUdharPaise,
          note = "Opening Balance",
        )
      }
    }
  }

  fun addSupplier(name: String, phone: String, initialDenaPaise: Long = 0L) {
    viewModelScope.launch {
      val supplier = SupplierEntity(
        name = name,
        phone = phone,
        currentBalancePaise = initialDenaPaise,
      )
      khataRepository.addSupplier(supplier)
    }
  }

  fun recordTransaction(partyId: String, isCustomer: Boolean, entryType: String, amountPaise: Long, note: String?) {
    viewModelScope.launch {
      if (isCustomer) {
        khataRepository.recordCustomerTransaction(partyId, entryType, amountPaise, note)
      } else {
        khataRepository.recordSupplierTransaction(partyId, entryType, amountPaise, note)
      }
    }
  }

  class Factory(private val khataRepository: KhataRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return KhataViewModel(khataRepository) as T
    }
  }
}
