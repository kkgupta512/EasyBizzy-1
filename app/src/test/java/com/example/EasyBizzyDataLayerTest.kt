package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.EasyBizzyDatabase
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProductEntity
import com.example.data.repository.BillingRepository
import com.example.data.repository.KhataRepository
import com.example.data.util.Money
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
class EasyBizzyDataLayerTest {

  private lateinit var db: EasyBizzyDatabase
  private lateinit var billingRepo: BillingRepository
  private lateinit var khataRepo: KhataRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = EasyBizzyDatabase.createInMemoryDatabase(context)
    billingRepo = BillingRepository(db.billDao(), db.customerDao(), db.ledgerDao(), db.paymentDao())
    khataRepo = KhataRepository(db.customerDao(), db.supplierDao(), db.ledgerDao(), db.paymentDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun testMoneyCalculationsAndFormatting() {
    // 1 Rupee = 100 Paise
    assertEquals(12550L, Money.toPaise(125.50))
    assertEquals(125.50, Money.toRupees(12550L), 0.001)

    // Quantity * Unit Price + Tax: 2.5 kg * ₹40.00 (4000 paise) + 5% tax = ₹105.00 (10500 paise)
    val lineTotal = Money.calculateLineTotal(quantity = 2.5, unitPricePaise = 4000L, taxRatePercent = 5.0)
    assertEquals(10500L, lineTotal)

    val formatted = Money.format(12500000L, showDecimals = false) // ₹ 1,25,000
    assertTrue("Should format Indian rupees", formatted.contains("₹") && formatted.contains("125"))
  }

  @Test
  fun testHistoricalBillItemPreservationWhenProductDeleted() = runBlocking {
    val productDao = db.productDao()
    val billDao = db.billDao()

    // 1. Create a Product
    val productId = UUID.randomUUID().toString()
    val product = ProductEntity(
      id = productId,
      name = "India Gate Basmati Rice 5kg",
      unit = "PACKET",
      purchasePricePaise = 38000L,
      sellingPricePaise = 45000L,
      currentStock = 20.0,
    )
    productDao.insertProduct(product)

    // 2. Create a Bill containing this Product
    val billId = UUID.randomUUID().toString()
    val bill = BillEntity(
      id = billId,
      billNumber = "EB-TEST-001",
      customerName = "Anita Verma",
      subtotalPaise = 45000L,
      netAmountPaise = 45000L,
      paymentMode = "CASH",
    )

    val item = BillItemEntity(
      billId = billId,
      productId = productId,
      productName = product.name, // Frozen historical name
      unit = product.unit,        // Frozen historical unit
      quantity = 1.0,
      unitPricePaise = product.sellingPricePaise,
      taxRatePercent = 0.0,
      totalPricePaise = 45000L,
    )

    billDao.insertBillWithItems(bill, listOf(item))

    // 3. Delete the Product from inventory
    productDao.deleteProductById(productId)
    assertNull("Product should be deleted from catalog", productDao.getProductById(productId))

    // 4. Verify historical bill item is PRESERVED exactly as billed
    val billWithItems = billDao.getBillWithItemsById(billId)
    assertNotNull("Bill must still exist", billWithItems)
    assertEquals(1, billWithItems!!.items.size)

    val preservedItem = billWithItems.items[0]
    assertEquals("India Gate Basmati Rice 5kg", preservedItem.productName)
    assertEquals("PACKET", preservedItem.unit)
    assertEquals(1.0, preservedItem.quantity, 0.001)
    assertEquals(45000L, preservedItem.totalPricePaise)
    assertNull("Product FK should be SET_NULL after catalog deletion", preservedItem.productId)
  }

  @Test
  fun testKhataLedgerCalculationsAndLenaDena() = runBlocking {
    val customerId = UUID.randomUUID().toString()
    val customer = CustomerEntity(
      id = customerId,
      name = "Suresh Gupta",
      phone = "9876543210",
      currentBalancePaise = 0L,
    )
    khataRepo.addCustomer(customer)

    // Record Udhar (Gave ₹500 = 50000 paise)
    khataRepo.recordCustomerTransaction(customerId, "GAVE", 50000L, "Milk and Bread on credit")

    var totalLena = khataRepo.totalReceivablesPaise.first()
    assertEquals("Total Lena must be 50000 paise", 50000L, totalLena)

    // Customer makes partial repayment (Got ₹200 = 20000 paise)
    khataRepo.recordCustomerTransaction(customerId, "GOT", 20000L, "Cash payment received")

    totalLena = khataRepo.totalReceivablesPaise.first()
    assertEquals("Remaining Lena must be 30000 paise", 30000L, totalLena)

    val entries = khataRepo.getEntriesForParty(customerId).first()
    assertEquals(2, entries.size)
  }

  @Test
  fun testDataPersistenceAcrossDatabaseInstances() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val persistentDb1 = EasyBizzyDatabase.getInstance(context)

    val testProduct = ProductEntity(
      id = "persistent_p1",
      name = "Tata Salt 1kg",
      sellingPricePaise = 2800L,
      currentStock = 50.0,
    )
    persistentDb1.productDao().insertProduct(testProduct)

    // Simulate app restart by getting the same persistent instance
    val persistentDb2 = EasyBizzyDatabase.getInstance(context)
    val fetched = persistentDb2.productDao().getProductById("persistent_p1")

    assertNotNull("Data must survive and be retrievable", fetched)
    assertEquals("Tata Salt 1kg", fetched!!.name)
    assertEquals(2800L, fetched.sellingPricePaise)
  }

  @Test
  fun testMigration1To2Definition() {
    assertNotNull("MIGRATION_1_2 must be defined", EasyBizzyDatabase.MIGRATION_1_2)
    assertEquals(1, EasyBizzyDatabase.MIGRATION_1_2.startVersion)
    assertEquals(2, EasyBizzyDatabase.MIGRATION_1_2.endVersion)
  }
}
