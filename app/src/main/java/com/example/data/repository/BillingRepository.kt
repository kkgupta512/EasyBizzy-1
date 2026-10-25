package com.example.data.repository

import com.example.data.dao.BillDao
import com.example.data.dao.BillDraftDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.LedgerDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.ProductDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.entity.BillDraftEntity
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.BillWithItems
import com.example.data.entity.LedgerEntryEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ShopSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Repository orchestrating offline-first billing transactions, collision-free invoice numbering,
 * immutable line snapshots, payment tracking, Khata debiting, and crash-resilient draft persistence.
 */
class BillingRepository(
  private val billDao: BillDao,
  private val customerDao: CustomerDao,
  private val ledgerDao: LedgerDao,
  private val paymentDao: PaymentDao,
  private val productDao: ProductDao? = null,
  private val billDraftDao: BillDraftDao? = null,
  private val shopSettingsDao: ShopSettingsDao? = null,
) {
  val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
  val allBillsWithItems: Flow<List<BillWithItems>> = billDao.getAllBillsWithItems()

  fun getTodaySalesPaise(): Flow<Long> {
    val startOfDay = getStartOfTodayEpochMillis()
    return billDao.getTotalSalesSince(startOfDay)
  }

  fun getTodayBillsCount(): Flow<Int> {
    val startOfDay = getStartOfTodayEpochMillis()
    return billDao.getBillsCountSince(startOfDay)
  }

  fun searchBills(query: String): Flow<List<BillEntity>> {
    return billDao.searchBills(query.trim())
  }

  fun getBillsByPaymentStatus(status: String): Flow<List<BillEntity>> {
    return billDao.getBillsByPaymentStatus(status)
  }

  suspend fun getBillDetails(billId: String): BillWithItems? {
    return billDao.getBillWithItemsById(billId)
  }

  fun getShopSettings(): Flow<ShopSettingsEntity?> {
    return shopSettingsDao?.getSettings() ?: kotlinx.coroutines.flow.flowOf(null)
  }

  suspend fun getShopSettingsOnce(): ShopSettingsEntity? {
    return shopSettingsDao?.getSettingsOnce()
  }

  // --- Collision-Free Unique Bill Number Generation ---
  suspend fun generateNextBillNumber(): String {
    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(Date())
    val prefix = "EB-$dateStr"
    val prefixPattern = "$prefix-%"

    val highestNumber = billDao.getHighestBillNumberWithPrefix(prefixPattern)
    val nextSeq = if (highestNumber != null && highestNumber.startsWith("$prefix-")) {
      val suffix = highestNumber.substringAfterLast("-")
      val num = suffix.toIntOrNull() ?: 0
      num + 1
    } else {
      val count = billDao.getBillCountWithPrefix(prefixPattern)
      count + 1
    }

    return String.format(Locale.ROOT, "%s-%04d", prefix, nextSeq)
  }

  // --- Draft Autosave & Failure Recovery ---
  fun getDraft(): Flow<BillDraftEntity?> {
    return billDraftDao?.getDraft() ?: kotlinx.coroutines.flow.flowOf(null)
  }

  suspend fun getDraftSnapshot(): BillDraftEntity? {
    return billDraftDao?.getDraftSnapshot()
  }

  suspend fun saveDraft(draft: BillDraftEntity) {
    billDraftDao?.saveDraft(draft)
  }

  suspend fun clearDraft() {
    billDraftDao?.clearDraft()
  }

  // --- Finalize and Create Bill ---
  /**
   * Saves the bill and items to Room.
   * - Retries with next sequential bill number if collision is encountered.
   * - Deducts product inventory stock for tracked items.
   * - Records cash/UPI payments in PaymentDao.
   * - If balance due remains and a customer is attached, debits customer balance and writes ledger entry.
   * - Clears the in-progress draft.
   */
  suspend fun createBill(
    bill: BillEntity,
    items: List<BillItemEntity>,
  ): BillEntity {
    var finalBill = bill
    var attempts = 0
    var savedSuccessfully = false

    while (attempts < 5 && !savedSuccessfully) {
      try {
        billDao.insertBillWithItems(finalBill, items)
        savedSuccessfully = true
      } catch (e: Exception) {
        val msg = e.message?.lowercase() ?: ""
        if (msg.contains("unique") || msg.contains("constraint")) {
          attempts++
          val nextNumber = generateNextBillNumber()
          finalBill = finalBill.copy(billNumber = nextNumber)
        } else {
          throw e
        }
      }
    }

    if (!savedSuccessfully) {
      // Final attempt with timestamp nonce to guarantee insertion
      val fallbackNumber = "EB-" + System.currentTimeMillis()
      finalBill = finalBill.copy(billNumber = fallbackNumber)
      billDao.insertBillWithItems(finalBill, items)
    }

    // 1. Deduct Product Inventory Stock
    if (productDao != null) {
      for (item in items) {
        if (item.productId != null && item.quantity > 0.0) {
          productDao.deductStock(item.productId, item.quantity)
        }
      }
    }

    // 2. Record Payment Records
    if (finalBill.cashReceivedPaise > 0L) {
      paymentDao.insertPayment(
        PaymentEntity(
          billId = finalBill.id,
          partyId = finalBill.customerId,
          amountPaise = finalBill.cashReceivedPaise,
          paymentMode = "CASH",
          note = "Bill ${finalBill.billNumber} Cash",
          timestamp = finalBill.createdAt,
        )
      )
    }
    if (finalBill.upiReceivedPaise > 0L) {
      paymentDao.insertPayment(
        PaymentEntity(
          billId = finalBill.id,
          partyId = finalBill.customerId,
          amountPaise = finalBill.upiReceivedPaise,
          paymentMode = "UPI",
          note = "Bill ${finalBill.billNumber} UPI",
          timestamp = finalBill.createdAt,
        )
      )
    }

    // 3. Khata (Credit / Udhar) Debit for remaining balance
    val balanceDue = finalBill.balanceDuePaise
    if (balanceDue > 0L && finalBill.customerId != null) {
      customerDao.adjustCustomerBalance(finalBill.customerId, balanceDue)
      ledgerDao.insertEntry(
        LedgerEntryEntity(
          partyId = finalBill.customerId,
          partyType = "CUSTOMER",
          entryType = "GAVE",
          amountPaise = balanceDue,
          billId = finalBill.id,
          note = "Bill ${finalBill.billNumber} Credit Balance",
          timestamp = finalBill.createdAt,
        )
      )
    }

    // 4. Clear in-progress draft
    billDraftDao?.clearDraft()

    return finalBill
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
