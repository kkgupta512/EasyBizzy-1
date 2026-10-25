package com.example.ui.showcase

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EasyBizzyCard
import com.example.ui.components.EasyBizzyConfirmDialog
import com.example.ui.components.EasyBizzyDangerButton
import com.example.ui.components.EasyBizzyEmptyState
import com.example.ui.components.EasyBizzyErrorMessage
import com.example.ui.components.EasyBizzyLoading
import com.example.ui.components.EasyBizzyLogo
import com.example.ui.components.EasyBizzyOutlinedButton
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzySecondaryButton
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.MetricSummaryCard
import com.example.ui.components.RupeeColorStyle
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Foundation Showcase Screen rendering all design system primitives:
 * Typography, Colors, Spacing, Buttons, Metric Cards, Form inputs with validation,
 * Dialogs, Empty states, Loaders, and Error banners.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundationShowcaseScreen(
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var showConfirmDialog by remember { mutableStateOf(false) }
  var showSuccessFeedback by remember { mutableStateOf(false) }

  // Form Fields State
  var customerName by remember { mutableStateOf("Sharma Kirana Store") }
  var mobileNumber by remember { mutableStateOf("9876543210") }
  var billAmount by remember { mutableStateOf("850") }
  var isButtonLoading by remember { mutableStateOf(false) }

  // Validation States
  val mobileError = remember(mobileNumber) {
    if (mobileNumber.isNotEmpty() && (mobileNumber.length != 10 || !mobileNumber.all { it.isDigit() })) {
      "Enter a valid 10-digit Indian mobile number"
    } else null
  }

  val amountError = remember(billAmount) {
    if (billAmount.isNotEmpty() && billAmount.toDoubleOrNull() == null) {
      "Please enter a valid amount"
    } else null
  }

  // Previews Toggle State
  var previewStateView by remember { mutableStateOf("FORM") } // "FORM", "EMPTY", "LOADING", "ERROR"

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          EasyBizzyLogo(
            subtitleText = "डिजिटल बही-खाता • Counter Mode",
          )
        },
        actions = {
          StatusPill(
            text = "COUNTER ACTIVE",
            backgroundColor = EasyBizzyThemeTokens.colors.khataLenaContainer,
            contentColor = EasyBizzyThemeTokens.colors.khataLena,
            modifier = Modifier.padding(end = 8.dp),
          )
          IconButton(
            onClick = onToggleTheme,
            modifier = Modifier.testTag("theme_toggle_button"),
          ) {
            Icon(
              imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Toggle Theme",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
        ),
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
      ) {
        val navItems = listOf(
          Triple("Billing", Icons.Outlined.PointOfSale, 0),
          Triple("Khata", Icons.Default.ReceiptLong, 1),
          Triple("Inventory", Icons.Outlined.Inventory2, 2),
          Triple("Expenses", Icons.Outlined.AccountBalanceWallet, 3),
          Triple("Market", Icons.Outlined.Storefront, 4),
        )

        navItems.forEach { (label, icon, index) ->
          NavigationBarItem(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            icon = {
              Icon(
                imageVector = icon,
                contentDescription = label,
              )
            },
            label = {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                ),
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimary,
              indicatorColor = MaterialTheme.colorScheme.primary,
            ),
          )
        }
      }
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. Metric Summary Cards (Lena / Dena / Sales)
      Text(
        text = "Counter Summary",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        MetricSummaryCard(
          title = "You will get (Lena)",
          amount = 14850.0,
          colorStyle = RupeeColorStyle.LENA_POSITIVE,
          icon = Icons.Default.TrendingUp,
          subtitle = "18 Customers",
          modifier = Modifier.weight(1f),
          testTag = "metric_lena",
        )
        MetricSummaryCard(
          title = "You will give (Dena)",
          amount = 3200.0,
          colorStyle = RupeeColorStyle.DENA_NEGATIVE,
          icon = Icons.Default.TrendingDown,
          subtitle = "4 Suppliers",
          modifier = Modifier.weight(1f),
          testTag = "metric_dena",
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        MetricSummaryCard(
          title = "Today's Sales",
          amount = 8450.0,
          colorStyle = RupeeColorStyle.NEUTRAL,
          icon = Icons.Default.ShoppingBag,
          subtitle = "24 Bills created",
          modifier = Modifier.weight(1f),
          testTag = "metric_sales",
        )
        MetricSummaryCard(
          title = "Cash in Drawer",
          amount = 5120.0,
          colorStyle = RupeeColorStyle.CASH_ACCENT,
          icon = Icons.Default.Payments,
          subtitle = "Reconciled at 10 AM",
          modifier = Modifier.weight(1f),
          testTag = "metric_cash",
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // 2. State Switcher Row (View Form, Empty, Loading, Error)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Component Sandbox",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
          listOf("FORM", "EMPTY", "LOADING", "ERROR").forEach { stateKey ->
            Surface(
              onClick = { previewStateView = stateKey },
              shape = MaterialTheme.shapes.small,
              color = if (previewStateView == stateKey) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceVariant
              },
              modifier = Modifier.testTag("filter_$stateKey"),
            ) {
              Text(
                text = stateKey,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (previewStateView == stateKey) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }
        }
      }

      // Conditional State Rendering
      when (previewStateView) {
        "EMPTY" -> {
          EasyBizzyEmptyState(
            title = "No Bills Recorded Today",
            description = "Start billing customers or add ledger entries. Bills work completely offline and sync securely.",
            actionButtonText = "Create First Bill",
            onActionClick = { previewStateView = "FORM" },
          )
        }
        "LOADING" -> {
          EasyBizzyLoading(
            message = "Encrypting and verifying local database records...",
          )
        }
        "ERROR" -> {
          EasyBizzyErrorMessage(
            title = "Database Sync Paused",
            message = "Local encrypted storage is active. Drive cloud backup is waiting for network connection.",
            onRetry = { previewStateView = "FORM" },
          )
        }
        else -> {
          // 3. Form Fields & Validation Demo
          EasyBizzyCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "quick_entry_card",
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                text = "Quick Counter Entry (POS / Khata)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
              )

              EasyBizzyTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = "Customer / Party Name",
                placeholder = "e.g., Rajesh Kumar",
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                  )
                },
                testTag = "customer_name_input",
              )

              EasyBizzyTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = "Customer Phone",
                prefix = "+91 ",
                placeholder = "10-digit number",
                errorMessage = mobileError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                testTag = "customer_phone_input",
              )

              EasyBizzyTextField(
                value = billAmount,
                onValueChange = { billAmount = it },
                label = "Bill / Khata Amount",
                prefix = "₹ ",
                placeholder = "0.00",
                errorMessage = amountError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                testTag = "bill_amount_input",
              )

              // Quick Amount Add Chips
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                listOf(50, 100, 200, 500, 1000).forEach { chipAmount ->
                  Surface(
                    onClick = {
                      val current = billAmount.toDoubleOrNull() ?: 0.0
                      billAmount = (current + chipAmount).toInt().toString()
                    },
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                  ) {
                    Text(
                      text = "+ ₹$chipAmount",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              // Action Buttons Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                EasyBizzyPrimaryButton(
                  text = "Save Bill",
                  onClick = {
                    isButtonLoading = true
                    showSuccessFeedback = true
                  },
                  isLoading = isButtonLoading,
                  icon = {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                  },
                  modifier = Modifier.weight(1f),
                  testTag = "save_bill_button",
                )

                EasyBizzySecondaryButton(
                  text = "Add Udhar",
                  onClick = { showConfirmDialog = true },
                  icon = {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                  },
                  modifier = Modifier.weight(1f),
                  testTag = "add_udhar_button",
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                EasyBizzyOutlinedButton(
                  text = "WhatsApp Bill",
                  onClick = { /* Formatted intent preview */ },
                  icon = {
                    Icon(
                      imageVector = Icons.Default.Share,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                  },
                  modifier = Modifier.weight(1f),
                  testTag = "whatsapp_bill_button",
                )

                EasyBizzyDangerButton(
                  text = "Clear All",
                  onClick = { showConfirmDialog = true },
                  modifier = Modifier.weight(1f),
                  testTag = "clear_form_button",
                )
              }
            }
          }
        }
      }

      if (showSuccessFeedback) {
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = EasyBizzyThemeTokens.colors.khataLenaContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = EasyBizzyThemeTokens.colors.khataLena,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Bill of ₹$billAmount saved offline successfully!",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = EasyBizzyThemeTokens.colors.khataLena,
              ),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Confirmation Dialog
  if (showConfirmDialog) {
    EasyBizzyConfirmDialog(
      title = "Clear Current Bill?",
      message = "Are you sure you want to clear current items from the counter?",
      confirmButtonText = "Yes, Clear",
      dismissButtonText = "Keep Editing",
      isDestructive = true,
      icon = Icons.Default.Delete,
      onConfirm = {
        customerName = ""
        mobileNumber = ""
        billAmount = ""
        isButtonLoading = false
        showSuccessFeedback = false
        showConfirmDialog = false
      },
      onDismiss = {
        showConfirmDialog = false
      },
      testTag = "clear_confirm_dialog",
    )
  }
}
