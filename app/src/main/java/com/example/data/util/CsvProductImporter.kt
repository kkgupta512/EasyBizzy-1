package com.example.data.util

import com.example.data.entity.ProductEntity
import java.util.UUID

data class CsvImportResult(
  val validProducts: List<ProductEntity>,
  val skippedDuplicatesCount: Int,
  val errorCount: Int,
  val validationErrors: List<String>,
)

/**
 * Validated CSV parser and importer for retail, electrical, hardware, and building material products.
 */
object CsvProductImporter {

  private val STANDARD_UNITS = setOf("PIECE", "BOX", "PACKET", "BAG", "METRE", "KILOGRAM")

  /**
   * Generates a sample CSV template for hardware, electrical and building materials.
   */
  fun getSampleCsvTemplate(): String {
    return """
name,brand,sku,category,unit,specification,purchase_price,selling_price,stock
Havells LifeLine Plus Wire,Havells,HAV-LL-15,Electrical,BOX,1.5 sq mm 90m,1450.00,1850.00,25
UltraTech Super Cement,UltraTech,UT-CEM-53,Building Material,BAG,Grade 53 50kg,380.00,430.00,100
Tata Tiscon 550D TMT Rebar,Tata Tiscon,TATA-TMT-12,Building Material,PIECE,12mm dia x 40ft,620.00,710.00,80
Finolex PVC Conduit Pipe,Finolex,FIN-PVC-25,Electrical,PIECE,25mm Heavy 3m,85.00,115.00,150
Anchor Roma 6A Switch,Anchor,ANC-ROM-6A,Electrical,BOX,10 pcs per box,280.00,360.00,40
Asian Paints Apex Weatherproof,Asian Paints,AP-APEX-20L,Paint,CUSTOM,20 Litre Drum,4200.00,4900.00,12
Astral CPVC Pipe SDR 11,Astral,AST-CPVC-1IN,Plumbing,PIECE,1 inch x 3m,320.00,410.00,60
    """.trimIndent()
  }

  /**
   * Parses CSV string, validates each row, and detects duplicates against existing products.
   */
  fun parseAndValidateCsv(
    csvContent: String,
    existingProducts: List<ProductEntity>,
  ): CsvImportResult {
    val lines = csvContent.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) {
      return CsvImportResult(emptyList(), 0, 1, listOf("CSV content is empty"))
    }

    // Parse Header
    val headerLine = lines.first()
    val delimiter = if (headerLine.contains(";")) ";" else ","
    val headers = headerLine.split(delimiter).map { it.trim().lowercase() }

    val nameIndex = headers.indexOfFirst { it == "name" || it == "product_name" }
    val brandIndex = headers.indexOfFirst { it == "brand" }
    val skuIndex = headers.indexOfFirst { it == "sku" || it == "code" || it == "item_code" }
    val categoryIndex = headers.indexOfFirst { it == "category" }
    val unitIndex = headers.indexOfFirst { it == "unit" }
    val specIndex = headers.indexOfFirst { it == "specification" || it == "spec" || it == "size" }
    val purchasePriceIndex = headers.indexOfFirst { it == "purchase_price" || it == "cost_price" || it == "cost" }
    val sellingPriceIndex = headers.indexOfFirst { it == "selling_price" || it == "price" || it == "mrp" }
    val stockIndex = headers.indexOfFirst { it == "stock" || it == "quantity" || it == "qty" }

    if (nameIndex == -1 || sellingPriceIndex == -1) {
      return CsvImportResult(
        validProducts = emptyList(),
        skippedDuplicatesCount = 0,
        errorCount = 1,
        validationErrors = listOf("Missing required columns in CSV header: 'name' and 'selling_price' must be present."),
      )
    }

    val existingIdentityKeys = existingProducts.map { it.toIdentityKey() }.toMutableSet()
    val existingSkus = existingProducts.mapNotNull { it.sku?.trim()?.lowercase() }.filter { it.isNotBlank() }.toMutableSet()

    val validProducts = mutableListOf<ProductEntity>()
    val validationErrors = mutableListOf<String>()
    var skippedDuplicatesCount = 0

    for (rowIdx in 1 until lines.size) {
      val line = lines[rowIdx].trim()
      if (line.isBlank()) continue

      val tokens = line.split(delimiter).map { it.trim() }
      val lineNum = rowIdx + 1

      val name = tokens.getOrNull(nameIndex)?.trim().orEmpty()
      if (name.isBlank()) {
        validationErrors.add("Line $lineNum: Product name is required and cannot be empty.")
        continue
      }

      val sellingPriceStr = tokens.getOrNull(sellingPriceIndex)?.trim().orEmpty()
      val sellingPrice = sellingPriceStr.toDoubleOrNull()
      if (sellingPrice == null || sellingPrice < 0.0) {
        validationErrors.add("Line $lineNum: Invalid selling price '$sellingPriceStr' for '$name'. Must be a non-negative number.")
        continue
      }

      val purchasePriceStr = if (purchasePriceIndex != -1) tokens.getOrNull(purchasePriceIndex)?.trim().orEmpty() else ""
      val purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0
      if (purchasePrice < 0.0) {
        validationErrors.add("Line $lineNum: Purchase price cannot be negative for '$name'.")
        continue
      }

      val brand = if (brandIndex != -1) tokens.getOrNull(brandIndex)?.ifBlank { null } else null
      val sku = if (skuIndex != -1) tokens.getOrNull(skuIndex)?.ifBlank { null } else null
      val category = if (categoryIndex != -1) tokens.getOrNull(categoryIndex)?.ifBlank { "General" } ?: "General" else "General"
      val rawUnit = if (unitIndex != -1) tokens.getOrNull(unitIndex)?.trim()?.uppercase()?.ifBlank { "PIECE" } ?: "PIECE" else "PIECE"
      val specification = if (specIndex != -1) tokens.getOrNull(specIndex)?.ifBlank { null } else null
      val stock = if (stockIndex != -1) tokens.getOrNull(stockIndex)?.toDoubleOrNull() else 0.0

      // Unit normalization
      val (unit, customUnit) = when {
        rawUnit in STANDARD_UNITS -> Pair(rawUnit, null)
        rawUnit == "PCS" || rawUnit == "PC" -> Pair("PIECE", null)
        rawUnit == "PKT" || rawUnit == "PACK" -> Pair("PACKET", null)
        rawUnit == "MTR" || rawUnit == "METER" || rawUnit == "METERS" -> Pair("METRE", null)
        rawUnit == "KG" || rawUnit == "KGS" -> Pair("KILOGRAM", null)
        else -> Pair("CUSTOM", rawUnit.lowercase().replaceFirstChar { it.uppercase() })
      }

      val candidate = ProductEntity(
        id = UUID.randomUUID().toString(),
        name = name,
        brand = brand,
        sku = sku,
        category = category,
        unit = unit,
        customUnit = customUnit,
        specification = specification,
        purchasePricePaise = Money.toPaise(purchasePrice),
        sellingPricePaise = Money.toPaise(sellingPrice),
        currentStock = stock,
      )

      // Duplicate Check
      val candidateIdentity = candidate.toIdentityKey()
      val candidateSku = candidate.sku?.trim()?.lowercase()

      val isDuplicateSku = candidateSku != null && candidateSku.isNotBlank() && existingSkus.contains(candidateSku)
      val isDuplicateIdentity = existingIdentityKeys.contains(candidateIdentity)

      if (isDuplicateSku || isDuplicateIdentity) {
        skippedDuplicatesCount++
        continue
      }

      // Add to tracking sets for intra-file duplicate prevention
      existingIdentityKeys.add(candidateIdentity)
      if (candidateSku != null && candidateSku.isNotBlank()) {
        existingSkus.add(candidateSku)
      }

      validProducts.add(candidate)
    }

    return CsvImportResult(
      validProducts = validProducts,
      skippedDuplicatesCount = skippedDuplicatesCount,
      errorCount = validationErrors.size,
      validationErrors = validationErrors,
    )
  }
}
