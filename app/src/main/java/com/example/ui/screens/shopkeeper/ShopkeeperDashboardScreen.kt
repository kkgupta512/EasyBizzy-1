package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.util.Money
import com.example.ui.components.EasyBizzyCard
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.MetricSummaryCard
import com.example.ui.components.RupeeColorStyle
import com.example.ui.components.RupeeText
import com.example.ui.components.StatusPill
import com.example.ui.navigation.ShopkeeperDestination
import com.example.ui.theme.EasyBizzyThemeTokens
import com.example.ui.viewmodel.DashboardUiState
import com.example.ui.viewmodel.ShopkeeperDashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shopkeeper Dashboard displaying sales today, bills count, receivables (Lena),
 * payables (Dena), and counter quick actions.
 * Backed by Room database through ShopkeeperDashboardViewModel.
 */
@Composable
fun ShopkeeperDashboardScreen(
  onNavigateTo: (ShopkeeperDestination) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ShopkeeperDashboardViewModel? = null,
) {
  val uiState by viewModel?.uiState?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.remember {
    androidx.compose.runtime.mutableStateOf(DashboardUiState())
  }

  val salesToday = Money.toRupees(uiState.salesTodayPaise)
  val billsTodayCount = uiState.billsTodayCount
  val outstandingReceivables = Money.toRupees(uiState.receivablesLenaPaise)
  val outstandingPayables = Money.toRupees(uiState.payablesDenaPaise)

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("shopkeeper_dashboard_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Header & Counter Status
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = "Shopkeeper Hub",
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Today's Ledger & Counter Overview",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.outline,
        )
      }

      StatusPill(
        text = "OFFLINE ENCRYPTED",
        backgroundColor = EasyBizzyThemeTokens.colors.khataLenaContainer,
        contentColor = EasyBizzyThemeTokens.colors.khataLena,
      )
    }

    // 2. High-Visibility Financial Metrics (Lena / Dena / Sales / Bills)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MetricSummaryCard(
        title = "Sales Today",
        amount = salesToday,
        colorStyle = RupeeColorStyle.NEUTRAL,
        icon = Icons.Default.ShoppingBag,
        subtitle = if (billsTodayCount > 0) "$billsTodayCount bills recorded" else "No bills recorded yet",
        modifier = Modifier.weight(1f),
        onClick = { onNavigateTo(ShopkeeperDestination.Bills) },
        testTag = "metric_sales_today",
      )

      Card(
        modifier = Modifier
          .weight(1f)
          .testTag("metric_bills_today"),
        shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Bills Today",
              style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
              ),
            )
            Icon(
              imageVector = Icons.Default.PointOfSale,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp),
            )
          }

          Text(
            text = "$billsTodayCount",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            ),
          )

          Text(
            text = if (billsTodayCount > 0) "Active counter session" else "No bills issued yet",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.outline,
            ),
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MetricSummaryCard(
        title = "Receivables (Lena)",
        amount = outstandingReceivables,
        colorStyle = RupeeColorStyle.LENA_POSITIVE,
        icon = Icons.Default.TrendingUp,
        subtitle = if (outstandingReceivables > 0) "Customer credit dues" else "0 customer dues",
        modifier = Modifier.weight(1f),
        onClick = { onNavigateTo(ShopkeeperDestination.Khata) },
        testTag = "metric_receivables",
      )

      MetricSummaryCard(
        title = "Payables (Dena)",
        amount = outstandingPayables,
        colorStyle = RupeeColorStyle.DENA_NEGATIVE,
        icon = Icons.Default.TrendingDown,
        subtitle = if (outstandingPayables > 0) "Supplier liabilities" else "0 supplier dues",
        modifier = Modifier.weight(1f),
        onClick = { onNavigateTo(ShopkeeperDestination.Khata) },
        testTag = "metric_payables",
      )
    }

    // 3. Quick Actions Grid
    Text(
      text = "Quick Counter Actions",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface,
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      DashboardActionTile(
        title = "New Bill",
        subtitle = "Fast POS",
        icon = Icons.Default.Add,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        onClick = { onNavigateTo(ShopkeeperDestination.NewBill) },
        testTag = "action_new_bill",
      )

      DashboardActionTile(
        title = "Add Khata",
        subtitle = "Udhar Entry",
        icon = Icons.Default.ReceiptLong,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick = { onNavigateTo(ShopkeeperDestination.Khata) },
        testTag = "action_add_khata",
      )

      DashboardActionTile(
        title = "Expense",
        subtitle = "Shop Costs",
        icon = Icons.Outlined.AccountBalanceWallet,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { onNavigateTo(ShopkeeperDestination.Expenses) },
        testTag = "action_add_expense",
      )

      DashboardActionTile(
        title = "Products",
        subtitle = "Stock & Items",
        icon = Icons.Default.Inventory2,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { onNavigateTo(ShopkeeperDestination.Products) },
        testTag = "action_products",
      )

      DashboardActionTile(
        title = "Marketplace",
        subtitle = "Public Profile",
        icon = Icons.Default.Storefront,
        containerColor = EasyBizzyThemeTokens.colors.cashAccentContainer.copy(alpha = 0.7f),
        contentColor = EasyBizzyThemeTokens.colors.cashAccent,
        onClick = { onNavigateTo(ShopkeeperDestination.PublicShopProfile) },
        testTag = "action_marketplace",
      )
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    // 4. Real Data or Verified Empty State
    EasyBizzyCard(
      modifier = Modifier.fillMaxWidth(),
      testTag = "dashboard_recent_card",
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Today's Transactions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
          )

          Text(
            text = "Room Encrypted",
            style = MaterialTheme.typography.labelSmall.copy(
              color = EasyBizzyThemeTokens.colors.khataLena,
              fontWeight = FontWeight.Bold,
            ),
          )
        }

        if (uiState.recentBills.isEmpty()) {
          // Labeled zero-state
          EasyBizzyEmptyState(
            title = "No Bills Recorded Today",
            description = "All sales and Khata entries made here will be stored in your encrypted local database.",
            icon = Icons.Default.ShoppingBag,
            actionButtonText = "+ Create New Bill",
            onActionClick = { onNavigateTo(ShopkeeperDestination.NewBill) },
            testTag = "dashboard_empty_bills",
          )
        } else {
          // Real persisted bills displayed with local timezone timestamp
          val localDateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
          uiState.recentBills.forEach { bill ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .padding(12.dp)
                  .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column {
                  Text(
                    text = bill.billNumber,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  )
                  Text(
                    text = "${bill.customerName ?: "Cash Customer"} • ${localDateFormat.format(Date(bill.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  RupeeText(
                    amount = Money.toRupees(bill.netAmountPaise),
                    style = MaterialTheme.typography.titleMedium,
                    colorStyle = RupeeColorStyle.NEUTRAL,
                  )
                  StatusPill(
                    text = bill.paymentMode,
                    backgroundColor = if (bill.paymentMode == "KHATA") {
                      EasyBizzyThemeTokens.colors.khataDenaContainer
                    } else {
                      EasyBizzyThemeTokens.colors.khataLenaContainer
                    },
                    contentColor = if (bill.paymentMode == "KHATA") {
                      EasyBizzyThemeTokens.colors.khataDena
                    } else {
                      EasyBizzyThemeTokens.colors.khataLena
                    },
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun DashboardActionTile(
  title: String,
  subtitle: String,
  icon: ImageVector,
  containerColor: androidx.compose.ui.graphics.Color,
  contentColor: androidx.compose.ui.graphics.Color,
  onClick: () -> Unit,
  testTag: String,
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(14.dp),
    color = containerColor,
    modifier = Modifier
      .width(135.dp)
      .height(86.dp)
      .testTag(testTag),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(24.dp),
      )
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = contentColor,
          ),
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 11.sp,
            color = contentColor.copy(alpha = 0.8f),
          ),
        )
      }
    }
  }
}
