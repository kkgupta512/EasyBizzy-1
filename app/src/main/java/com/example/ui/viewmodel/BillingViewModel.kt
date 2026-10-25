package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.BillDraftEntity
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.data.repository.BillingRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.KhataRepository
import com.example.data.util.DraftSerializer
import com.example.data.util.LineItemCalculation
import com.example.data.util.Money
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

data class DraftCartItem(
  val id: String = UUID.randomUUID().toString(),
  val productId: String? = null,
  val productName: String,
  val brand: String? = null,
  val specification: String? = null,
  val unit: String = "PCS",
  val quantity: Double = 1.0,
  val unitPricePaise: Long = 0L,
  val discountPaise: Long = 0L,
  val taxRatePercent: Double = 0.0,
) {
  val calculation: LineItemCalculation
    get() = Money.calculateLineItem(quantity, unitPricePaise, discountPaise, taxRatePercent)

  val lineTotalPaise: Long
    get() = calculation.lineTotalPaise
}

data class BillingUiState(
  val customerName: String = "",
  val customerPhone: String = "",
  val customerId: String? = null,
  val customerGstin: String = "",
  val paymentMode: String = "CASH", // "CASH", "UPI", "KHATA", "SPLIT"
  val items: List<DraftCartItem> = emptyList(),
  val billDiscountRupees: String = "",
  val cashReceivedRupees: String = "",
  val upiReceivedRupees: String = "",
  val isGstBill: Boolean = false,
  val applyRoundOff: Boolean = true,
  val notes: String = "",
  val isSaving: Boolean = false,
  val lastSavedBill: BillEntity? = null,
  val shopSettings: ShopSettingsEntity? = null,
  val availableProducts: List<ProductEntity> = emptyList(),
  val availableCustomers: List<CustomerEntity> = emptyList(),
) {
  val billDiscountPaise: Long
    get() = Money.toPaise(billDiscountRupees.toDoubleOrNull() ?: 0.0)

  val totals = Money.calculateBillTotals(
    lineCalculations = items.map { it.calculation },
    billDiscountPaise = billDiscountPaise,
    applyRoundOff = applyRoundOff,
  )

  val subtotalPaise: Long get() = totals.grossSubtotalPaise
  val totalDiscountPaise: Long get() = totals.totalDiscountPaise
  val taxableSubtotalPaise: Long get() = totals.taxableSubtotalPaise
  val taxPaise: Long get() = totals.totalTaxPaise
  val roundOffPaise: Long get() = totals.roundOffPaise
  val netAmountPaise: Long get() = totals.finalNetPaise

  val cashTenderedPaise: Long
    get() = Money.toPaise(cashReceivedRupees.toDoubleOrNull() ?: 0.0)

  val upiTenderedPaise: Long
    get() = Money.toPaise(upiReceivedRupees.toDoubleOrNull() ?: 0.0)

  val paidAmountPaise: Long
    get() {
      return when (paymentMode) {
        "CASH" -> {
          // If cash input is empty or higher, paid is min(netAmount, cashTendered)
          if (cashReceivedRupees.isBlank()) netAmountPaise
          else cashTenderedPaise.coerceAtMost(netAmountPaise)
        }
        "UPI" -> {
          if (upiReceivedRupees.isBlank()) netAmountPaise
          else upiTenderedPaise.coerceAtMost(netAmountPaise)
        }
        "KHATA" -> 0L // Full unpaid on credit
        "SPLIT" -> (cashTenderedPaise + upiTenderedPaise).coerceAtMost(netAmountPaise)
        else -> netAmountPaise
      }
    }

  val changeToReturnPaise: Long
    get() {
      if (paymentMode == "CASH" && cashTenderedPaise > netAmountPaise) {
        return cashTenderedPaise - netAmountPaise
      }
      return 0L
    }

  val balanceDuePaise: Long
    get() = max(0L, netAmountPaise - paidAmountPaise)

  val paymentStatus: String
    get() {
      return when {
        netAmountPaise <= 0L -> "PAID"
        paidAmountPaise >= netAmountPaise -> "PAID"
        paidAmountPaise > 0L -> "PARTIALLY_PAID"
        else -> "UNPAID"
      }
    }
}

/**
 * ViewModel for POS Billing checkout and invoice creation.
 * Fully offline-first with automatic crash-resilient draft persistence.
 */
class BillingViewModel(
  private val billingRepository: BillingRepository,
  private val inventoryRepository: InventoryRepository? = null,
  private val khataRepository: KhataRepository? = null,
) : ViewModel() {

  private val _uiState = MutableStateFlow(BillingUiState())
  val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

  private var autosaveJob: Job? = null

  init {
    loadSettingsAndCatalog()
    restoreDraftFromLocalDb()
  }

  private fun loadSettingsAndCatalog() {
    viewModelScope.launch {
      val settings = billingRepository.getShopSettingsOnce()
      val isGst = !settings?.gstin.isNullOrBlank()
      _uiState.update { it.copy(shopSettings = settings, isGstBill = isGst) }

      // Listen for products if repository provided
      inventoryRepository?.activeProducts?.collect { prods ->
        _uiState.update { it.copy(availableProducts = prods) }
      }
    }

    viewModelScope.launch {
      khataRepository?.allCustomers?.collect { custs ->
        _uiState.update { it.copy(availableCustomers = custs) }
      }
    }
  }

  private fun restoreDraftFromLocalDb() {
    viewModelScope.launch {
      val draft = billingRepository.getDraftSnapshot() ?: return@launch
      val restoredItems = DraftSerializer.deserializeItems(draft.itemsJson)

      if (restoredItems.isNotEmpty() || draft.customerName.isNotBlank() || draft.customerPhone.isNotBlank()) {
        _uiState.update { current ->
          current.copy(
            customerName = draft.customerName,
            customerPhone = draft.customerPhone,
            customerId = draft.customerId,
            customerGstin = draft.customerGstin,
            paymentMode = draft.paymentMode,
            billDiscountRupees = draft.billDiscountRupees,
            cashReceivedRupees = draft.cashReceivedRupees,
            upiReceivedRupees = draft.upiReceivedRupees,
            isGstBill = draft.isGstBill,
            applyRoundOff = draft.applyRoundOff,
            notes = draft.notes,
            items = restoredItems,
          )
        }
      }
    }
  }

  private fun triggerAutosave() {
    autosaveJob?.cancel()
    autosaveJob = viewModelScope.launch {
      delay(300) // Debounce rapid keystrokes
      val s = _uiState.value
      val draft = BillDraftEntity(
        customerName = s.customerName,
        customerPhone = s.customerPhone,
        customerId = s.customerId,
        customerGstin = s.customerGstin,
        paymentMode = s.paymentMode,
        billDiscountRupees = s.billDiscountRupees,
        cashReceivedRupees = s.cashReceivedRupees,
        upiReceivedRupees = s.upiReceivedRupees,
        isGstBill = s.isGstBill,
        applyRoundOff = s.applyRoundOff,
        notes = s.notes,
        itemsJson = DraftSerializer.serializeItems(s.items),
        updatedAt = System.currentTimeMillis(),
      )
      billingRepository.saveDraft(draft)
    }
  }

  fun updateCustomerName(name: String) {
    _uiState.update { it.copy(customerName = name) }
    triggerAutosave()
  }

  fun updateCustomerPhone(phone: String) {
    _uiState.update { it.copy(customerPhone = phone) }
    triggerAutosave()
  }

  fun selectCustomer(customer: CustomerEntity) {
    _uiState.update {
      it.copy(
        customerId = customer.id,
        customerName = customer.name,
        customerPhone = customer.phone ?: "",
      )
    }
    triggerAutosave()
  }

  fun clearCustomer() {
    _uiState.update {
      it.copy(
        customerId = null,
        customerName = "",
        customerPhone = "",
        customerGstin = "",
      )
    }
    triggerAutosave()
  }

  fun updateCustomerGstin(gstin: String) {
    _uiState.update { it.copy(customerGstin = gstin) }
    triggerAutosave()
  }

  fun setPaymentMode(mode: String) {
    _uiState.update { it.copy(paymentMode = mode) }
    triggerAutosave()
  }

  fun setBillDiscountRupees(rupees: String) {
    _uiState.update { it.copy(billDiscountRupees = rupees) }
    triggerAutosave()
  }

  fun setCashReceivedRupees(rupees: String) {
    _uiState.update { it.copy(cashReceivedRupees = rupees) }
    triggerAutosave()
  }

  fun setUpiReceivedRupees(rupees: String) {
    _uiState.update { it.copy(upiReceivedRupees = rupees) }
    triggerAutosave()
  }

  fun toggleGstBill(isGst: Boolean) {
    _uiState.update { state ->
      val defaultRate = if (isGst) state.shopSettings?.defaultGstRate ?: 18.0 else 0.0
      val updatedItems = state.items.map { item ->
        item.copy(taxRatePercent = if (isGst) defaultRate else 0.0)
      }
      state.copy(isGstBill = isGst, items = updatedItems)
    }
    triggerAutosave()
  }

  fun toggleRoundOff(apply: Boolean) {
    _uiState.update { it.copy(applyRoundOff = apply) }
    triggerAutosave()
  }

  fun setNotes(notes: String) {
    _uiState.update { it.copy(notes = notes) }
    triggerAutosave()
  }

  fun addProductToCart(product: ProductEntity, quantity: Double = 1.0) {
    _uiState.update { state ->
      val existingIndex = state.items.indexOfFirst { it.productId == product.id }
      val taxRate = if (state.isGstBill) state.shopSettings?.defaultGstRate ?: 18.0 else 0.0

      if (existingIndex >= 0) {
        val existing = state.items[existingIndex]
        val updatedList = state.items.toMutableList()
        updatedList[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        state.copy(items = updatedList)
      } else {
        val newItem = DraftCartItem(
          productId = product.id,
          productName = product.name,
          brand = product.brand,
          specification = product.specification,
          unit = if (product.unit == "CUSTOM") product.customUnit ?: "Custom" else product.unit,
          quantity = quantity,
          unitPricePaise = product.sellingPricePaise,
          taxRatePercent = taxRate,
        )
        state.copy(items = state.items + newItem)
      }
    }
    triggerAutosave()
  }

  fun addCustomItem(name: String, unitPricePaise: Long, quantity: Double = 1.0, unit: String = "PCS") {
    val taxRate = if (_uiState.value.isGstBill) _uiState.value.shopSettings?.defaultGstRate ?: 18.0 else 0.0
    val newItem = DraftCartItem(
      productName = name.ifBlank { "General Item" },
      unit = unit,
      quantity = quantity,
      unitPricePaise = unitPricePaise,
      taxRatePercent = taxRate,
    )
    _uiState.update { it.copy(items = it.items + newItem) }
    triggerAutosave()
  }

  fun updateItemQuantity(itemId: String, quantity: Double) {
    if (quantity <= 0.0) {
      removeItem(itemId)
      return
    }
    _uiState.update { state ->
      state.copy(items = state.items.map { if (it.id == itemId) it.copy(quantity = quantity) else it })
    }
    triggerAutosave()
  }

  fun updateItemUnitPrice(itemId: String, unitPricePaise: Long) {
    _uiState.update { state ->
      state.copy(items = state.items.map { if (it.id == itemId) it.copy(unitPricePaise = unitPricePaise) else it })
    }
    triggerAutosave()
  }

  fun updateItemDiscount(itemId: String, discountPaise: Long) {
    _uiState.update { state ->
      state.copy(items = state.items.map { if (it.id == itemId) it.copy(discountPaise = discountPaise) else it })
    }
    triggerAutosave()
  }

  fun updateItemTaxRate(itemId: String, taxRatePercent: Double) {
    _uiState.update { state ->
      state.copy(items = state.items.map { if (it.id == itemId) it.copy(taxRatePercent = taxRatePercent) else it })
    }
    triggerAutosave()
  }

  fun removeItem(itemId: String) {
    _uiState.update { state ->
      state.copy(items = state.items.filterNot { it.id == itemId })
    }
    triggerAutosave()
  }

  fun clearCart() {
    viewModelScope.launch {
      billingRepository.clearDraft()
      _uiState.update {
        BillingUiState(
          shopSettings = it.shopSettings,
          availableProducts = it.availableProducts,
          availableCustomers = it.availableCustomers,
          isGstBill = !it.shopSettings?.gstin.isNullOrBlank(),
        )
      }
    }
  }

  // --- Finalize Bill ---
  fun saveBill(onSuccess: (BillEntity) -> Unit, onError: (String) -> Unit = {}) {
    val state = _uiState.value
    if (state.items.isEmpty()) {
      onError("Please add at least one item to create a bill.")
      return
    }
    if (state.netAmountPaise <= 0L) {
      onError("Bill amount must be greater than zero.")
      return
    }
    if (state.paymentMode == "KHATA" && state.customerId == null) {
      onError("Customer selection is required for Khata (Credit) bills.")
      return
    }
    if (state.balanceDuePaise > 0L && state.customerId == null) {
      onError("Customer selection is required when there is a remaining balance due.")
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true) }

      try {
        val billNumber = billingRepository.generateNextBillNumber()
        val billId = UUID.randomUUID().toString()

        val billEntity = BillEntity(
          id = billId,
          billNumber = billNumber,
          customerId = state.customerId,
          customerName = state.customerName.ifBlank { null },
          customerPhone = state.customerPhone.ifBlank { null },
          customerGstin = state.customerGstin.ifBlank { null },
          subtotalPaise = state.subtotalPaise,
          discountPaise = state.totalDiscountPaise,
          taxPaise = state.taxPaise,
          roundOffPaise = state.roundOffPaise,
          netAmountPaise = state.netAmountPaise,
          paymentMode = state.paymentMode,
          paymentStatus = state.paymentStatus,
          cashReceivedPaise = if (state.paymentMode == "CASH") state.paidAmountPaise else if (state.paymentMode == "SPLIT") state.cashTenderedPaise else 0L,
          upiReceivedPaise = if (state.paymentMode == "UPI") state.paidAmountPaise else if (state.paymentMode == "SPLIT") state.upiTenderedPaise else 0L,
          khataDebitedPaise = state.balanceDuePaise,
          paidAmountPaise = state.paidAmountPaise,
          balanceDuePaise = state.balanceDuePaise,
          isGstBill = state.isGstBill,
          sellerGstin = if (state.isGstBill) state.shopSettings?.gstin else null,
          notes = state.notes.ifBlank { null },
          createdAt = System.currentTimeMillis(),
        )

        val billItems = state.items.map { item ->
          val calc = item.calculation
          BillItemEntity(
            billId = billId,
            productId = item.productId,
            productName = item.productName,
            brand = item.brand,
            specification = item.specification,
            unit = item.unit,
            quantity = item.quantity,
            unitPricePaise = item.unitPricePaise,
            discountPaise = calc.discountPaise,
            taxRatePercent = calc.taxRatePercent,
            taxPaise = calc.taxPaise,
            totalPricePaise = calc.lineTotalPaise,
          )
        }

        val savedBill = billingRepository.createBill(billEntity, billItems)

        // Reset state after successful save
        _uiState.update {
          BillingUiState(
            shopSettings = it.shopSettings,
            availableProducts = it.availableProducts,
            availableCustomers = it.availableCustomers,
            isGstBill = !it.shopSettings?.gstin.isNullOrBlank(),
            lastSavedBill = savedBill,
          )
        }

        onSuccess(savedBill)
      } catch (e: Exception) {
        _uiState.update { it.copy(isSaving = false) }
        onError(e.message ?: "Failed to save bill. Please try again.")
      }
    }
  }

  class Factory(
    private val billingRepository: BillingRepository,
    private val inventoryRepository: InventoryRepository? = null,
    private val khataRepository: KhataRepository? = null,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return BillingViewModel(billingRepository, inventoryRepository, khataRepository) as T
    }
  }
}
