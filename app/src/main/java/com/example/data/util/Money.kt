package com.example.data.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToLong

data class LineItemCalculation(
  val grossPaise: Long,
  val discountPaise: Long,
  val taxablePaise: Long,
  val taxRatePercent: Double,
  val taxPaise: Long,
  val lineTotalPaise: Long,
)

data class BillTotalsCalculation(
  val grossSubtotalPaise: Long,
  val itemDiscountsPaise: Long,
  val billDiscountPaise: Long,
  val totalDiscountPaise: Long,
  val taxableSubtotalPaise: Long,
  val totalTaxPaise: Long,
  val cgstPaise: Long,
  val sgstPaise: Long,
  val preRoundNetPaise: Long,
  val roundOffPaise: Long,
  val finalNetPaise: Long,
)

/**
 * Money utilities enforcing integer minor currency units (Paise)
 * to avoid floating-point rounding errors in retail billing and accounting.
 * 1 Rupee = 100 Paise.
 */
object Money {

  /**
   * Converts Rupee double to integer Paise.
   */
  fun toPaise(rupees: Double): Long {
    return (rupees * 100.0).roundToLong()
  }

  /**
   * Converts integer Paise to Rupee double.
   */
  fun toRupees(paise: Long): Double {
    return paise / 100.0
  }

  /**
   * Calculates line item details in paise:
   * Gross = round(quantity * unitPricePaise)
   * Taxable = max(0, Gross - discountPaise)
   * Tax = round(Taxable * taxRatePercent / 100.0)
   * Line Total = Taxable + Tax
   */
  fun calculateLineItem(
    quantity: Double,
    unitPricePaise: Long,
    discountPaise: Long = 0L,
    taxRatePercent: Double = 0.0,
  ): LineItemCalculation {
    val gross = (quantity * unitPricePaise).roundToLong()
    val cappedDiscount = discountPaise.coerceIn(0L, gross)
    val taxable = gross - cappedDiscount
    val tax = if (taxRatePercent > 0.0) {
      ((taxable * taxRatePercent) / 100.0).roundToLong()
    } else 0L
    val lineTotal = taxable + tax
    return LineItemCalculation(
      grossPaise = gross,
      discountPaise = cappedDiscount,
      taxablePaise = taxable,
      taxRatePercent = taxRatePercent,
      taxPaise = tax,
      lineTotalPaise = lineTotal,
    )
  }

  /**
   * Legacy simple calculator for backward compatibility.
   */
  fun calculateLineTotal(quantity: Double, unitPricePaise: Long, taxRatePercent: Double = 0.0): Long {
    return calculateLineItem(quantity, unitPricePaise, 0L, taxRatePercent).lineTotalPaise
  }

  /**
   * Standard Indian currency round-off to nearest rupee:
   * E.g.
   * 150.40 (15040 paise) -> roundOff = -40 paise, final = 15000 paise (₹150.00)
   * 150.50 (15050 paise) -> roundOff = +50 paise, final = 15100 paise (₹151.00)
   * 150.80 (15080 paise) -> roundOff = +20 paise, final = 15100 paise (₹151.00)
   */
  fun calculateRoundOff(amountPaise: Long): Long {
    val remainder = amountPaise % 100L
    return when {
      remainder == 0L -> 0L
      remainder >= 50L -> 100L - remainder
      remainder > 0L -> -remainder
      remainder <= -50L -> -100L - remainder
      else -> -remainder
    }
  }

  /**
   * Calculates overall bill totals with item and bill discounts, GST breakdown (CGST/SGST),
   * and optional round-off to nearest whole rupee.
   */
  fun calculateBillTotals(
    lineCalculations: List<LineItemCalculation>,
    billDiscountPaise: Long = 0L,
    applyRoundOff: Boolean = true,
  ): BillTotalsCalculation {
    val grossSubtotal = lineCalculations.sumOf { it.grossPaise }
    val itemDiscounts = lineCalculations.sumOf { it.discountPaise }
    val cappedBillDiscount = billDiscountPaise.coerceIn(0L, max(0L, grossSubtotal - itemDiscounts))
    val totalDiscount = itemDiscounts + cappedBillDiscount

    // If bill discount is applied, it reduces taxable subtotal proportionately or directly
    val taxableSubtotal = max(0L, grossSubtotal - totalDiscount)
    val totalTax = lineCalculations.sumOf { it.taxPaise }

    val cgst = totalTax / 2L
    val sgst = totalTax - cgst

    val preRoundNet = taxableSubtotal + totalTax
    val roundOff = if (applyRoundOff) calculateRoundOff(preRoundNet) else 0L
    val finalNet = preRoundNet + roundOff

    return BillTotalsCalculation(
      grossSubtotalPaise = grossSubtotal,
      itemDiscountsPaise = itemDiscounts,
      billDiscountPaise = cappedBillDiscount,
      totalDiscountPaise = totalDiscount,
      taxableSubtotalPaise = taxableSubtotal,
      totalTaxPaise = totalTax,
      cgstPaise = cgst,
      sgstPaise = sgst,
      preRoundNetPaise = preRoundNet,
      roundOffPaise = roundOff,
      finalNetPaise = finalNet,
    )
  }

  /**
   * Formats paise into Indian currency string: e.g. "₹ 1,25,000.00" or "₹ 850".
   */
  fun format(paise: Long, showDecimals: Boolean = true): String {
    val rupees = toRupees(paise)
    return try {
      val locale = Locale("en", "IN")
      val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = if (showDecimals && paise % 100 != 0L) 2 else 0
        maximumFractionDigits = if (showDecimals) 2 else 0
      }
      "₹ " + formatter.format(rupees)
    } catch (_: Exception) {
      if (showDecimals) String.format(Locale.ROOT, "₹ %.2f", rupees) else "₹ ${paise / 100}"
    }
  }
}
