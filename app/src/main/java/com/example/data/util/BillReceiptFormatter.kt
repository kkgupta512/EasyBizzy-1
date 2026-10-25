package com.example.data.util

import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.ShopSettingsEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formatter for generating professional, compliant retail receipts and tax invoices.
 * Accurately differentiates GST registered tax invoices from unregistered/composition retail receipts.
 */
object BillReceiptFormatter {

  private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("en", "IN"))

  /**
   * Formats a clean, text-based receipt suitable for WhatsApp, SMS, or thermal printer sharing.
   */
  fun formatPlainTextReceipt(
    bill: BillEntity,
    items: List<BillItemEntity>,
    settings: ShopSettingsEntity?,
  ): String {
    val shopName = settings?.shopName?.ifBlank { "EasyBizzy Store" } ?: "EasyBizzy Store"
    val phone = settings?.phone?.ifBlank { null }
    val address = settings?.address?.ifBlank { null }
    val sellerGstin = bill.sellerGstin ?: settings?.gstin

    val isTaxInvoice = bill.isGstBill && !sellerGstin.isNullOrBlank()
    val docTitle = if (isTaxInvoice) "TAX INVOICE" else "RETAIL RECEIPT"

    val sb = StringBuilder()
    sb.appendLine("========================================")
    sb.appendLine(shopName.center(40))
    if (!address.isNullOrBlank()) sb.appendLine(address.center(40))
    if (!phone.isNullOrBlank()) sb.appendLine("Ph: $phone".center(40))
    if (isTaxInvoice) {
      sb.appendLine("GSTIN: $sellerGstin".center(40))
    } else {
      sb.appendLine("(Unregistered / Composition Retailer)".center(40))
    }
    sb.appendLine("========================================")
    sb.appendLine(docTitle.center(40))
    sb.appendLine("Bill No: ${bill.billNumber}")
    sb.appendLine("Date: ${dateFormat.format(Date(bill.createdAt))}")
    if (!bill.customerName.isNullOrBlank()) {
      sb.appendLine("Customer: ${bill.customerName}")
    }
    if (!bill.customerPhone.isNullOrBlank()) {
      sb.appendLine("Customer Phone: ${bill.customerPhone}")
    }
    if (!bill.customerGstin.isNullOrBlank()) {
      sb.appendLine("Customer GSTIN: ${bill.customerGstin}")
    }
    sb.appendLine("----------------------------------------")
    sb.appendLine(String.format(Locale.ROOT, "%-18s %4s %7s %8s", "Item", "Qty", "Rate", "Total"))
    sb.appendLine("----------------------------------------")

    for (item in items) {
      val nameLine = buildString {
        append(item.productName)
        if (!item.specification.isNullOrBlank()) append(" (${item.specification})")
      }
      // Wrap long names
      sb.appendLine(nameLine)
      val qtyStr = String.format(Locale.ROOT, "%.1f %s", item.quantity, item.unit)
      val rateStr = Money.format(item.unitPricePaise, showDecimals = false).replace("₹", "").trim()
      val totalStr = Money.format(item.totalPricePaise, showDecimals = false).replace("₹", "").trim()
      sb.appendLine(String.format(Locale.ROOT, "  %-16s %7s %8s", qtyStr, rateStr, totalStr))
    }

    sb.appendLine("----------------------------------------")
    sb.appendLine(String.format(Locale.ROOT, "%-25s: %12s", "Subtotal", Money.format(bill.subtotalPaise)))
    if (bill.discountPaise > 0L) {
      sb.appendLine(String.format(Locale.ROOT, "%-25s: -%11s", "Discount", Money.format(bill.discountPaise)))
    }
    if (isTaxInvoice && bill.taxPaise > 0L) {
      val cgst = bill.taxPaise / 2L
      val sgst = bill.taxPaise - cgst
      sb.appendLine(String.format(Locale.ROOT, "%-25s: %12s", "CGST", Money.format(cgst)))
      sb.appendLine(String.format(Locale.ROOT, "%-25s: %12s", "SGST", Money.format(sgst)))
      sb.appendLine(String.format(Locale.ROOT, "%-25s: %12s", "Total Tax (GST)", Money.format(bill.taxPaise)))
    }
    if (bill.roundOffPaise != 0L) {
      val sign = if (bill.roundOffPaise > 0) "+" else ""
      val roundOffStr = "$sign${Money.format(bill.roundOffPaise)}"
      sb.appendLine(String.format(Locale.ROOT, "%-25s: %12s", "Round-off", roundOffStr))
    }
    sb.appendLine("----------------------------------------")
    sb.appendLine(String.format(Locale.ROOT, "%-25s: %12s", "NET TOTAL", Money.format(bill.netAmountPaise)))
    sb.appendLine("----------------------------------------")
    sb.appendLine("Payment Mode: ${bill.paymentMode}")
    sb.appendLine("Status: ${bill.paymentStatus.replace("_", " ")}")
    sb.appendLine("Amount Paid: ${Money.format(bill.paidAmountPaise)}")
    if (bill.balanceDuePaise > 0L) {
      sb.appendLine("Balance Due (Khata): ${Money.format(bill.balanceDuePaise)}")
    }
    sb.appendLine("========================================")
    val footer = settings?.receiptFooter?.ifBlank { "Thank you for shopping with us!" }
      ?: "Thank you for shopping with us!"
    sb.appendLine(footer.center(40))
    if (!isTaxInvoice) {
      sb.appendLine("Composition / Unregistered Seller".center(40))
      sb.appendLine("Not eligible to collect tax".center(40))
    }
    sb.appendLine("========================================")
    return sb.toString()
  }

  /**
   * Formats HTML receipt for printing via Android PrintManager.
   */
  fun formatHtmlReceipt(
    bill: BillEntity,
    items: List<BillItemEntity>,
    settings: ShopSettingsEntity?,
  ): String {
    val shopName = settings?.shopName?.ifBlank { "EasyBizzy Store" } ?: "EasyBizzy Store"
    val phone = settings?.phone?.ifBlank { "" } ?: ""
    val address = settings?.address?.ifBlank { "" } ?: ""
    val sellerGstin = bill.sellerGstin ?: settings?.gstin
    val isTaxInvoice = bill.isGstBill && !sellerGstin.isNullOrBlank()
    val docTitle = if (isTaxInvoice) "TAX INVOICE" else "RETAIL RECEIPT"

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          body { font-family: monospace, sans-serif; margin: 10px; font-size: 13px; color: #111; }
          .header { text-align: center; border-bottom: 2px dashed #333; padding-bottom: 8px; margin-bottom: 8px; }
          .title { font-size: 16px; font-weight: bold; margin: 4px 0; }
          .shop { font-size: 18px; font-weight: bold; }
          .meta { font-size: 12px; margin-bottom: 8px; }
          table { width: 100%; border-collapse: collapse; margin-top: 8px; }
          th { border-bottom: 1px dashed #333; text-align: left; padding: 4px 2px; }
          td { padding: 4px 2px; }
          .right { text-align: right; }
          .totals { border-top: 1px dashed #333; border-bottom: 1px dashed #333; margin-top: 8px; padding: 6px 0; }
          .totals-row { display: flex; justify-content: space-between; margin: 3px 0; }
          .grand-total { font-size: 15px; font-weight: bold; border-top: 1px solid #333; padding-top: 4px; }
          .footer { text-align: center; margin-top: 12px; font-size: 11px; color: #555; }
        </style>
      </head>
      <body>
        <div class="header">
          <div class="shop">$shopName</div>
          ${if (address.isNotBlank()) "<div>$address</div>" else ""}
          ${if (phone.isNotBlank()) "<div>Ph: $phone</div>" else ""}
          ${if (isTaxInvoice) "<div>GSTIN: $sellerGstin</div>" else "<div>(Unregistered / Composition Retailer)</div>"}
          <div class="title">$docTitle</div>
        </div>

        <div class="meta">
          <div><strong>Bill No:</strong> ${bill.billNumber}</div>
          <div><strong>Date:</strong> ${dateFormat.format(Date(bill.createdAt))}</div>
          ${if (!bill.customerName.isNullOrBlank()) "<div><strong>Customer:</strong> ${bill.customerName}</div>" else ""}
          ${if (!bill.customerPhone.isNullOrBlank()) "<div><strong>Phone:</strong> ${bill.customerPhone}</div>" else ""}
          ${if (!bill.customerGstin.isNullOrBlank()) "<div><strong>Buyer GSTIN:</strong> ${bill.customerGstin}</div>" else ""}
        </div>

        <table>
          <thead>
            <tr>
              <th>Item</th>
              <th class="right">Qty</th>
              <th class="right">Rate</th>
              <th class="right">Total</th>
            </tr>
          </thead>
          <tbody>
            ${items.joinToString("") { item ->
              val desc = item.productName + (if (!item.specification.isNullOrBlank()) " (${item.specification})" else "")
              "<tr><td>$desc</td><td class='right'>${item.quantity} ${item.unit}</td><td class='right'>${Money.format(item.unitPricePaise)}</td><td class='right'>${Money.format(item.totalPricePaise)}</td></tr>"
            }}
          </tbody>
        </table>

        <div class="totals">
          <div class="totals-row"><span>Subtotal:</span><span>${Money.format(bill.subtotalPaise)}</span></div>
          ${if (bill.discountPaise > 0L) "<div class='totals-row'><span>Discount:</span><span>-${Money.format(bill.discountPaise)}</span></div>" else ""}
          ${if (isTaxInvoice && bill.taxPaise > 0L) {
            val cgst = bill.taxPaise / 2L
            val sgst = bill.taxPaise - cgst
            "<div class='totals-row'><span>CGST:</span><span>${Money.format(cgst)}</span></div>" +
            "<div class='totals-row'><span>SGST:</span><span>${Money.format(sgst)}</span></div>"
          } else ""}
          ${if (bill.roundOffPaise != 0L) "<div class='totals-row'><span>Round-off:</span><span>${Money.format(bill.roundOffPaise)}</span></div>" else ""}
          <div class="totals-row grand-total"><span>NET TOTAL:</span><span>${Money.format(bill.netAmountPaise)}</span></div>
        </div>

        <div style="margin-top: 8px;">
          <div><strong>Payment:</strong> ${bill.paymentMode} (${bill.paymentStatus.replace("_", " ")})</div>
          <div><strong>Paid:</strong> ${Money.format(bill.paidAmountPaise)}</div>
          ${if (bill.balanceDuePaise > 0L) "<div><strong>Balance Due (Khata):</strong> ${Money.format(bill.balanceDuePaise)}</div>" else ""}
        </div>

        <div class="footer">
          <div>${settings?.receiptFooter ?: "Thank you for shopping with us!"}</div>
          ${if (!isTaxInvoice) "<div>Unregistered Retailer under GST - Not eligible to collect tax</div>" else ""}
        </div>
      </body>
      </html>
    """.trimIndent()
  }

  private fun String.center(width: Int): String {
    if (length >= width) return this
    val padding = (width - length) / 2
    return " ".repeat(padding) + this
  }
}
