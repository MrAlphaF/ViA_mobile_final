package com.group3.financialapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class TransactionLocation(
    val transactionId: Int,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean
)

class LocationManager(context: Context) {

    private val file = File(context.filesDir, "transaction_locations.json")

    fun saveLocation(location: TransactionLocation) {
        val all = loadAll().toMutableList()
        all.removeAll { it.transactionId == location.transactionId }
        all.add(location)
        writeAll(all)
    }

    fun deleteLocation(transactionId: Int) {
        val all = loadAll().toMutableList()
        all.removeAll { it.transactionId == transactionId }
        writeAll(all)
    }

    fun loadAll(): List<TransactionLocation> {
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                TransactionLocation(
                    transactionId = o.getInt("transactionId"),
                    latitude      = o.getDouble("latitude"),
                    longitude     = o.getDouble("longitude"),
                    description   = o.getString("description"),
                    amount        = o.getDouble("amount"),
                    category      = o.getString("category"),
                    isExpense     = o.getBoolean("isExpense")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeAll(locations: List<TransactionLocation>) {
        val arr = JSONArray()
        locations.forEach { loc ->
            arr.put(JSONObject().apply {
                put("transactionId", loc.transactionId)
                put("latitude",      loc.latitude)
                put("longitude",     loc.longitude)
                put("description",   loc.description)
                put("amount",        loc.amount)
                put("category",      loc.category)
                put("isExpense",     loc.isExpense)
            })
        }
        file.writeText(arr.toString())
    }
}