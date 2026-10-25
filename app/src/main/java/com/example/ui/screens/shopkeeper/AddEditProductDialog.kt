package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.ProductEntity
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyOutlinedButton
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.theme.EasyBizzyThemeTokens
import java.util.UUID

val PREDEFINED_CATEGORIES = listOf(
  "Hardware",
  "Electrical",
  "Building Material",
  "Plumbing",
  "Paint",
  "Kirana",
  "General",
)

val PREDEFINED_UNITS = listOf(
  "PIECE" to "Piece (Pcs)",
  "BOX" to "Box",
  "PACKET" to "Packet (Pkt)",
  "BAG" to "Bag (50kg/Cement)",
  "METRE" to "Metre (m)",
  "KILOGRAM" to "Kilogram (kg)",
  "CUSTOM" to "Custom Unit",
)

/**
 * Modal form for creating or editing inventory products.
 * Accommodates hardware, electrical, and construction materials with exact specifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
  existingProduct: ProductEntity?,
  onDismiss: () -> Unit,
  onSave: (ProductEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  var name by remember { mutableStateOf(existingProduct?.name.orEmpty()) }
  var brand by remember { mutableStateOf(existingProduct?.brand.orEmpty()) }
  var sku by remember { mutableStateOf(existingProduct?.sku.orEmpty()) }
  var barcode by remember { mutableStateOf(existingProduct?.barcode.orEmpty()) }
  var category by remember { mutableStateOf(existingProduct?.category ?: "Hardware") }
  var unit by remember { mutableStateOf(existingProduct?.unit ?: "PIECE") }
  var customUnit by remember { mutableStateOf(existingProduct?.customUnit.orEmpty()) }
  var specification by remember { mutableStateOf(existingProduct?.specification.orEmpty()) }
  var purchasePriceRupees by remember {
    mutableStateOf(if (existingProduct != null) Money.toRupees(existingProduct.purchasePricePaise).toString() else "")
  }
  var sellingPriceRupees by remember {
    mutableStateOf(if (existingProduct != null) Money.toRupees(existingProduct.sellingPricePaise).toString() else "")
  }
  var stockQuantity by remember {
    mutableStateOf(existingProduct?.currentStock?.toString() ?: "10")
  }
  var minStockAlert by remember {
    mutableStateOf(existingProduct?.minStockAlert?.toString() ?: "5")
  }
  var isMarketplaceLive by remember {
    mutableStateOf(existingProduct?.isMarketplacePublished ?: false)
  }

  var nameError by remember { mutableStateOf<String?>(null) }
  var priceError by remember { mutableStateOf<String?>(null) }
  var customUnitError by remember { mutableStateOf<String?>(null) }

  BasicAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = modifier.padding(16.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("add_edit_product_dialog"),
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Title & Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (existingProduct == null) "Add New Product" else "Edit Product",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          )
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_close_button")) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider()

        // Product Name
        EasyBizzyTextField(
          value = name,
          onValueChange = {
            name = it
            nameError = if (it.isBlank()) "Product name is required" else null
          },
          label = "Product Name *",
          placeholder = "e.g., Copper Wire 90m, TMT Rebar, Switch",
          errorMessage = nameError,
          testTag = "input_product_name",
        )

        // Brand & SKU Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          EasyBizzyTextField(
            value = brand,
            onValueChange = { brand = it },
            label = "Brand",
            placeholder = "e.g., Havells, UltraTech",
            modifier = Modifier.weight(1f),
            testTag = "input_product_brand",
          )
          EasyBizzyTextField(
            value = sku,
            onValueChange = { sku = it },
            label = "SKU / Code",
            placeholder = "e.g., HAV-15",
            modifier = Modifier.weight(1f),
            testTag = "input_product_sku",
          )
        }

        // Specification / Size (Critical for distinguishing 1.5 sq mm from 2.5 sq mm or 10mm from 12mm)
        EasyBizzyTextField(
          value = specification,
          onValueChange = { specification = it },
          label = "Size / Technical Specification",
          placeholder = "e.g., 1.5 sq mm 90m roll, 12mm x 40ft, Grade 53",
          supportingText = "Differentiates this product from other sizes/varieties",
          testTag = "input_product_spec",
        )

        // Category Picker
        Text(
          text = "Category",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          PREDEFINED_CATEGORIES.forEach { cat ->
            FilterChip(
              selected = category == cat,
              onClick = { category = cat },
              label = { Text(cat) },
              modifier = Modifier.testTag("chip_cat_$cat"),
            )
          }
        }

        // Unit Selection
        Text(
          text = "Unit of Sale",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          PREDEFINED_UNITS.forEach { (unitKey, label) ->
            FilterChip(
              selected = unit == unitKey,
              onClick = { unit = unitKey },
              label = { Text(label) },
              modifier = Modifier.testTag("chip_unit_$unitKey"),
            )
          }
        }

        if (unit == "CUSTOM") {
          EasyBizzyTextField(
            value = customUnit,
            onValueChange = {
              customUnit = it
              customUnitError = if (it.isBlank()) "Specify custom unit name" else null
            },
            label = "Custom Unit Name *",
            placeholder = "e.g., Litre, Drum, Roll, Bundle",
            errorMessage = customUnitError,
            testTag = "input_custom_unit",
          )
        }

        // Pricing Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          EasyBizzyTextField(
            value = purchasePriceRupees,
            onValueChange = { purchasePriceRupees = it },
            label = "Purchase Cost (Wholesale)",
            prefix = "₹ ",
            placeholder = "0.00",
            supportingText = "Private cost",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            testTag = "input_purchase_price",
          )
          EasyBizzyTextField(
            value = sellingPriceRupees,
            onValueChange = {
              sellingPriceRupees = it
              priceError = if (it.toDoubleOrNull() == null || it.toDouble() < 0) "Invalid selling price" else null
            },
            label = "Selling Price *",
            prefix = "₹ ",
            placeholder = "0.00",
            errorMessage = priceError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            testTag = "input_selling_price",
          )
        }

        // Stock & Alert Threshold
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          EasyBizzyTextField(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = "Current Stock Quantity",
            placeholder = "e.g. 50",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            testTag = "input_stock_qty",
          )
          EasyBizzyTextField(
            value = minStockAlert,
            onValueChange = { minStockAlert = it },
            label = "Low Stock Alert At",
            placeholder = "e.g. 5",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            testTag = "input_min_stock",
          )
        }

        // Marketplace Toggle
        Surface(
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Publish to Public Marketplace",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              )
              Text(
                text = "Allows nearby customers to see this product and price",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
              )
            }
            Switch(
              checked = isMarketplaceLive,
              onCheckedChange = { isMarketplaceLive = it },
              modifier = Modifier.testTag("switch_product_marketplace"),
            )
          }
        }

        // Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          EasyBizzyOutlinedButton(
            text = "Cancel",
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            testTag = "btn_cancel_product",
          )

          EasyBizzyPrimaryButton(
            text = "Save Product",
            onClick = {
              if (name.isBlank()) {
                nameError = "Product name is required"
                return@EasyBizzyPrimaryButton
              }
              val sellingPrice = sellingPriceRupees.toDoubleOrNull()
              if (sellingPrice == null || sellingPrice < 0) {
                priceError = "Valid selling price is required"
                return@EasyBizzyPrimaryButton
              }
              if (unit == "CUSTOM" && customUnit.isBlank()) {
                customUnitError = "Specify custom unit"
                return@EasyBizzyPrimaryButton
              }

              val purchasePrice = purchasePriceRupees.toDoubleOrNull() ?: 0.0
              val stock = stockQuantity.toDoubleOrNull()
              val minAlert = minStockAlert.toDoubleOrNull() ?: 5.0

              val productToSave = (existingProduct ?: ProductEntity(name = name)).copy(
                name = name.trim(),
                brand = brand.trim().ifBlank { null },
                sku = sku.trim().ifBlank { null },
                barcode = barcode.trim().ifBlank { null },
                category = category,
                unit = unit,
                customUnit = if (unit == "CUSTOM") customUnit.trim() else null,
                specification = specification.trim().ifBlank { null },
                purchasePricePaise = Money.toPaise(purchasePrice),
                sellingPricePaise = Money.toPaise(sellingPrice),
                currentStock = stock,
                minStockAlert = minAlert,
                isMarketplacePublished = isMarketplaceLive,
                updatedAt = System.currentTimeMillis(),
              )

              onSave(productToSave)
            },
            modifier = Modifier.weight(1f),
            testTag = "btn_save_product_form",
          )
        }
      }
    }
  }
}
