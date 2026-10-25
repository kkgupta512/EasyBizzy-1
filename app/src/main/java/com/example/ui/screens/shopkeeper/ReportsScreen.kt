package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.MetricSummaryCard
import com.example.ui.components.RupeeColorStyle

/**
 * Screen providing business performance reports, P&L estimates, and GST summaries.
 */
@Composable
fun ReportsScreen(
  onExportReportClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedRange by remember { mutableStateOf("TODAY") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("reports_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text(
      text = "Reports & Analytics",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      listOf("TODAY" to "Today", "7DAYS" to "Last 7 Days", "MONTH" to "This Month").forEach { (code, label) ->
        FilterChip(
          selected = selectedRange == code,
          onClick = { selectedRange = code },
          label = { Text(label) },
          modifier = Modifier.testTag("filter_report_$code"),
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MetricSummaryCard(
        title = "Gross Revenue",
        amount = 0.0,
        colorStyle = RupeeColorStyle.NEUTRAL,
        subtitle = "0 Transactions",
        modifier = Modifier.weight(1f),
      )
      MetricSummaryCard(
        title = "Est. Gross Profit",
        amount = 0.0,
        colorStyle = RupeeColorStyle.LENA_POSITIVE,
        subtitle = "Based on wholesale cost",
        modifier = Modifier.weight(1f),
      )
    }

    EasyBizzyEmptyState(
      title = "No Report Data Available",
      description = "Detailed graphs, customer collection trends, and profit margins will calculate automatically as bills and expenses are recorded.",
      icon = Icons.Outlined.Analytics,
      actionButtonText = "Download Sample Template",
      onActionClick = onExportReportClick,
      testTag = "empty_state_reports",
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
