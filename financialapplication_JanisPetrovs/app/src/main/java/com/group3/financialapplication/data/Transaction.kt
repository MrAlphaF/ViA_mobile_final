package com.group3.financialapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: Long,
    val description: String,
    val isExpense: Boolean // true for expense, false for income
)