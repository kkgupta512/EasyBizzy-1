package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
  @Query("SELECT * FROM payments ORDER BY timestamp DESC")
  fun getAllPayments(): Flow<List<PaymentEntity>>

  @Query("SELECT * FROM payments WHERE partyId = :partyId ORDER BY timestamp DESC")
  fun getPaymentsForParty(partyId: String): Flow<List<PaymentEntity>>

  @Query("SELECT * FROM payments WHERE billId = :billId LIMIT 1")
  suspend fun getPaymentForBill(billId: String): PaymentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: PaymentEntity)
}
