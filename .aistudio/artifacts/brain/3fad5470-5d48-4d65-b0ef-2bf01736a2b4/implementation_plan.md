# EasyBizzy: Implementation Plan & Architecture Specification

An offline-first native Android business suite built for Indian small-and-medium retail merchants (Kirana, medical, hardware, apparel, general stores) paired with a public local marketplace for consumers. EasyBizzy provides zero-latency billing, digital Khata (customer & supplier credit ledgers), real-time inventory management, expense tracking, and encrypted backup, while enabling merchants to selectively broadcast catalog items to a Spring Boot-backed local marketplace.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Strict Privacy & Data Separation**:
> Private business records (customer names, customer phone numbers, credit/debit balances, profit margins, supplier purchase costs, invoice histories, and expense records) **never** leave the local device unencrypted. They are strictly isolated in a SQLCipher-encrypted SQLite database protected by the Android KeyStore. Only items explicitly flagged by the merchant (`is_published_to_marketplace = true`) along with public shop profile metadata are synchronized with the external Spring Boot marketplace API.

### Key Architectural Choices:
- **Local Persistence & Cryptography**: SQLCipher for Room with AES-256 GCM key derivation stored securely in the hardware-backed `AndroidKeyStore`.
- **Marketplace Backend Decoupling**: Pure Retrofit/OkHttp REST client targeting the independent Spring Boot marketplace backend. When backend endpoints are unavailable, the app displays clear network/offline status without fabricated or fake responses.
- **Authentication Strategy**: Local merchant profile with device-bound PIN/Biometric lock for daily POS speed. Optional Firebase Authentication or Google Sign-In via Credential Manager is activated solely when syncing encrypted cloud backups to Google Drive or claiming verified marketplace shop ownership.
- **Dual-Persona Architecture**: Seamless single-app toggle between **Shopkeeper Mode** (secure operational hub) and **Customer Mode** (nearby shop discovery, price comparison, direct call/WhatsApp engagement).

---

## 1. Overview & Core Concept

EasyBizzy bridges the digital divide for Bharat’s local merchants:
1. **Shopkeeper Flow (Offline-First)**: Rapid point-of-sale billing under 5 seconds, digital Udhar Bahi-Khata with automated WhatsApp payment reminder links (UPI deeplinks), stock management with low-stock warnings, expense categorization, and P&L analytics.
2. **Customer Flow (Local Commerce)**: Hyperlocal discovery where neighborhood shoppers can search staple groceries or medicines, compare prices across local stores within 1–5 km, verify store operating hours, and connect directly via WhatsApp or phone call without middleman commission fees.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Aesthetic Direction**: High-efficiency, utilitarian Material 3 design optimized for high-contrast indoor/outdoor shop environments with large tap targets (minimum 48dp).
- **Color Palette**:
  - Primary: Deep Teal / Merchant Green (`#006D5B`, `#00A884` accent) symbolizing trust and positive ledger balance (*Jama / Lena*).
  - Khata Alert / Debit: Coral Red (`#BA1A1A`) representing outstanding credit given (*Udhar / Dena*).
  - Surface & Background: Clean neutral light tones (`#FBFDF9`) with M3 tonal elevation cards (`tonalElevation = 2.dp`).
  - Dark Theme: High-contrast slate background (`#101413`) reducing battery drain on OLED screens during long store hours.
- **Typography**: Clean, readable sans-serif typography (Rupee symbol `₹` consistently styled, monospace tabular figures for ledger and bill calculations).

### Key User Flows
```
┌────────────────────────────────────────────────────────────────────────┐
│                        EASYBIZZY DUAL PORTAL                          │
├──────────────────────────────────┬─────────────────────────────────────┤
│      SHOPKEEPER PORTAL           │          CUSTOMER PORTAL            │
├──────────────────────────────────┼─────────────────────────────────────┤
│ 1. Quick Billing (POS)           │ 1. Hyperlocal Shop Discovery        │
│    - Scan barcode or pick items  │    - Location/Pincode filter        │
│    - Split cash / UPI / Khata    │    - Category tags (Kirana, Pharma) │
│    - Generate PDF/WhatsApp bill  │                                     │
│                                  │ 2. Storefront & Catalog             │
│ 2. Khata Management              │    - Browse verified inventory      │
│    - Customer / Supplier ledger  │    - Check live prices & discounts  │
│    - Instant UPI payment links   │    - Tap to Call / WhatsApp shop    │
│                                  │                                     │
│ 3. Inventory & Marketplace Sync  │ 3. Local Price Comparison           │
│    - Stock in / out tracking     │    - Search staple commodities      │
│    - One-tap "Publish to Market" │    - Compare prices across nearby   │
│                                  │      neighborhood stores            │
│ 4. Encrypted Backup & Security   │                                     │
│    - KeyStore-locked database    │                                     │
│    - Google Drive AES-256 export │                                     │
└──────────────────────────────────┴─────────────────────────────────────┘
```

---

## 3. Security Boundaries & Data Isolation

```
┌────────────────────────────────────────────────────────────────────────┐
│                     DEVICE HARDWARE-SECURED ZONE                       │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    Android KeyStore (Master Key)                 │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
│                                     ▼                                  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │     SQLCipher Encrypted SQLite Database (Room: easybizzy.db)      │  │
│  │  - Customer/Supplier Khata entries & phone numbers               │  │
│  │  - Billing transactions, items sold & invoices                   │  │
│  │  - Wholesale cost prices, profit margins & expenses              │  │
│  │  - Daily cash register & financial reports                       │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────┘
                                      │ STRICT AIRGAP
                                      │ (Only published catalog items)
                                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    PUBLIC NETWORK BOUNDARY (HTTPS)                     │
│                                                                        │
│  ┌─────────────────────────┐               ┌────────────────────────┐  │
│  │   Merchant App Engine   │               │   Customer App Engine  │  │
│  └────────────┬────────────┘               └───────────▲────────────┘  │
│               │ (Publish Catalog / Profile)            │ (Search/Browse)│
│               ▼                                        │                │
│  ┌─────────────────────────────────────────────────────┴────────────┐  │
│  │           Independent Spring Boot Marketplace Backend            │  │
│  │  - Public store profile (Name, address, geo-coords, UPI ID)      │  │
│  │  - Published items only (Product name, selling price, unit, MRP) │  │
│  │  - Zero access to customer records, sales volume, or expenses    │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Technical Architecture & Package Structure

### System Architecture
The application adheres to clean **MVVM (Model-View-ViewModel)** architecture:
- **Presentation Layer**: Jetpack Compose UI with unidirectional data flow (`StateFlow`), M3 Scaffold, and Compose Navigation with type-safe arguments.
- **Domain Layer**: Clean business models, ledger calculation use-cases, and validation rules.
- **Data Layer**:
  - `Local`: Room DAOs with SQLCipher `SupportFactory`, `KeyStoreHelper`, and TypeConverters.
  - `Remote`: Retrofit with Moshi for the Spring Boot Marketplace API.
  - `Backup`: Google Drive AppData folder backup service with payload encryption.

### Package Layout
```
com.example.easybizzy/
├── EasyBizzyApplication.kt          // App initialization & SQLCipher setup
├── data/
│   ├── local/
│   │   ├── EasyBizzyDatabase.kt     // Encrypted Room database definition
│   │   ├── dao/
│   │   │   ├── ProductDao.kt
│   │   │   ├── KhataPartyDao.kt
│   │   │   ├── KhataTransactionDao.kt
│   │   │   ├── InvoiceDao.kt
│   │   │   ├── ExpenseDao.kt
│   │   │   └── ShopProfileDao.kt
│   │   ├── entity/
│   │   │   ├── ProductEntity.kt
│   │   │   ├── KhataPartyEntity.kt
│   │   │   ├── KhataTransactionEntity.kt
│   │   │   ├── InvoiceEntity.kt
│   │   │   ├── InvoiceItemEntity.kt
│   │   │   ├── ExpenseEntity.kt
│   │   │   └── ShopProfileEntity.kt
│   │   └── security/
│   │       ├── KeyStoreManager.kt   // AES-256 key generation in Android KeyStore
│   │       └── DatabasePassphraseProvider.kt
│   ├── remote/
│   │   ├── MarketplaceApiService.kt // Retrofit interface for Spring Boot API
│   │   ├── MarketplaceClient.kt     // OkHttp client with timeout/retry logic
│   │   └── dto/
│   │       ├── ShopDto.kt
│   │       ├── MarketplaceProductDto.kt
│   │       └── SearchResponseDto.kt
│   ├── backup/
│   │   ├── BackupManager.kt         // Export/Import encrypted SQLite snapshots
│   │   └── GoogleDriveService.kt    // Drive AppData integration
│   └── repository/
│       ├── BillingRepository.kt
│       ├── KhataRepository.kt
│       ├── InventoryRepository.kt
│       ├── ExpenseRepository.kt
│       ├── ReportRepository.kt
│       └── MarketplaceRepository.kt
├── domain/
│   └── model/
│       ├── Enums.kt                 // PartyType, TransactionType, PaymentMode
│       ├── CartItem.kt
│       ├── InvoiceDetail.kt
│       └── KhataSummary.kt
└── ui/
    ├── navigation/
    │   ├── EasyBizzyNavGraph.kt
    │   └── Destinations.kt
    ├── common/
    │   ├── Components.kt            // RupeeText, StatusChip, MetricCard, SearchBar
    │   └── WhatsAppHelper.kt        // Intent builder for receipt & reminder
    ├── billing/
    │   ├── BillingScreen.kt
    │   ├── CartSheet.kt
    │   ├── InvoiceReceiptScreen.kt
    │   └── BillingViewModel.kt
    ├── khata/
    │   ├── KhataHomeScreen.kt       // Customer/Supplier tabs with Lena/Dena
    │   ├── PartyDetailScreen.kt     // Statement timeline & payment log
    │   ├── AddTransactionSheet.kt
    │   └── KhataViewModel.kt
    ├── inventory/
    │   ├── ProductListScreen.kt
    │   ├── AddEditProductScreen.kt
    │   ├── MarketplacePublishDialog.kt
    │   └── InventoryViewModel.kt
    ├── expenses/
    │   ├── ExpenseListScreen.kt
    │   └── ExpenseViewModel.kt
    ├── reports/
    │   ├── ReportsScreen.kt
    │   └── ReportsViewModel.kt
    ├── marketplace/
    │   ├── CustomerMarketplaceScreen.kt // Discovery & price comparison
    │   ├── StorefrontDetailScreen.kt
    │   └── MarketplaceViewModel.kt
    ├── settings/
    │   ├── SettingsScreen.kt
    │   ├── BackupRestoreScreen.kt
    │   └── SettingsViewModel.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 5. Screen Inventory & UX Specifications

### Shopkeeper Screens
1. **Quick Billing / POS (`billing_screen`)**:
   - Quick-add grid of top items + instant search by product name/barcode.
   - "Custom Item / Chillar" quick amount entry for unlisted items.
   - Interactive sliding Cart Bar showing total items and amount in ₹.
   - Payment modal: Full Cash, UPI QR code generation, Split Payment, or "Add to Khata" (debit customer ledger directly).
2. **Receipt & Invoice Viewer (`invoice_receipt_screen/{invoiceId}`)**:
   - Clean printable/shareable invoice with shop header, GSTIN (optional), line items, and payment mode.
   - Direct WhatsApp share button formatting message with item summary, total, and thank-you note.
3. **Khata Ledger Hub (`khata_home_screen`)**:
   - Top banner: Total *You will get* (Green, ₹) and *You will give* (Red, ₹).
   - Dual tabs: **Customers (Grahak)** and **Suppliers (Vyapari)** with search and sorting by due amount.
   - Party cards showing contact number, last transaction date, and net balance.
4. **Party Ledger Detail (`party_detail_screen/{partyId}`)**:
   - Complete transaction timeline with "You gave" (Debit) and "You got" (Credit) entries.
   - Action row: "Send WhatsApp Reminder" (embeds UPI intent link for direct payment), "Download PDF Statement", "Settle Balance".
5. **Inventory Management (`inventory_screen`)**:
   - Product list with stock counter, unit type (kg, pcs, ltr, packet), purchase cost, and selling price.
   - Low-stock badge indicator.
   - Toggle switch on each item: **"Live on Marketplace"** with custom display price.
6. **Expense Tracker (`expense_screen`)**:
   - Categorized daily expense entries: Rent, Staff wages, Electricity, Tea/Snacks, Logistics, Miscellaneous.
   - Daily Cash Register reconciliation: (Opening Cash + Cash Sales - Cash Expenses = Closing Cash).
7. **Business Reports & Analytics (`reports_screen`)**:
   - Filterable summaries (Today, Last 7 Days, Month, Custom Range).
   - Total Gross Sales, Net Profit estimation, Cash vs UPI collection breakdown.
   - Outstanding Khata dues analysis and top-selling products.
8. **Shop Profile & Marketplace Settings (`shop_profile_screen`)**:
   - Shop Name, Category, Physical Address, Pincode, GPS coordinates, Phone number, UPI ID.
   - "Sync Catalog with Marketplace" button showing last synchronization timestamp.
9. **Backup & Security (`backup_screen`)**:
   - Database encryption status indicator.
   - Export encrypted snapshot to local downloads or Google Drive.
   - PIN / Biometric lock setup.

### Customer Screens
10. **Local Marketplace Discovery (`marketplace_discovery_screen`)**:
    - Location detector / Pincode selector.
    - Category pills: Kirana & Grocery, Fruits & Veggies, Pharmacy, Hardware, Dairy.
    - List of verified nearby shops with distance, open/closed status, and featured deals.
11. **Storefront & Product Catalog (`storefront_screen/{shopId}`)**:
    - Shop information card with direct **"Call Shop"** and **"WhatsApp Enquiry"** action buttons.
    - Searchable list of published items with live prices and packaging units.
12. **Price Comparison Engine (`price_compare_screen`)**:
    - Search bar for essential items (e.g. Sugar 1kg, Mustard Oil 1L, Basmati Rice 5kg).
    - Side-by-side price cards from all nearby merchants stocking the product.

---

## 6. Entity Model & Room Database Schema

### 1. `products`
```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String? = null,
    val category: String,
    val unit: String,               // "PCS", "KG", "LTR", "PACKET", "MTR"
    val purchasePrice: Double,      // Strictly private: wholesale cost
    val sellingPrice: Double,       // Standard store price
    val currentStock: Double,
    val minStockAlert: Double = 5.0,
    val isMarketplacePublished: Boolean = false,
    val marketplacePrice: Double? = null, // Optional discounted/special web price
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 2. `khata_parties`
```kotlin
@Entity(tableName = "khata_parties")
data class KhataPartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val partyType: String,          // "CUSTOMER" or "SUPPLIER"
    val currentBalance: Double = 0.0, // Positive: They owe you; Negative: You owe them
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 3. `khata_transactions`
```kotlin
@Entity(
    tableName = "khata_transactions",
    foreignKeys = [
        ForeignKey(
            entity = KhataPartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("partyId")]
)
data class KhataTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partyId: Long,
    val amount: Double,
    val transactionType: String,    // "GAVE" (Udhar/Debit) or "GOT" (Jama/Credit)
    val note: String? = null,
    val billId: Long? = null,       // Linked invoice if originated from billing
    val timestamp: Long = System.currentTimeMillis()
)
```

### 4. `invoices` & `invoice_items`
```kotlin
@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val netAmount: Double,
    val paymentMode: String,        // "CASH", "UPI", "KHATA", "SPLIT"
    val cashReceived: Double = 0.0,
    val upiReceived: Double = 0.0,
    val khataPartyId: Long? = null, // Linked khata account if credit bill
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long? = null,
    val productName: String,
    val unitPrice: Double,
    val quantity: Double,
    val totalPrice: Double
)
```

### 5. `expenses`
```kotlin
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,           // "RENT", "SALARY", "ELECTRICITY", "TEA_SNACKS", "MAINTENANCE", "OTHER"
    val amount: Double,
    val paymentMode: String,        // "CASH", "UPI"
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

### 6. `shop_profile`
```kotlin
@Entity(tableName = "shop_profile")
data class ShopProfileEntity(
    @PrimaryKey val id: Int = 1,    // Single-row merchant configuration
    val shopName: String,
    val ownerName: String,
    val phoneNumber: String,
    val category: String,
    val address: String,
    val pincode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val upiId: String? = null,
    val gstin: String? = null,
    val isMarketplaceActive: Boolean = false,
    val lastSyncTimestamp: Long? = null
)
```

---

## 7. Dependencies & External Configuration

### Gradle Dependencies to Enable
- `androidx-navigation-compose` (already declared in TOML: `2.8.9`)
- `androidx-room-runtime`, `androidx-room-ktx`, `androidx-room-compiler` (already in TOML: `2.7.0`)
- `coil-compose` (in TOML: `2.7.0` for shop banners and product icons)
- `androidx-datastore-preferences` (in TOML: `1.1.7` for merchant settings/PIN)
- `net.zetetic:sqlcipher-android:4.5.5` for hardware-backed encrypted Room databases
- `retrofit`, `converter-moshi`, `okhttp`, `logging-interceptor` (active in Gradle)

### External Services & Setup Requirements
- **Spring Boot Marketplace Server**:
  - Independent service running in its own repository/host.
  - The Android app connects via configurable base URL stored in `BuildConfig.MARKETPLACE_BASE_URL` (configured via `.env`).
  - Clear graceful offline degradation when backend is unreachable.
- **Google Drive Backup**:
  - Requires Google Cloud OAuth 2.0 Client ID for Drive `drive.appdata` scope (setup via Google Cloud Console with SHA-1 fingerprint).
- **Camera/Barcode Scanner (Optional)**:
  - Requires `android.permission.CAMERA` and CameraX for fast product barcode scanning.

---

## 8. Ordered Phased Build Plan

- [ ] **Milestone 1: Project Setup & Encrypted Database Foundation**
  - Configure `libs.versions.toml` and `app/build.gradle.kts` for Navigation Compose, Coil, DataStore, and SQLCipher.
  - Implement `KeyStoreManager` for generating and protecting AES-256 database passphrase.
  - Create Room entities, TypeConverters, DAOs, and `EasyBizzyDatabase` with encrypted `SupportOpenHelperFactory`.
  - Provide initial database seed data for common Indian retail categories and sample products.

- [ ] **Milestone 2: Navigation Architecture & Dual-Mode Shell**
  - Implement `EasyBizzyNavGraph` with bottom navigation destinations.
  - Build top-level Role Switcher (Shopkeeper Hub ⟷ Customer Marketplace).
  - Setup unified Material 3 Indian retail theme (Teal/Coral palette, custom rupee text components, responsive insets).

- [ ] **Milestone 3: Offline-First Billing & POS System**
  - Build `BillingViewModel` and `QuickBillingScreen` with fast item selection, cart counter, and custom amount dialog.
  - Implement payment modal supporting Cash, UPI, and direct Khata debit.
  - Build `InvoiceReceiptScreen` with formatted WhatsApp bill generator.

- [ ] **Milestone 4: Customer & Supplier Khata (Udhar Bahi-Khata)**
  - Implement `KhataRepository` and `KhataViewModel`.
  - Build `KhataHomeScreen` with tabs for Customers (Grahak) and Suppliers (Vyapari) displaying live Lena/Dena metrics.
  - Build `PartyDetailScreen` with ledger timeline, "You gave" / "You got" sheets, and WhatsApp payment reminder intent with UPI link.

- [ ] **Milestone 5: Inventory Management & Marketplace Publishing**
  - Implement `ProductListScreen` with stock tracking, low-stock warnings, and barcode search.
  - Create Add/Edit Product sheet with purchase price vs selling price separation.
  - Implement the "Publish to Marketplace" toggle isolating public listing metadata.

- [ ] **Milestone 6: Expenses, Cash Register & Business Reports**
  - Implement `ExpenseListScreen` with quick expense categories and daily cash drawer reconciliation.
  - Build `ReportsScreen` showing daily/weekly sales, gross profit estimate, and payment mode breakdowns.

- [ ] **Milestone 7: Public Marketplace & Customer Experience**
  - Implement `MarketplaceApiService` with Retrofit for the independent Spring Boot server.
  - Build `CustomerMarketplaceScreen` for searching nearby stores and filtering categories.
  - Build `StorefrontDetailScreen` with published price list and one-tap Call/WhatsApp buttons.
  - Build `PriceCompareScreen` for side-by-side local grocery price checks.

- [ ] **Milestone 8: Backup, Security & Verification**
  - Implement local encrypted database export/import and Google Drive backup workflow.
  - Write Robolectric JVM tests verifying Khata balance calculations, billing totals, and security boundary isolation.
  - Verify complete build with `compile_applet`.
