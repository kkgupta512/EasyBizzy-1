package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.EasyBizzyDatabase
import com.example.data.repository.BillingRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.KhataRepository
import com.example.data.repository.ShopSettingsRepository
import com.example.ui.components.EasyBizzyLogo
import com.example.ui.components.StatusPill
import com.example.ui.screens.customer.CustomerAccountScreen
import com.example.ui.screens.customer.CustomerCategoriesScreen
import com.example.ui.screens.customer.CustomerDiscoverScreen
import com.example.ui.screens.customer.CustomerSavedShopsScreen
import com.example.ui.screens.customer.CustomerSearchScreen
import com.example.ui.screens.shopkeeper.BackupRestoreScreen
import com.example.ui.screens.shopkeeper.BillsListScreen
import com.example.ui.screens.shopkeeper.ExpensesScreen
import com.example.ui.screens.shopkeeper.KhataScreen
import com.example.ui.screens.shopkeeper.NewBillScreen
import com.example.ui.screens.shopkeeper.ProductsScreen
import com.example.ui.screens.shopkeeper.PublicShopProfileScreen
import com.example.ui.screens.shopkeeper.ReportsScreen
import com.example.ui.screens.shopkeeper.SettingsScreen
import com.example.ui.screens.shopkeeper.ShopkeeperDashboardScreen
import com.example.ui.theme.EasyBizzyThemeTokens
import com.example.ui.viewmodel.BillingViewModel
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.KhataViewModel
import com.example.ui.viewmodel.ShopkeeperDashboardViewModel
import kotlinx.coroutines.launch

/**
 * Adaptive Application Scaffold supporting Shopkeeper and Customer experiences.
 * Connects the Room database, Repositories, and ViewModels to screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyBizzyAppScaffold(
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val database = remember { EasyBizzyDatabase.getInstance(context) }
  val billingRepo = remember {
    BillingRepository(
      database.billDao(),
      database.customerDao(),
      database.ledgerDao(),
      database.paymentDao(),
      database.productDao(),
      database.billDraftDao(),
      database.shopSettingsDao(),
    )
  }
  val khataRepo = remember {
    KhataRepository(
      database.customerDao(),
      database.supplierDao(),
      database.ledgerDao(),
      database.paymentDao(),
    )
  }
  val expenseRepo = remember { ExpenseRepository(database.expenseDao()) }
  val inventoryRepo = remember { InventoryRepository(database.productDao()) }
  val settingsRepo = remember {
    ShopSettingsRepository(
      database.shopSettingsDao(),
      database.publishingPreferencesDao(),
    )
  }

  val dashboardViewModel: ShopkeeperDashboardViewModel = viewModel(
    factory = ShopkeeperDashboardViewModel.Factory(billingRepo, khataRepo, expenseRepo)
  )
  val billingViewModel: BillingViewModel = viewModel(
    factory = BillingViewModel.Factory(billingRepo, inventoryRepo, khataRepo)
  )
  val khataViewModel: KhataViewModel = viewModel(
    factory = KhataViewModel.Factory(khataRepo)
  )
  val inventoryViewModel: InventoryViewModel = viewModel(
    factory = InventoryViewModel.Factory(inventoryRepo)
  )

  val allBills by billingRepo.allBills.collectAsStateWithLifecycle(initialValue = emptyList())

  var currentRole by remember { mutableStateOf(UserRole.SHOPKEEPER) }
  var currentShopkeeperDest by remember { mutableStateOf<ShopkeeperDestination>(ShopkeeperDestination.Dashboard) }
  var currentCustomerDest by remember { mutableStateOf<CustomerDestination>(CustomerDestination.Discover) }

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  // Handle Android system back gestures
  BackHandler(enabled = currentRole == UserRole.SHOPKEEPER && currentShopkeeperDest != ShopkeeperDestination.Dashboard) {
    currentShopkeeperDest = ShopkeeperDestination.Dashboard
  }

  BackHandler(enabled = currentRole == UserRole.CUSTOMER && currentCustomerDest != CustomerDestination.Discover) {
    currentCustomerDest = CustomerDestination.Discover
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier
          .width(300.dp)
          .testTag("app_navigation_drawer"),
      ) {
        Column(
          modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Drawer Header
          EasyBizzyLogo(
            subtitleText = if (currentRole == UserRole.SHOPKEEPER) "Shopkeeper Operating System" else "Hyperlocal Marketplace",
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Role Switcher Tile in Drawer
          Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
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
                  text = "Active Experience",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                  text = currentRole.label,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }

              IconButton(
                onClick = {
                  currentRole = if (currentRole == UserRole.SHOPKEEPER) UserRole.CUSTOMER else UserRole.SHOPKEEPER
                  scope.launch { drawerState.close() }
                },
                modifier = Modifier.testTag("drawer_switch_role_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.SwapHoriz,
                  contentDescription = "Switch Mode",
                  tint = MaterialTheme.colorScheme.primary,
                )
              }
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

          // Destinations List based on active role
          if (currentRole == UserRole.SHOPKEEPER) {
            Text(
              text = "Shopkeeper Navigation (10)",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
              ),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )

            ShopkeeperDestination.all.forEach { dest ->
              NavigationDrawerItem(
                label = { Text(dest.title, fontWeight = FontWeight.Medium) },
                icon = { Icon(dest.icon, contentDescription = null) },
                selected = currentShopkeeperDest == dest,
                onClick = {
                  currentShopkeeperDest = dest
                  scope.launch { drawerState.close() }
                },
                colors = NavigationDrawerItemDefaults.colors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.testTag("drawer_item_${dest.route}"),
              )
            }
          } else {
            Text(
              text = "Customer Navigation (5)",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
              ),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )

            CustomerDestination.all.forEach { dest ->
              NavigationDrawerItem(
                label = { Text(dest.title, fontWeight = FontWeight.Medium) },
                icon = { Icon(dest.icon, contentDescription = null) },
                selected = currentCustomerDest == dest,
                onClick = {
                  currentCustomerDest = dest
                  scope.launch { drawerState.close() }
                },
                colors = NavigationDrawerItemDefaults.colors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.testTag("drawer_item_${dest.route}"),
              )
            }
          }
        }
      }
    },
  ) {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
          title = {
            val titleText = if (currentRole == UserRole.SHOPKEEPER) {
              currentShopkeeperDest.title
            } else {
              currentCustomerDest.title
            }

            Column {
              Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              )
              Text(
                text = "EasyBizzy • ${currentRole.label} Mode",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.outline,
                ),
              )
            }
          },
          navigationIcon = {
            val isRoot = if (currentRole == UserRole.SHOPKEEPER) {
              currentShopkeeperDest == ShopkeeperDestination.Dashboard
            } else {
              currentCustomerDest == CustomerDestination.Discover
            }

            if (!isRoot) {
              IconButton(
                onClick = {
                  if (currentRole == UserRole.SHOPKEEPER) {
                    currentShopkeeperDest = ShopkeeperDestination.Dashboard
                  } else {
                    currentCustomerDest = CustomerDestination.Discover
                  }
                },
                modifier = Modifier.testTag("top_bar_back_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                )
              }
            } else {
              IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.testTag("top_bar_drawer_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Menu,
                  contentDescription = "Open Navigation Menu",
                )
              }
            }
          },
          actions = {
            // Quick Role Switch Button
            Surface(
              onClick = {
                currentRole = if (currentRole == UserRole.SHOPKEEPER) UserRole.CUSTOMER else UserRole.SHOPKEEPER
              },
              shape = CircleShape,
              color = if (currentRole == UserRole.SHOPKEEPER) {
                EasyBizzyThemeTokens.colors.khataLenaContainer
              } else {
                MaterialTheme.colorScheme.secondaryContainer
              },
              modifier = Modifier.testTag("role_switch_pill"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = if (currentRole == UserRole.SHOPKEEPER) Icons.Default.Store else Icons.Default.Person,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = if (currentRole == UserRole.SHOPKEEPER) {
                    EasyBizzyThemeTokens.colors.khataLena
                  } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                  },
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = currentRole.label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (currentRole == UserRole.SHOPKEEPER) {
                      EasyBizzyThemeTokens.colors.khataLena
                    } else {
                      MaterialTheme.colorScheme.onSecondaryContainer
                    },
                  ),
                )
              }
            }

            // Dark/Light Theme Switcher
            IconButton(
              onClick = onToggleTheme,
              modifier = Modifier.testTag("top_bar_theme_toggle"),
            ) {
              Icon(
                imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
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
          modifier = Modifier.testTag("app_bottom_bar"),
        ) {
          if (currentRole == UserRole.SHOPKEEPER) {
            ShopkeeperDestination.bottomNavItems.forEach { dest ->
              NavigationBarItem(
                selected = currentShopkeeperDest == dest,
                onClick = { currentShopkeeperDest = dest },
                icon = { Icon(dest.icon, contentDescription = dest.title) },
                label = { Text(dest.title, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                  indicatorColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.testTag("bottom_nav_${dest.route}"),
              )
            }

            // More item to open Drawer for Products, Expenses, Reports, Profile, Backup, Settings
            NavigationBarItem(
              selected = currentShopkeeperDest !in ShopkeeperDestination.bottomNavItems,
              onClick = { scope.launch { drawerState.open() } },
              icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
              label = { Text("More", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                indicatorColor = MaterialTheme.colorScheme.primary,
              ),
              modifier = Modifier.testTag("bottom_nav_more"),
            )
          } else {
            CustomerDestination.all.forEach { dest ->
              NavigationBarItem(
                selected = currentCustomerDest == dest,
                onClick = { currentCustomerDest = dest },
                icon = { Icon(dest.icon, contentDescription = dest.title) },
                label = { Text(dest.title, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                  indicatorColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.testTag("bottom_nav_${dest.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Box(modifier = Modifier.padding(innerPadding)) {
        if (currentRole == UserRole.SHOPKEEPER) {
          when (currentShopkeeperDest) {
            ShopkeeperDestination.Dashboard -> ShopkeeperDashboardScreen(
              onNavigateTo = { currentShopkeeperDest = it },
              viewModel = dashboardViewModel,
            )
            ShopkeeperDestination.NewBill -> NewBillScreen(
              onBillSaved = { currentShopkeeperDest = ShopkeeperDestination.Bills },
              viewModel = billingViewModel,
            )
            ShopkeeperDestination.Bills -> BillsListScreen(
              onNewBillClick = { currentShopkeeperDest = ShopkeeperDestination.NewBill },
              bills = allBills,
              billingRepository = billingRepo,
            )
            ShopkeeperDestination.Products -> ProductsScreen(
              onAddProductClick = { /* Handled in screen dialog */ },
              viewModel = inventoryViewModel,
            )
            ShopkeeperDestination.Khata -> KhataScreen(
              onAddPartyClick = { /* Add customer or supplier */ },
            )
            ShopkeeperDestination.Expenses -> ExpensesScreen(
              onAddExpenseClick = { /* Add expense modal */ },
            )
            ShopkeeperDestination.Reports -> ReportsScreen(
              onExportReportClick = { /* Export summary */ },
            )
            ShopkeeperDestination.PublicShopProfile -> PublicShopProfileScreen(
              onSaveProfile = { currentShopkeeperDest = ShopkeeperDestination.Dashboard },
            )
            ShopkeeperDestination.BackupRestore -> BackupRestoreScreen(
              onExportBackup = { /* Export */ },
              onRestoreBackup = { /* Restore */ },
            )
            ShopkeeperDestination.Settings -> SettingsScreen(
              onSaveSettings = { currentShopkeeperDest = ShopkeeperDestination.Dashboard },
            )
          }
        } else {
          when (currentCustomerDest) {
            CustomerDestination.Discover -> CustomerDiscoverScreen(
              onShopClick = { /* Open storefront */ },
              onSearchClick = { currentCustomerDest = CustomerDestination.Search },
            )
            CustomerDestination.Categories -> CustomerCategoriesScreen(
              onCategorySelected = { currentCustomerDest = CustomerDestination.Search },
            )
            CustomerDestination.Search -> CustomerSearchScreen(
              onSelectProduct = { /* Select */ },
            )
            CustomerDestination.SavedShops -> CustomerSavedShopsScreen(
              onExploreShops = { currentCustomerDest = CustomerDestination.Discover },
            )
            CustomerDestination.Account -> CustomerAccountScreen(
              onSwitchToShopkeeper = {
                currentRole = UserRole.SHOPKEEPER
                currentShopkeeperDest = ShopkeeperDestination.Dashboard
              },
            )
          }
        }
      }
    }
  }
}
