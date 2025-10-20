// New content for Transaction.kt
package com.janis_petrovs.financialapplication.data // Will be updated when you move it

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: Long, // Use a timestamp for easy sorting and filtering
    val description: String,
    val isExpense: Boolean // true for expense, false for income
)