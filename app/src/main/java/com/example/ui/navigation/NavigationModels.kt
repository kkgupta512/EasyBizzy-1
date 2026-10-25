package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * High-level user persona in EasyBizzy.
 * Role selection switches UI views and enforces that Customer mode can ONLY query
 * public marketplace endpoints, while Shopkeeper mode operates device-local encrypted data.
 */
enum class UserRole(val label: String) {
  SHOPKEEPER("Shopkeeper"),
  CUSTOMER("Customer"),
}

/**
 * All 10 Shopkeeper Navigation Destinations.
 */
sealed class ShopkeeperDestination(
  val route: String,
  val title: String,
  val icon: ImageVector,
  val isBottomNav: Boolean = false,
) {
  data object Dashboard : ShopkeeperDestination("sk_dashboard", "Dashboard", Icons.Default.Dashboard, true)
  data object NewBill : ShopkeeperDestination("sk_new_bill", "New Bill", Icons.Default.AddCircle, true)
  data object Bills : ShopkeeperDestination("sk_bills", "Bills", Icons.Default.PointOfSale, true)
  data object Khata : ShopkeeperDestination("sk_khata", "Khata", Icons.Default.ReceiptLong, true)
  data object Products : ShopkeeperDestination("sk_products", "Products", Icons.Default.Inventory2, false)
  data object Expenses : ShopkeeperDestination("sk_expenses", "Expenses", Icons.Outlined.AccountBalanceWallet, false)
  data object Reports : ShopkeeperDestination("sk_reports", "Reports", Icons.Outlined.Analytics, false)
  data object PublicShopProfile : ShopkeeperDestination("sk_profile", "Public Shop Profile", Icons.Default.Storefront, false)
  data object BackupRestore : ShopkeeperDestination("sk_backup", "Backup & Restore", Icons.Default.Security, false)
  data object Settings : ShopkeeperDestination("sk_settings", "Settings", Icons.Default.Settings, false)

  companion object {
    // Custom getter prevents circular static initialization nulls in companion object
    val all: List<ShopkeeperDestination>
      get() = listOf(
        Dashboard,
        NewBill,
        Bills,
        Products,
        Khata,
        Expenses,
        Reports,
        PublicShopProfile,
        BackupRestore,
        Settings,
      )

    val bottomNavItems: List<ShopkeeperDestination>
      get() = listOf(Dashboard, NewBill, Bills, Khata)
  }
}

/**
 * All 5 Customer Navigation Destinations.
 */
sealed class CustomerDestination(
  val route: String,
  val title: String,
  val icon: ImageVector,
) {
  data object Discover : CustomerDestination("cust_discover", "Discover", Icons.Default.Explore)
  data object Categories : CustomerDestination("cust_categories", "Categories", Icons.Default.Category)
  data object Search : CustomerDestination("cust_search", "Search", Icons.Default.Search)
  data object SavedShops : CustomerDestination("cust_saved", "Saved Shops", Icons.Default.Favorite)
  data object Account : CustomerDestination("cust_account", "Account", Icons.Default.AccountCircle)

  companion object {
    // Custom getter prevents circular static initialization nulls in companion object
    val all: List<CustomerDestination>
      get() = listOf(Discover, Categories, Search, SavedShops, Account)
  }
}
