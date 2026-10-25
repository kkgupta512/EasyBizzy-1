package com.example.data.util

import com.example.ui.viewmodel.DraftCartItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Serializer for caching active bill drafts into SQLite via Room.
 * Enables automatic draft preservation and failure recovery without external libraries.
 */
object DraftSerializer {

  fun serializeItems(items: List<DraftCartItem>): String {
    val array = JSONArray()
    for (item in items) {
      val obj = JSONObject().apply {
        put("id", item.id)
        put("productId", item.productId ?: "")
        put("productName", item.productName)
        put("brand", item.brand ?: "")
        put("specification", item.specification ?: "")
        put("unit", item.unit)
        put("quantity", item.quantity)
        put("unitPricePaise", item.unitPricePaise)
        put("discountPaise", item.discountPaise)
        put("taxRatePercent", item.taxRatePercent)
      }
      array.put(obj)
    }
    return array.toString()
  }

  fun deserializeItems(json: String?): List<DraftCartItem> {
    if (json.isNullOrBlank()) return emptyList()
    val list = mutableListOf<DraftCartItem>()
    try {
      val array = JSONArray(json)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          DraftCartItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            productId = obj.optString("productId").ifBlank { null },
            productName = obj.optString("productName", "Item"),
            brand = obj.optString("brand").ifBlank { null },
            specification = obj.optString("specification").ifBlank { null },
            unit = obj.optString("unit", "PCS"),
            quantity = obj.optDouble("quantity", 1.0),
            unitPricePaise = obj.optLong("unitPricePaise", 0L),
            discountPaise = obj.optLong("discountPaise", 0L),
            taxRatePercent = obj.optDouble("taxRatePercent", 0.0),
          )
        )
      }
    } catch (_: Exception) {
      // Graceful fallback on malformed JSON
    }
    return list
  }
}
