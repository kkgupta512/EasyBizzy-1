package com.example.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Composite relation containing a Bill header and all its preserved historical line items.
 */
data class BillWithItems(
  @Embedded val bill: BillEntity,
  @Relation(
    parentColumn = "id",
    entityColumn = "billId"
  )
  val items: List<BillItemEntity>,
)
