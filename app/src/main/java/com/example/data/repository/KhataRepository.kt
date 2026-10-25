package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.LedgerDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.SupplierDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.LedgerEntryEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository orchestrating Digital Udhar Bahi-Khata ledgers,
 * Lena/Dena calculations, and transaction histories.
 */
class KhataRepository(
  private val customerDao: CustomerDao,
  private val supplierDao: SupplierDao,
  private val ledgerDao: LedgerDao,
  private val paymentDao: PaymentDao,
) {
  val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
  val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
  val totalReceivablesPaise: Flow<Long> = customerDao.getTotalReceivablesPaise()
  val totalPayablesPaise: Flow<Long> = supplierDao.getTotalPayablesPaise()

  suspend fun addCustomer(customer: CustomerEntity) {
    customerDao.insertCustomer(customer)
  }

  suspend fun addSupplier(supplier: SupplierEntity) {
    supplierDao.insertSupplier(supplier)
  }

  fun getEntriesForParty(partyId: String): Flow<List<LedgerEntryEntity>> {
    return ledgerDao.getEntriesForParty(partyId)
  }

  /**
   * Records a Customer transaction:
   * "GAVE" -> Merchant gave goods or credit (+ balance)
   * "GOT"  -> Customer paid money (- balance)
   */
  suspend fun recordCustomerTransaction(
    customerId: String,
    entryType: String,
    amountPaise: Long,
    note: String?,
  ) {
    val delta = if (entryType == "GAVE") amountPaise else -amountPaise
    customerDao.adjustCustomerBalance(customerId, delta)

    ledgerDao.insertEntry(
      LedgerEntryEntity(
        partyId = customerId,
        partyType = "CUSTOMER",
        entryType = entryType,
        amountPaise = amountPaise,
        note = note,
      )
    )

    if (entryType == "GOT") {
      paymentDao.insertPayment(
        PaymentEntity(
          partyId = customerId,
          amountPaise = amountPaise,
          paymentMode = "CASH",
          note = note ?: "Khata Payment Received",
        )
      )
    }
  }

  /**
   * Records a Supplier transaction:
   * "GAVE" -> Merchant paid supplier (- balance)
   * "GOT"  -> Merchant received stock on credit (+ balance)
   */
  suspend fun recordSupplierTransaction(
    supplierId: String,
    entryType: String,
    amountPaise: Long,
    note: String?,
  ) {
    val delta = if (entryType == "GOT") amountPaise else -amountPaise
    supplierDao.adjustSupplierBalance(supplierId, delta)

    ledgerDao.insertEntry(
      LedgerEntryEntity(
        partyId = supplierId,
        partyType = "SUPPLIER",
        entryType = entryType,
        amountPaise = amountPaise,
        note = note,
      )
    )
  }
}
