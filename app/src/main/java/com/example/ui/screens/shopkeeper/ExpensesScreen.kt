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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.MetricSummaryCard
import com.example.ui.components.RupeeColorStyle

/**
 * Screen for tracking daily business operational expenses and cash outflows.
 */
@Composable
fun ExpensesScreen(
  onAddExpenseClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val totalExpensesToday = 0.0

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("expenses_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text(
      text = "Shop Expenses",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    MetricSummaryCard(
      title = "Today's Total Expenses",
      amount = totalExpensesToday,
      colorStyle = RupeeColorStyle.CASH_ACCENT,
      icon = Icons.Outlined.AccountBalanceWallet,
      subtitle = "Rent, Staff, Tea, Electricity & Logistics",
      modifier = Modifier.fillMaxWidth(),
      testTag = "metric_total_expenses",
    )

    EasyBizzyEmptyState(
      title = "No Expenses Logged Today",
      description = "Log small cash expenses like tea/snacks, shop maintenance, staff wages, and electricity to keep your cash drawer accurate.",
      icon = Icons.Outlined.AccountBalanceWallet,
      actionButtonText = "+ Add Expense",
      onActionClick = onAddExpenseClick,
      testTag = "empty_state_expenses",
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
