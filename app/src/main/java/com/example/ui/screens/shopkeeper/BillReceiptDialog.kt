package com.example.ui.screens.shopkeeper

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.data.util.BillReceiptFormatter
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyOutlinedButton
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyGreen
import com.example.ui.theme.EasyBizzyOrange
import com.example.ui.theme.EasyBizzyRed
import com.example.ui.theme.EasyBizzyThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillReceiptDialog(
  bill: BillEntity,
  items: List<BillItemEntity>,
  settings: ShopSettingsEntity?,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val isTaxInvoice = bill.isGstBill && !bill.sellerGstin.isNullOrBlank()
  val dateFormatted = remember(bill.createdAt) {
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("en", "IN")).format(Date(bill.createdAt))
  }

  val plainTextReceipt = remember(bill, items, settings) {
    BillReceiptFormatter.formatPlainTextReceipt(bill, items, settings)
  }

  val htmlReceipt = remember(bill, items, settings) {
    BillReceiptFormatter.formatHtmlReceipt(bill, items, settings)
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("dialog_bill_receipt"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Header Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = if (isTaxInvoice) "TAX INVOICE" else "RETAIL RECEIPT",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = bill.billNumber,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_receipt")) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider()

        // Scrollable Receipt Content
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // Shop & Customer Info Card
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = settings?.shopName?.ifBlank { "EasyBizzy Store" } ?: "EasyBizzy Store",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              )
              if (!settings?.phone.isNullOrBlank()) {
                Text(text = "Phone: ${settings?.phone}", style = MaterialTheme.typography.bodySmall)
              }
              if (isTaxInvoice) {
                Text(text = "Seller GSTIN: ${bill.sellerGstin}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
              } else {
                Text(
                  text = "Unregistered Seller under GST (Not eligible to collect tax)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.outline,
                )
              }

              HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

              Text(text = "Date: $dateFormatted", style = MaterialTheme.typography.bodySmall)
              if (!bill.customerName.isNullOrBlank()) {
                Text(text = "Customer: ${bill.customerName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
              }
              if (!bill.customerPhone.isNullOrBlank()) {
                Text(text = "Customer Phone: ${bill.customerPhone}", style = MaterialTheme.typography.bodySmall)
              }
              if (!bill.customerGstin.isNullOrBlank()) {
                Text(text = "Customer GSTIN: ${bill.customerGstin}", style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          // Items Table
          Text(text = "Purchased Items", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

          items.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.productName,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                val detailStr = buildString {
                  if (!item.specification.isNullOrBlank()) append("${item.specification} • ")
                  append("${item.quantity} ${item.unit} @ ${Money.format(item.unitPricePaise)}")
                  if (item.discountPaise > 0L) append(" (Disc -${Money.format(item.discountPaise)})")
                }
                Text(text = detailStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Text(
                text = Money.format(item.totalPricePaise),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              )
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          // Financial Summary
          SummaryRow(label = "Subtotal", value = Money.format(bill.subtotalPaise))
          if (bill.discountPaise > 0L) {
            SummaryRow(label = "Discount", value = "-${Money.format(bill.discountPaise)}", valueColor = EasyBizzyGreen)
          }
          if (isTaxInvoice && bill.taxPaise > 0L) {
            val cgst = bill.taxPaise / 2L
            val sgst = bill.taxPaise - cgst
            SummaryRow(label = "CGST", value = Money.format(cgst))
            SummaryRow(label = "SGST", value = Money.format(sgst))
            SummaryRow(label = "Total Tax (GST)", value = Money.format(bill.taxPaise))
          }
          if (bill.roundOffPaise != 0L) {
            val sign = if (bill.roundOffPaise > 0L) "+" else ""
            SummaryRow(label = "Round-off", value = "$sign${Money.format(bill.roundOffPaise)}")
          }

          HorizontalDivider()

          // Net Total
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(text = "NET TOTAL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
              text = Money.format(bill.netAmountPaise),
              style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
            )
          }

          // Payment Status Card
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(text = "Payment: ${bill.paymentMode}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(text = "Paid: ${Money.format(bill.paidAmountPaise)}", style = MaterialTheme.typography.bodySmall)
                if (bill.balanceDuePaise > 0L) {
                  Text(
                    text = "Balance Due (Khata): ${Money.format(bill.balanceDuePaise)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = EasyBizzyOrange,
                  )
                }
              }

              val statusColor = when (bill.paymentStatus) {
                "PAID" -> EasyBizzyGreen
                "PARTIALLY_PAID" -> EasyBizzyOrange
                else -> EasyBizzyRed
              }
              StatusPill(label = bill.paymentStatus.replace("_", " "), color = statusColor)
            }
          }
        }

        HorizontalDivider()

        // Actions: Share, Print, Done
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          EasyBizzyOutlinedButton(
            text = "Share",
            icon = Icons.Default.Share,
            onClick = {
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, plainTextReceipt)
                type = "text/plain"
              }
              context.startActivity(Intent.createChooser(sendIntent, "Share Bill Receipt"))
            },
            modifier = Modifier
              .weight(1f)
              .testTag("btn_share_bill"),
          )

          EasyBizzyOutlinedButton(
            text = "Print",
            icon = Icons.Default.Print,
            onClick = {
              printHtmlReceipt(context, htmlReceipt, bill.billNumber)
            },
            modifier = Modifier
              .weight(1f)
              .testTag("btn_print_bill"),
          )

          EasyBizzyPrimaryButton(
            text = "Done",
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("btn_done_bill"),
          )
        }
      }
    }
  }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = valueColor)
  }
}

private fun printHtmlReceipt(context: Context, html: String, jobName: String) {
  try {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
      override fun onPageFinished(view: WebView, url: String) {
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
      }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
  } catch (_: Exception) {
    // Gracefully handle devices where printing service is unavailable
  }
}
