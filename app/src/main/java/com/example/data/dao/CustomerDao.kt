package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
  @Query("SELECT * FROM customers ORDER BY name ASC")
  fun getAllCustomers(): Flow<List<CustomerEntity>>

  @Query("SELECT * FROM customers WHERE id = :id")
  suspend fun getCustomerById(id: String): CustomerEntity?

  @Query("SELECT * FROM customers WHERE currentBalancePaise > 0 ORDER BY currentBalancePaise DESC")
  fun getCustomersWithDues(): Flow<List<CustomerEntity>>

  @Query("SELECT COALESCE(SUM(currentBalancePaise), 0) FROM customers WHERE currentBalancePaise > 0")
  fun getTotalReceivablesPaise(): Flow<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomer(customer: CustomerEntity)

  @Update
  suspend fun updateCustomer(customer: CustomerEntity)

  @Query("UPDATE customers SET currentBalancePaise = currentBalancePaise + :deltaPaise, updatedAt = :updatedAt WHERE id = :id")
  suspend fun adjustCustomerBalance(id: String, deltaPaise: Long, updatedAt: Long = System.currentTimeMillis())

  @Delete
  suspend fun deleteCustomer(customer: CustomerEntity)
}
