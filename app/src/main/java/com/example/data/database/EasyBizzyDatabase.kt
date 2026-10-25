package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BackupMetadataDao
import com.example.data.dao.BillDao
import com.example.data.dao.BillDraftDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.LedgerDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.ProductDao
import com.example.data.dao.PublishingPreferencesDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.dao.SupplierDao
import com.example.data.entity.BackupMetadataEntity
import com.example.data.entity.BillDraftEntity
import com.example.data.entity.BillEntity
import com.example.data.entity.BillItemEntity
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.LedgerEntryEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.PublicPublishingPreferencesEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.data.entity.SupplierEntity

/**
 * Main Room Database for EasyBizzy.
 *
 * Security Architecture & SQLCipher Integration:
 * - Compatible reviewed encryption engine: `net.zetetic:sqlcipher-android` (v4.5.5+).
 * - Configuration required for hardware-backed encryption:
 *   1. Generate a 256-bit passphrase using Android KeyStore (`KeyGenParameterSpec` with AES/GCM).
 *   2. Instantiate `net.sqlcipher.database.SupportOpenHelperFactory(passphraseBytes)`.
 *   3. Supply `.openHelperFactory(factory)` to `Room.databaseBuilder(...)`.
 * - Zero Leakage: Customer Khata ledgers, supplier accounts, bill histories, and expenses
 *   reside solely within this local SQLite file and are never sent to public marketplace endpoints.
 */
@Database(
  entities = [
    ProductEntity::class,
    CustomerEntity::class,
    SupplierEntity::class,
    BillEntity::class,
    BillItemEntity::class,
    BillDraftEntity::class,
    LedgerEntryEntity::class,
    PaymentEntity::class,
    ExpenseEntity::class,
    ShopSettingsEntity::class,
    BackupMetadataEntity::class,
    PublicPublishingPreferencesEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class EasyBizzyDatabase : RoomDatabase() {

  abstract fun productDao(): ProductDao
  abstract fun customerDao(): CustomerDao
  abstract fun supplierDao(): SupplierDao
  abstract fun billDao(): BillDao
  abstract fun billDraftDao(): BillDraftDao
  abstract fun ledgerDao(): LedgerDao
  abstract fun paymentDao(): PaymentDao
  abstract fun expenseDao(): ExpenseDao
  abstract fun shopSettingsDao(): ShopSettingsDao
  abstract fun backupMetadataDao(): BackupMetadataDao
  abstract fun publishingPreferencesDao(): PublishingPreferencesDao

  companion object {
    const val DATABASE_NAME = "easybizzy_local.db"

    @Volatile
    private var INSTANCE: EasyBizzyDatabase? = null

    /**
     * Singleton accessor for application runtime.
     */
    fun getInstance(context: Context): EasyBizzyDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          EasyBizzyDatabase::class.java,
          DATABASE_NAME,
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    /**
     * Factory for creating an in-memory database for testing.
     */
    fun createInMemoryDatabase(context: Context): EasyBizzyDatabase {
      return Room.inMemoryDatabaseBuilder(
        context.applicationContext,
        EasyBizzyDatabase::class.java,
      )
        .allowMainThreadQueries()
        .build()
    }

    /**
     * Example Migration path from Version 1 to Version 2:
     * Adds an optional tags column to the products table.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE products ADD COLUMN tags TEXT DEFAULT NULL")
      }
    }
  }
}
