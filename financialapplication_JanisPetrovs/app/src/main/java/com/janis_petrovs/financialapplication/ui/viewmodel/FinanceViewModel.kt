package com.janis_petrovs.financialapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.janis_petrovs.financialapplication.data.Transaction
import com.janis_petrovs.financialapplication.data.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

// Datu klases ziņojumiem
data class ReportsData(
    val gaugeData: List<GaugeData>,
    val barChartData: List<BarChartData>
)

data class GaugeData(val category: String, val spent: Double, val budget: Double)
data class BarChartData(val day: String, val amount: Double)

class FinanceViewModel(private val dao: TransactionDao) : ViewModel() {

    // Visi darījumi plūsmai
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.insert(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.delete(transaction)
        }
    }

    // Labots: Reports loģika, kas izmanto jauno amount sistēmu (+/-)
    fun getReportsData(): Flow<ReportsData> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endOfMonth = calendar.timeInMillis

        return dao.getTransactionsForMonth(startOfMonth, endOfMonth).map { transactions ->
            // Grupējam tikai izdevumus (kur amount < 0)
            val expensesByCategory = transactions.filter { it.amount < 0 }
                .groupBy { it.category }
                .map { (category, list) ->
                    GaugeData(category, Math.abs(list.sumOf { it.amount }), 500.0) // Budžets pagaidām statisks
                }

            val dailyExpenses = transactions.filter { it.amount < 0 }
                .groupBy {
                    val c = Calendar.getInstance()
                    c.timeInMillis = it.date
                    c.get(Calendar.DAY_OF_MONTH).toString()
                }
                .map { (day, list) ->
                    BarChartData(day, Math.abs(list.sumOf { it.amount }))
                }

            ReportsData(expensesByCategory, dailyExpenses)
        }
    }
}