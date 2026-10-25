package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyCard
import com.example.ui.components.EasyBizzyOutlinedButton
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.RupeeColorStyle
import com.example.ui.components.RupeeText
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyGreen
import com.example.ui.theme.EasyBizzyOrange
import com.example.ui.theme.EasyBizzyRed
import com.example.ui.theme.EasyBizzyThemeTokens
import com.example.ui.viewmodel.BillingViewModel
import com.example.ui.viewmodel.DraftCartItem
import kotlinx.coroutines.launch

/**
 * Fast POS / New Bill Creation screen for rapid counter transactions.
 * Features multi-product cart, quick product search, configurable GST tax treatment,
 * rounding, partial payments, and crash-resilient local Room draft autosave.
 */
@Composable
fun NewBillScreen(
  onBillSaved: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: BillingViewModel,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var showProductSearch by remember { mutableStateOf(false) }
  var showCustomItemDialog by remember { mutableStateOf(false) }
  var showCustomerDialog by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var savedBillForReceipt by remember { mutableStateOf<BillEntity?>(null) }
  var savedItemsForReceipt by remember { mutableStateOf<List<BillItemEntity>>(emptyList()) }

  val isShopGstRegistered = !uiState.shopSettings?.gstin.isNullOrBlank()

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
        .testTag("new_bill_screen"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Top Header & Tax Invoice / Receipt Mode
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "New Bill",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (uiState.isGstBill) "Tax Invoice Mode (GST)" else "Retail Receipt Mode (No Tax)",
            style = MaterialTheme.typography.bodySmall,
            color = if (uiState.isGstBill) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
          )
        }

        if (isShopGstRegistered) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(text = "GST", style = MaterialTheme.typography.labelMedium)
            Switch(
              checked = uiState.isGstBill,
              onCheckedChange = { viewModel.toggleGstBill(it) },
              modifier = Modifier.testTag("switch_gst_toggle"),
            )
          }
        } else {
          StatusPill(label = "Non-GST Retailer", color = MaterialTheme.colorScheme.outline)
        }
      }

      // Customer Bar
      Surface(
        shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showCustomerDialog = true }
          .testTag("btn_open_customer_select"),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
              )
            }

            Column {
              if (uiState.customerName.isNotBlank()) {
                Text(
                  text = uiState.customerName,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                val subtitle = buildString {
                  if (uiState.customerPhone.isNotBlank()) append(uiState.customerPhone)
                  if (uiState.customerId != null) append(" • Khata Account")
                }
                if (subtitle.isNotBlank()) {
                  Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              } else {
                Text(
                  text = "Add Customer / Khata (Optional)",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = "Required for credit (Udhar) or partial payments",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.outline,
                )
              }
            }
          }

          if (uiState.customerName.isNotBlank()) {
            IconButton(
              onClick = { viewModel.clearCustomer() },
              modifier = Modifier.testTag("btn_clear_customer"),
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Customer")
            }
          } else {
            Text(
              text = "Select",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }

      // Add Items Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        EasyBizzyPrimaryButton(
          text = "+ Quick Product",
          icon = Icons.Default.Search,
          onClick = { showProductSearch = true },
          modifier = Modifier
            .weight(1f)
            .testTag("btn_add_product"),
        )

        EasyBizzyOutlinedButton(
          text = "+ Loose Item",
          icon = Icons.Default.Add,
          onClick = { showCustomItemDialog = true },
          modifier = Modifier
            .weight(1f)
            .testTag("btn_add_loose_item"),
        )
      }

      // Cart Items List
      Text(
        text = "Bill Items (${uiState.items.size})",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      )

      if (uiState.items.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Default.ShoppingCart,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Cart is empty",
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "Tap '+ Quick Product' or '+ Loose Item' to add items.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline,
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          uiState.items.forEach { item ->
            CartItemRow(
              item = item,
              isGstBill = uiState.isGstBill,
              onQuantityChange = { viewModel.updateItemQuantity(item.id, it) },
              onPriceChange = { viewModel.updateItemUnitPrice(item.id, it) },
              onRemove = { viewModel.removeItem(item.id) },
            )
          }
        }
      }

      // Bill Calculations Card
      EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "Bill Summary",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )

          // Subtotal
          CalculationRow(label = "Subtotal", value = Money.format(uiState.subtotalPaise))

          // Bill Discount input
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(text = "Bill Discount (₹)", style = MaterialTheme.typography.bodyMedium)
            EasyBizzyTextField(
              value = uiState.billDiscountRupees,
              onValueChange = { viewModel.setBillDiscountRupees(it) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier
                .width(130.dp)
                .testTag("input_bill_discount"),
            )
          }

          // Tax breakdown
          if (uiState.isGstBill && uiState.taxPaise > 0L) {
            val cgst = uiState.taxPaise / 2L
            val sgst = uiState.taxPaise - cgst
            CalculationRow(label = "CGST", value = Money.format(cgst))
            CalculationRow(label = "SGST", value = Money.format(sgst))
            CalculationRow(label = "Total GST", value = Money.format(uiState.taxPaise))
          } else if (uiState.isGstBill) {
            CalculationRow(label = "Tax (GST 0%)", value = "₹ 0.00")
          }

          // Round-off Toggle & Value
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(text = "Round-off to ₹1", style = MaterialTheme.typography.bodyMedium)
              Switch(
                checked = uiState.applyRoundOff,
                onCheckedChange = { viewModel.toggleRoundOff(it) },
                modifier = Modifier.testTag("switch_roundoff"),
              )
            }
            val sign = if (uiState.roundOffPaise > 0L) "+" else ""
            Text(
              text = if (uiState.applyRoundOff) "$sign${Money.format(uiState.roundOffPaise)}" else "Off",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
          }

          HorizontalDivider()

          // Net Total
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "NET AMOUNT",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
            RupeeText(
              paise = uiState.netAmountPaise,
              style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
              colorStyle = RupeeColorStyle.INCOME,
            )
          }
        }
      }

      // Payment Method Section
      EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "Payment Mode",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )

          // Payment mode filter chips
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            listOf("CASH" to "Cash", "UPI" to "UPI", "KHATA" to "Khata (Credit)", "SPLIT" to "Split (Cash+UPI)").forEach { (mode, label) ->
              FilterChip(
                selected = uiState.paymentMode == mode,
                onClick = { viewModel.setPaymentMode(mode) },
                label = { Text(label) },
                modifier = Modifier.testTag("payment_chip_$mode"),
              )
            }
          }

          // Payment Mode specific controls
          when (uiState.paymentMode) {
            "CASH" -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(text = "Cash Received (₹)", style = MaterialTheme.typography.bodyMedium)
                EasyBizzyTextField(
                  value = uiState.cashReceivedRupees,
                  onValueChange = { viewModel.setCashReceivedRupees(it) },
                  placeholder = Money.toRupees(uiState.netAmountPaise).toString(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier
                    .width(140.dp)
                    .testTag("input_cash_tendered"),
                )
              }

              if (uiState.changeToReturnPaise > 0L) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(EasyBizzyGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(text = "Change to Return:", fontWeight = FontWeight.Bold, color = EasyBizzyGreen)
                  Text(text = Money.format(uiState.changeToReturnPaise), fontWeight = FontWeight.Bold, color = EasyBizzyGreen)
                }
              }
            }

            "UPI" -> {
              val upiId = uiState.shopSettings?.upiId
              if (!upiId.isNullOrBlank()) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                      Text(text = "Merchant UPI ID", style = MaterialTheme.typography.labelSmall)
                      Text(text = upiId, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                  }
                }
              }
            }

            "KHATA" -> {
              if (uiState.customerId == null) {
                Text(
                  text = "⚠️ Please select a customer above for Khata (Udhar) bills.",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                  color = EasyBizzyOrange,
                )
              } else {
                Text(
                  text = "Full amount of ${Money.format(uiState.netAmountPaise)} will be debited to ${uiState.customerName}'s account.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            "SPLIT" -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                EasyBizzyTextField(
                  value = uiState.cashReceivedRupees,
                  onValueChange = { viewModel.setCashReceivedRupees(it) },
                  label = "Cash (₹)",
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier.weight(1f),
                  testTag = "input_split_cash",
                )
                EasyBizzyTextField(
                  value = uiState.upiReceivedRupees,
                  onValueChange = { viewModel.setUpiReceivedRupees(it) },
                  label = "UPI (₹)",
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier.weight(1f),
                  testTag = "input_split_upi",
                )
              }
              if (uiState.balanceDuePaise > 0L) {
                Text(
                  text = "Remaining balance due: ${Money.format(uiState.balanceDuePaise)} (Requires customer for Khata)",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                  color = EasyBizzyOrange,
                )
              }
            }
          }
        }
      }

      // Actions: Clear Cart and Complete Bill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        EasyBizzyOutlinedButton(
          text = "Clear Bill",
          onClick = { showClearConfirmDialog = true },
          modifier = Modifier
            .weight(1f)
            .testTag("btn_clear_bill"),
        )

        EasyBizzyPrimaryButton(
          text = if (uiState.isSaving) "Saving..." else "Complete & Save Bill",
          icon = Icons.Default.CheckCircle,
          enabled = uiState.items.isNotEmpty() && uiState.netAmountPaise > 0L && !uiState.isSaving,
          onClick = {
            viewModel.saveBill(
              onSuccess = { savedBill ->
                // Capture items snapshot for receipt dialog
                savedBillForReceipt = savedBill
                savedItemsForReceipt = uiState.items.map { item ->
                  BillItemEntity(
                    billId = savedBill.id,
                    productName = item.productName,
                    brand = item.brand,
                    specification = item.specification,
                    unit = item.unit,
                    quantity = item.quantity,
                    unitPricePaise = item.unitPricePaise,
                    discountPaise = item.discountPaise,
                    taxRatePercent = item.taxRatePercent,
                    taxPaise = item.calculation.taxPaise,
                    totalPricePaise = item.lineTotalPaise,
                  )
                }
                onBillSaved()
              },
              onError = { err ->
                scope.launch {
                  snackbarHostState.showSnackbar(err)
                }
              },
            )
          },
          modifier = Modifier
            .weight(2f)
            .testTag("btn_complete_bill"),
        )
      }
    }

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
  }

  // Dialogs
  if (showProductSearch) {
    QuickProductSearchDialog(
      products = uiState.availableProducts,
      onProductSelected = { viewModel.addProductToCart(it) },
      onDismiss = { showProductSearch = false },
    )
  }

  if (showCustomItemDialog) {
    AddCustomLineItemDialog(
      onAddItem = { name, price, qty, unit ->
        viewModel.addCustomItem(name, price, qty, unit)
      },
      onDismiss = { showCustomItemDialog = false },
    )
  }

  if (showCustomerDialog) {
    CustomerSelectDialog(
      customers = uiState.availableCustomers,
      onCustomerSelected = { viewModel.selectCustomer(it) },
      onManualCustomerEntered = { name, phone, gstin ->
        viewModel.updateCustomerName(name)
        viewModel.updateCustomerPhone(phone)
        viewModel.updateCustomerGstin(gstin)
      },
      onDismiss = { showCustomerDialog = false },
    )
  }

  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Clear Current Bill?") },
      text = { Text("This will discard all items and customer details in the active draft.") },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.clearCart()
            showClearConfirmDialog = false
          },
          modifier = Modifier.testTag("btn_confirm_clear_bill"),
        ) {
          Text("Clear", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      },
    )
  }

  savedBillForReceipt?.let { bill ->
    BillReceiptDialog(
      bill = bill,
      items = savedItemsForReceipt,
      settings = uiState.shopSettings,
      onDismiss = {
        savedBillForReceipt = null
        savedItemsForReceipt = emptyList()
      },
    )
  }
}

@Composable
private fun CartItemRow(
  item: DraftCartItem,
  isGstBill: Boolean,
  onQuantityChange: (Double) -> Unit,
  onPriceChange: (Long) -> Unit,
  onRemove: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("cart_item_${item.id}"),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.productName,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
          )
          val detailStr = buildString {
            if (!item.brand.isNullOrBlank()) append("${item.brand} • ")
            if (!item.specification.isNullOrBlank()) append("${item.specification} • ")
            append(item.unit)
          }
          Text(
            text = detailStr,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp).testTag("btn_remove_item_${item.id}")) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Quantity adjustment
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          IconButton(
            onClick = { onQuantityChange((item.quantity - 1.0).coerceAtLeast(0.0)) },
            modifier = Modifier.size(32.dp).testTag("btn_qty_minus_${item.id}"),
          ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
          }

          Text(
            text = String.format(java.util.Locale.ROOT, "%.1f", item.quantity),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 4.dp),
          )

          IconButton(
            onClick = { onQuantityChange(item.quantity + 1.0) },
            modifier = Modifier.size(32.dp).testTag("btn_qty_plus_${item.id}"),
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
          }
        }

        // Price and Line Total
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${Money.format(item.unitPricePaise)} / ${item.unit}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = Money.format(item.lineTotalPaise),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}

@Composable
private fun CalculationRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
  }
}
