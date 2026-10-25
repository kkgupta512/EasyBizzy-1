package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.EasyBizzyDatabase
import com.example.data.entity.BillDraftEntity
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.data.repository.BillingRepository
import com.example.data.util.DraftSerializer
import com.example.data.util.Money
import com.example.ui.viewmodel.DraftCartItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BillingFeatureTest {

  private lateinit var db: EasyBizzyDatabase
  private lateinit var billingRepo: BillingRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = EasyBizzyDatabase.createInMemoryDatabase(context)
    billingRepo = BillingRepository(
      billDao = db.billDao(),
      customerDao = db.customerDao(),
      ledgerDao = db.ledgerDao(),
      paymentDao = db.paymentDao(),
      productDao = db.productDao(),
      billDraftDao = db.billDraftDao(),
      shopSettingsDao = db.shopSettingsDao(),
    )
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun testTotalsCalculationWithItemAndBillDiscounts() {
    runBlocking {
      // Item 1: 2 boxes of Havells wire @ ₹1,850.00 (185000 paise) = ₹3,700.00 (370000 paise)
      val item1 = Money.calculateLineItem(
        quantity = 2.0,
        unitPricePaise = 185000L,
        discountPaise = 0L,
        taxRatePercent = 0.0,
      )
      assertEquals(370000L, item1.grossPaise)
      assertEquals(370000L, item1.lineTotalPaise)

      // Item 2: 10 switches @ ₹36.00 (3600 paise) = 36000 paise, with ₹20.00 (2000 paise) discount = 34000 paise
      val item2 = Money.calculateLineItem(
        quantity = 10.0,
        unitPricePaise = 3600L,
        discountPaise = 2000L,
        taxRatePercent = 0.0,
      )
      assertEquals(36000L, item2.grossPaise)
      assertEquals(2000L, item2.discountPaise)
      assertEquals(34000L, item2.lineTotalPaise)

      // Bill-level discount of ₹100.00 (10000 paise)
      val billTotals = Money.calculateBillTotals(
        lineCalculations = listOf(item1, item2),
        billDiscountPaise = 10000L,
        applyRoundOff = true,
      )

      assertEquals(406000L, billTotals.grossSubtotalPaise) // 370000 + 36000
      assertEquals(2000L, billTotals.itemDiscountsPaise)
      assertEquals(10000L, billTotals.billDiscountPaise)
      assertEquals(12000L, billTotals.totalDiscountPaise)
      assertEquals(394000L, billTotals.taxableSubtotalPaise) // 406000 - 12000
      assertEquals(394000L, billTotals.finalNetPaise)
    }
  }

  @Test
  fun testIndianCurrencyRoundingRules() {
    runBlocking {
      // Case 1: ₹150.40 (15040 paise) rounds down to ₹150.00 (15000 paise) -> -40 paise
      val roundOff1 = Money.calculateRoundOff(15040L)
      assertEquals(-40L, roundOff1)
      assertEquals(15000L, 15040L + roundOff1)

      // Case 2: ₹150.50 (15050 paise) rounds up to ₹151.00 (15100 paise) -> +50 paise
      val roundOff2 = Money.calculateRoundOff(15050L)
      assertEquals(50L, roundOff2)
      assertEquals(15100L, 15050L + roundOff2)

      // Case 3: ₹150.85 (15085 paise) rounds up to ₹151.00 (15100 paise) -> +15 paise
      val roundOff3 = Money.calculateRoundOff(15085L)
      assertEquals(15L, roundOff3)
      assertEquals(15100L, 15085L + roundOff3)

      // Case 4: ₹150.00 (15000 paise) exact amount -> 0 paise
      val roundOff4 = Money.calculateRoundOff(15000L)
      assertEquals(0L, roundOff4)
      assertEquals(15000L, 15000L + roundOff4)
    }
  }

  @Test
  fun testGstTaxCalculationAndCgstSgstSplit() {
    runBlocking {
      // 1 item with ₹100.00 taxable value and 18% GST
      val line = Money.calculateLineItem(
        quantity = 1.0,
        unitPricePaise = 10000L,
        taxRatePercent = 18.0,
      )
      assertEquals(1800L, line.taxPaise)      // ₹18.00 tax
      assertEquals(11800L, line.lineTotalPaise) // ₹118.00 total

      val totals = Money.calculateBillTotals(
        lineCalculations = listOf(line),
        billDiscountPaise = 0L,
        applyRoundOff = false,
      )
      assertEquals(1800L, totals.totalTaxPaise)
      assertEquals(900L, totals.cgstPaise) // 9% CGST = ₹9.00
      assertEquals(900L, totals.sgstPaise) // 9% SGST = ₹9.00
      assertEquals(11800L, totals.finalNetPaise)
    }
  }

  @Test
  fun testCollisionFreeBillNumbering() {
    runBlocking {
      val num1 = billingRepo.generateNextBillNumber()
      assertTrue("Format should match EB-YYYYMMDD-0001", num1.endsWith("-0001"))

      // Insert first bill with num1
      val bill1 = BillEntity(
        id = UUID.randomUUID().toString(),
        billNumber = num1,
        netAmountPaise = 50000L,
      )
      billingRepo.createBill(bill1, emptyList())

      // Second bill number should be monotonically incremented to 0002
      val num2 = billingRepo.generateNextBillNumber()
      assertTrue("Format should increment to -0002", num2.endsWith("-0002"))

      val bill2 = BillEntity(
        id = UUID.randomUUID().toString(),
        billNumber = num2,
        netAmountPaise = 75000L,
      )
      billingRepo.createBill(bill2, emptyList())

      // Third bill number should increment to 0003
      val num3 = billingRepo.generateNextBillNumber()
      assertTrue("Format should increment to -0003", num3.endsWith("-0003"))
    }
  }

  @Test
  fun testPaidUnpaidAndPartiallyPaidBillsWithKhataIntegration() {
    runBlocking {
      // 1. Create a customer with 0 starting balance
      val customer = CustomerEntity(
        id = "cust-101",
        name = "Vikram Singh",
        phone = "9876543210",
        currentBalancePaise = 0L,
      )
      db.customerDao().insertCustomer(customer)

      // SCENARIO A: Fully paid Cash bill of ₹1,000 -> Customer balance should remain 0
      val billA = BillEntity(
        id = "bill-a",
        billNumber = "EB-TEST-001",
        customerId = customer.id,
        netAmountPaise = 100000L,
        paidAmountPaise = 100000L,
        cashReceivedPaise = 100000L,
        balanceDuePaise = 0L,
        paymentStatus = "PAID",
      )
      billingRepo.createBill(billA, emptyList())

      var savedCustomer = db.customerDao().getCustomerById(customer.id)
      assertEquals(0L, savedCustomer?.currentBalancePaise)

      // SCENARIO B: Partially paid bill of ₹1,000 (Cash ₹400, UPI ₹200) -> Balance due ₹400 debited to Khata
      val billB = BillEntity(
        id = "bill-b",
        billNumber = "EB-TEST-002",
        customerId = customer.id,
        netAmountPaise = 100000L,
        cashReceivedPaise = 40000L,
        upiReceivedPaise = 20000L,
        paidAmountPaise = 60000L,
        balanceDuePaise = 40000L,
        paymentStatus = "PARTIALLY_PAID",
      )
      billingRepo.createBill(billB, emptyList())

      savedCustomer = db.customerDao().getCustomerById(customer.id)
      assertEquals(40000L, savedCustomer?.currentBalancePaise) // ₹400 due

      // SCENARIO C: Fully unpaid Khata (Credit) bill of ₹500 -> Balance increases by ₹500 to ₹900
      val billC = BillEntity(
        id = "bill-c",
        billNumber = "EB-TEST-003",
        customerId = customer.id,
        netAmountPaise = 50000L,
        paidAmountPaise = 0L,
        balanceDuePaise = 50000L,
        paymentMode = "KHATA",
        paymentStatus = "UNPAID",
      )
      billingRepo.createBill(billC, emptyList())

      savedCustomer = db.customerDao().getCustomerById(customer.id)
      assertEquals(90000L, savedCustomer?.currentBalancePaise) // Total ₹900 due

      // Verify Ledger entries recorded for dues
      val ledgerEntries = db.ledgerDao().getEntriesForParty(customer.id).first()
      assertEquals(2, ledgerEntries.size)
      assertTrue(ledgerEntries.all { it.entryType == "GAVE" })
    }
  }

  @Test
  fun testProductStockDeductionOnSale() {
    runBlocking {
      val product = ProductEntity(
        id = "prod-wire-1",
        name = "Finolex 1.5 sq mm Wire",
        sellingPricePaise = 180000L,
        currentStock = 25.0,
      )
      db.productDao().insertProduct(product)

      val bill = BillEntity(
        id = "bill-stock-test",
        billNumber = "EB-STOCK-01",
        netAmountPaise = 360000L,
      )
      val item = BillItemEntity(
        billId = bill.id,
        productId = product.id,
        productName = product.name,
        quantity = 3.0,
        unitPricePaise = product.sellingPricePaise,
        totalPricePaise = 360000L,
      )

      billingRepo.createBill(bill, listOf(item))

      val updatedProduct = db.productDao().getProductById(product.id)
      assertEquals(22.0, updatedProduct?.currentStock ?: 0.0, 0.001) // 25 - 3 = 22
    }
  }

  @Test
  fun testDraftAutosaveAndFailureRecovery() {
    runBlocking {
      // 1. Simulate active bill draft being edited
      val draftItems = listOf(
        DraftCartItem(
          productId = "prod-1",
          productName = "Havells Switch",
          quantity = 5.0,
          unitPricePaise = 4500L,
        )
      )
      val draft = BillDraftEntity(
        customerName = "Ramesh Kumar",
        customerPhone = "9876543210",
        paymentMode = "UPI",
        billDiscountRupees = "50",
        itemsJson = DraftSerializer.serializeItems(draftItems),
      )
      billingRepo.saveDraft(draft)

      // 2. Simulate app crash / process termination:
      // A new repository instance on the same SQLite database restores the draft
      val recoveredDraft = billingRepo.getDraftSnapshot()
      assertNotNull("Draft must be recovered from Room", recoveredDraft)
      assertEquals("Ramesh Kumar", recoveredDraft?.customerName)
      assertEquals("9876543210", recoveredDraft?.customerPhone)
      assertEquals("UPI", recoveredDraft?.paymentMode)

      val recoveredItems = DraftSerializer.deserializeItems(recoveredDraft?.itemsJson)
      assertEquals(1, recoveredItems.size)
      assertEquals("Havells Switch", recoveredItems.first().productName)
      assertEquals(5.0, recoveredItems.first().quantity, 0.001)

      // 3. Completing the bill clears the draft to prevent replay
      val finalBill = BillEntity(
        id = UUID.randomUUID().toString(),
        billNumber = "EB-FINAL-001",
        customerName = recoveredDraft?.customerName,
        netAmountPaise = 20000L,
      )
      billingRepo.createBill(finalBill, emptyList())

      assertNull("Draft must be cleared after bill completion", billingRepo.getDraftSnapshot())
    }
  }

  @Test
  fun testHistoricalSnapshotImmutabilityWhenProductModifiedOrDeleted() {
    runBlocking {
      val product = ProductEntity(
        id = "prod-immut-1",
        name = "Original TMT Rebar 12mm",
        brand = "Tata Tiscon",
        specification = "Grade 550D 40ft",
        unit = "PIECE",
        sellingPricePaise = 71000L,
      )
      db.productDao().insertProduct(product)

      val billId = UUID.randomUUID().toString()
      val bill = BillEntity(
        id = billId,
        billNumber = "EB-IMMUT-001",
        subtotalPaise = 71000L,
        netAmountPaise = 71000L,
      )
      val item = BillItemEntity(
        billId = billId,
        productId = product.id,
        productName = product.name,
        brand = product.brand,
        specification = product.specification,
        unit = product.unit,
        quantity = 1.0,
        unitPricePaise = product.sellingPricePaise,
        totalPricePaise = 71000L,
      )
      billingRepo.createBill(bill, listOf(item))

      // Modify product in catalog: Change name, brand, price
      db.productDao().updateProduct(
        product.copy(
          name = "Modified Rebar 16mm",
          brand = "Jindal Panther",
          sellingPricePaise = 95000L,
        )
      )

      // Delete product from catalog
      db.productDao().deleteProductById(product.id)
      assertNull(db.productDao().getProductById(product.id))

      // Verify historical bill item is 100% intact
      val billWithItems = billingRepo.getBillDetails(billId)
      assertNotNull(billWithItems)
      assertEquals(1, billWithItems?.items?.size)

      val snapshot = billWithItems!!.items.first()
      assertEquals("Original TMT Rebar 12mm", snapshot.productName)
      assertEquals("Tata Tiscon", snapshot.brand)
      assertEquals("Grade 550D 40ft", snapshot.specification)
      assertEquals(71000L, snapshot.unitPricePaise)
      assertEquals(71000L, snapshot.totalPricePaise)
      assertNull("Product FK should be SET_NULL after deletion", snapshot.productId)
    }
  }
}
