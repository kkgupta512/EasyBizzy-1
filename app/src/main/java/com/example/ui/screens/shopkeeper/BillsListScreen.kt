package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.data.repository.BillingRepository
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.RupeeColorStyle
import com.example.ui.components.RupeeText
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyGreen
import com.example.ui.theme.EasyBizzyOrange
import com.example.ui.theme.EasyBizzyRed
import com.example.ui.theme.EasyBizzyThemeTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen displaying the history of created bills and invoices with instant search,
 * payment status filtering, detailed receipt dialog, sharing, and printing.
 */
@Composable
fun BillsListScreen(
  onNewBillClick: () -> Unit,
  modifier: Modifier = Modifier,
  bills: List<BillEntity> = emptyList(),
  billingRepository: BillingRepository? = null,
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("ALL") }

  var selectedBillForReceipt by remember { mutableStateOf<BillEntity?>(null) }
  var selectedBillItems by remember { mutableStateOf<List<BillItemEntity>>(emptyList()) }
  var shopSettings by remember { mutableStateOf<ShopSettingsEntity?>(null) }

  val scope = rememberCoroutineScope()

  LaunchedEffect(billingRepository) {
    shopSettings = billingRepository?.getShopSettingsOnce()
  }

  val filteredBills = remember(bills, searchQuery, selectedFilter) {
    bills.filter { bill ->
      val q = searchQuery.trim().lowercase()
      val matchesSearch = q.isBlank() ||
        bill.billNumber.lowercase().contains(q) ||
        (bill.customerName?.lowercase()?.contains(q) == true) ||
        (bill.customerPhone?.contains(q) == true)

      val matchesFilter = when (selectedFilter) {
        "ALL" -> true
        "PAID" -> bill.paymentStatus == "PAID"
        "PARTIALLY_PAID" -> bill.paymentStatus == "PARTIALLY_PAID"
        "UNPAID" -> bill.paymentStatus == "UNPAID" || bill.paymentMode == "KHATA"
        "CASH" -> bill.paymentMode == "CASH"
        "UPI" -> bill.paymentMode == "UPI"
        else -> true
      }
      matchesSearch && matchesFilter
    }
  }

  val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("en", "IN")) }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onNewBillClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_new_bill"),
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "New Bill")
      }
    },
    modifier = modifier,
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
        .testTag("bills_list_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Bills & Receipts",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          )
          Text(
            text = "${bills.size} total bills recorded offline",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      EasyBizzyTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = "Search by bill number, customer or phone",
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        testTag = "input_search_bills",
      )

      // Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf(
          "ALL" to "All (${bills.size})",
          "PAID" to "Paid",
          "PARTIALLY_PAID" to "Partially Paid",
          "UNPAID" to "Khata / Due",
          "CASH" to "Cash",
          "UPI" to "UPI",
        ).forEach { (filterKey, label) ->
          FilterChip(
            selected = selectedFilter == filterKey,
            onClick = { selectedFilter = filterKey },
            label = { Text(label) },
            modifier = Modifier.testTag("filter_bill_$filterKey"),
          )
        }
      }

      // Bills List
      if (filteredBills.isEmpty()) {
        EasyBizzyEmptyState(
          title = if (searchQuery.isBlank()) "No bills created yet" else "No matching bills found",
          description = if (searchQuery.isBlank()) "Create your first bill to record cash, UPI, or Khata sales." else "Try searching with a different term.",
          icon = Icons.Default.ReceiptLong,
          actionLabel = if (searchQuery.isBlank()) "Create First Bill" else null,
          onAction = onNewBillClick,
          modifier = Modifier.weight(1f),
        )
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(filteredBills, key = { it.id }) { bill ->
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
              shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  scope.launch {
                    val details = billingRepository?.getBillDetails(bill.id)
                    selectedBillItems = details?.items ?: emptyList()
                    selectedBillForReceipt = bill
                  }
                }
                .testTag("bill_card_${bill.id}"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column {
                    Text(
                      text = bill.billNumber,
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                      text = dateFormat.format(Date(bill.createdAt)),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }

                  val statusColor = when (bill.paymentStatus) {
                    "PAID" -> EasyBizzyGreen
                    "PARTIALLY_PAID" -> EasyBizzyOrange
                    else -> EasyBizzyRed
                  }
                  StatusPill(label = bill.paymentStatus.replace("_", " "), color = statusColor)
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column {
                    Text(
                      text = bill.customerName?.ifBlank { "Counter Sale" } ?: "Counter Sale",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                      text = "Mode: ${bill.paymentMode}${if (bill.isGstBill) " • GST" else ""}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }

                  Column(horizontalAlignment = Alignment.End) {
                    RupeeText(
                      paise = bill.netAmountPaise,
                      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                      colorStyle = RupeeColorStyle.INCOME,
                    )
                    if (bill.balanceDuePaise > 0L) {
                      Text(
                        text = "Due: ${Money.format(bill.balanceDuePaise)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = EasyBizzyOrange,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  selectedBillForReceipt?.let { bill ->
    BillReceiptDialog(
      bill = bill,
      items = selectedBillItems,
      settings = shopSettings,
      onDismiss = {
        selectedBillForReceipt = null
        selectedBillItems = emptyList()
      },
    )
  }
}
