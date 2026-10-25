package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.EasyBizzyDatabase
import com.example.data.entity.ProductEntity
import com.example.data.repository.DuplicateProductException
import com.example.data.repository.InventoryRepository
import com.example.data.util.CsvProductImporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductManagementTest {

  private lateinit var db: EasyBizzyDatabase
  private lateinit var inventoryRepo: InventoryRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = EasyBizzyDatabase.createInMemoryDatabase(context)
    inventoryRepo = InventoryRepository(db.productDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun testAddAndRetrieveProduct() {
    runBlocking {
      val product = ProductEntity(
        id = UUID.randomUUID().toString(),
        name = "Havells LifeLine Plus Wire",
        brand = "Havells",
        sku = "HAV-LL-15",
        specification = "1.5 sq mm 90m",
        category = "Electrical",
        unit = "BOX",
        purchasePricePaise = 145000L, // ₹1,450.00
        sellingPricePaise = 185000L,  // ₹1,850.00
        currentStock = 25.0,
        minStockAlert = 5.0,
      )

      inventoryRepo.addProduct(product)

      val active = inventoryRepo.activeProducts.first()
      assertEquals(1, active.size)
      val saved = active.first()
      assertEquals("Havells LifeLine Plus Wire", saved.name)
      assertEquals("Havells", saved.brand)
      assertEquals("HAV-LL-15", saved.sku)
      assertEquals("1.5 sq mm 90m", saved.specification)
      assertEquals("Electrical", saved.category)
      assertEquals("BOX", saved.unit)
      assertEquals(145000L, saved.purchasePricePaise)
      assertEquals(185000L, saved.sellingPricePaise)
      assertEquals(25.0, saved.currentStock ?: 0.0, 0.001)
    }
  }

  @Test
  fun testEditProduct() {
    runBlocking {
      val product = ProductEntity(
        id = "prod-edit-1",
        name = "UltraTech Cement",
        brand = "UltraTech",
        unit = "BAG",
        sellingPricePaise = 42000L,
        currentStock = 50.0,
      )
      inventoryRepo.addProduct(product)

      // Edit price and stock
      val updated = product.copy(
        sellingPricePaise = 43500L,
        currentStock = 75.0,
      )
      inventoryRepo.updateProduct(updated)

      val active = inventoryRepo.activeProducts.first()
      assertEquals(1, active.size)
      assertEquals(43500L, active.first().sellingPricePaise)
      assertEquals(75.0, active.first().currentStock ?: 0.0, 0.001)
    }
  }

  @Test
  fun testHardwareAndElectricalDifferentSpecificationsAreNotDuplicates() {
    runBlocking {
      // 1.5 sq mm copper wire vs 2.5 sq mm copper wire must NOT be treated as duplicates!
      val wire15 = ProductEntity(
        id = "wire-15",
        name = "House Wire FR",
        brand = "Finolex",
        sku = "FIN-15-FR",
        specification = "1.5 sq mm",
        category = "Electrical",
        unit = "BOX",
        sellingPricePaise = 180000L,
      )

      val wire25 = ProductEntity(
        id = "wire-25",
        name = "House Wire FR",
        brand = "Finolex",
        sku = "FIN-25-FR",
        specification = "2.5 sq mm",
        category = "Electrical",
        unit = "BOX",
        sellingPricePaise = 280000L,
      )

      inventoryRepo.addProduct(wire15)
      inventoryRepo.addProduct(wire25) // Should succeed without exception!

      val all = inventoryRepo.activeProducts.first()
      assertEquals(2, all.size)
    }
  }

  @Test
  fun testDifferentUnitsAreNotDuplicates() {
    runBlocking {
      // Selling whole box vs per single piece
      val perPiece = ProductEntity(
        id = "switch-pc",
        name = "Anchor Roma 6A Switch",
        brand = "Anchor",
        sku = "ANC-SW-PC",
        unit = "PIECE",
        sellingPricePaise = 4000L,
      )

      val perBox = ProductEntity(
        id = "switch-box",
        name = "Anchor Roma 6A Switch",
        brand = "Anchor",
        sku = "ANC-SW-BX",
        unit = "BOX",
        specification = "10 Pcs Pack",
        sellingPricePaise = 36000L,
      )

      inventoryRepo.addProduct(perPiece)
      inventoryRepo.addProduct(perBox)

      val all = inventoryRepo.activeProducts.first()
      assertEquals(2, all.size)
    }
  }

  @Test
  fun testDuplicateSkuThrowsException() {
    runBlocking {
      val prod1 = ProductEntity(
        id = "sku-1",
        name = "Product A",
        sku = "ELEC-UNIQUE-001",
        sellingPricePaise = 10000L,
      )
      val prod2 = ProductEntity(
        id = "sku-2",
        name = "Product B",
        sku = "ELEC-UNIQUE-001",
        sellingPricePaise = 12000L,
      )

      inventoryRepo.addProduct(prod1)

      assertThrows(DuplicateProductException::class.java) {
        runBlocking {
          inventoryRepo.addProduct(prod2)
        }
      }
    }
  }

  @Test
  fun testDuplicateIdentityKeyThrowsException() {
    runBlocking {
      val prod1 = ProductEntity(
        id = "dup-1",
        name = "Tata Tiscon TMT Bar",
        brand = "Tata Tiscon",
        specification = "12mm 40ft",
        unit = "PIECE",
        sellingPricePaise = 71000L,
      )
      val prod2 = ProductEntity(
        id = "dup-2",
        name = "Tata Tiscon TMT Bar", // Same name
        brand = "Tata Tiscon",        // Same brand
        specification = "12mm 40ft", // Same specification
        unit = "PIECE",              // Same unit
        sellingPricePaise = 72000L,
      )

      inventoryRepo.addProduct(prod1)

      assertThrows(DuplicateProductException::class.java) {
        runBlocking {
          inventoryRepo.addProduct(prod2)
        }
      }
    }
  }

  @Test
  fun testCustomUnitValidation() {
    runBlocking {
      // Valid custom unit
      val customValid = ProductEntity(
        name = "Asian Paints Apex",
        brand = "Asian Paints",
        unit = "CUSTOM",
        customUnit = "Litre",
        sellingPricePaise = 450000L,
      )
      inventoryRepo.addProduct(customValid)

      // Invalid custom unit without customUnit text
      val customInvalid = ProductEntity(
        name = "Sand Aggregate",
        unit = "CUSTOM",
        customUnit = "",
        sellingPricePaise = 120000L,
      )

      assertThrows(IllegalArgumentException::class.java) {
        runBlocking {
          inventoryRepo.addProduct(customInvalid)
        }
      }
    }
  }

  @Test
  fun testValidationRules() {
    runBlocking {
      // Blank name rejected
      assertThrows(IllegalArgumentException::class.java) {
        runBlocking {
          inventoryRepo.addProduct(ProductEntity(name = "   ", sellingPricePaise = 1000L))
        }
      }

      // Negative selling price rejected
      assertThrows(IllegalArgumentException::class.java) {
        runBlocking {
          inventoryRepo.addProduct(ProductEntity(name = "Pipe", sellingPricePaise = -500L))
        }
      }

      // Negative purchase price rejected
      assertThrows(IllegalArgumentException::class.java) {
        runBlocking {
          inventoryRepo.addProduct(ProductEntity(name = "Pipe", sellingPricePaise = 1000L, purchasePricePaise = -200L))
        }
      }
    }
  }

  @Test
  fun testArchiveAndRestoreProduct() {
    runBlocking {
      val product = ProductEntity(
        id = "archive-test-1",
        name = "Old Discontinued Fitting",
        sellingPricePaise = 5000L,
      )
      inventoryRepo.addProduct(product)

      assertEquals(1, inventoryRepo.activeProducts.first().size)
      assertEquals(0, inventoryRepo.archivedProducts.first().size)

      // Archive it
      inventoryRepo.setArchived(product.id, true)

      assertEquals(0, inventoryRepo.activeProducts.first().size)
      val archived = inventoryRepo.archivedProducts.first()
      assertEquals(1, archived.size)
      assertEquals(product.id, archived.first().id)

      // Restore it
      inventoryRepo.setArchived(product.id, false)

      assertEquals(1, inventoryRepo.activeProducts.first().size)
      assertEquals(0, inventoryRepo.archivedProducts.first().size)
    }
  }

  @Test
  fun testCsvBulkImportAndDuplicateSkipping() {
    runBlocking {
      val sampleCsv = CsvProductImporter.getSampleCsvTemplate()
      val importResult = inventoryRepo.bulkImportCsv(sampleCsv)

      assertEquals(7, importResult.validProducts.size)
      assertEquals(0, importResult.skippedDuplicatesCount)
      assertEquals(0, importResult.errorCount)

      val active = inventoryRepo.activeProducts.first()
      assertEquals(7, active.size)

      // Re-importing the same CSV should detect 7 duplicates and insert 0 new items
      val secondImport = inventoryRepo.bulkImportCsv(sampleCsv)
      assertEquals(0, secondImport.validProducts.size)
      assertEquals(7, secondImport.skippedDuplicatesCount)

      // Database still contains only 7 products
      assertEquals(7, inventoryRepo.activeProducts.first().size)
    }
  }

  @Test
  fun testCsvParsingWithInvalidRows() {
    val malformedCsv = """
      name,selling_price,unit
      Good Product,250.00,PIECE
      ,100.00,PIECE
      Invalid Price Product,not_a_number,BOX
      Negative Price Product,-50.00,PIECE
    """.trimIndent()

    val result = CsvProductImporter.parseAndValidateCsv(malformedCsv, emptyList())
    assertEquals(1, result.validProducts.size)
    assertEquals("Good Product", result.validProducts.first().name)
    assertEquals(3, result.errorCount)
  }
}
