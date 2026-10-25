package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
  @Query("SELECT * FROM suppliers ORDER BY name ASC")
  fun getAllSuppliers(): Flow<List<SupplierEntity>>

  @Query("SELECT * FROM suppliers WHERE id = :id")
  suspend fun getSupplierById(id: String): SupplierEntity?

  @Query("SELECT * FROM suppliers WHERE currentBalancePaise > 0 ORDER BY currentBalancePaise DESC")
  fun getSuppliersWithDues(): Flow<List<SupplierEntity>>

  @Query("SELECT COALESCE(SUM(currentBalancePaise), 0) FROM suppliers WHERE currentBalancePaise > 0")
  fun getTotalPayablesPaise(): Flow<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSupplier(supplier: SupplierEntity)

  @Update
  suspend fun updateSupplier(supplier: SupplierEntity)

  @Query("UPDATE suppliers SET currentBalancePaise = currentBalancePaise + :deltaPaise, updatedAt = :updatedAt WHERE id = :id")
  suspend fun adjustSupplierBalance(id: String, deltaPaise: Long, updatedAt: Long = System.currentTimeMillis())

  @Delete
  suspend fun deleteSupplier(supplier: SupplierEntity)
}
