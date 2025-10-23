package com.janis_petrovs.financialapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.janis_petrovs.financialapplication.data.Transaction
import com.janis_petrovs.financialapplication.data.TransactionDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class ChartData(val category: String, val amount: Float)

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val chartData: List<ChartData>
)

class FinanceViewModel(private val dao: TransactionDao) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()


    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()


    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthlySummary: Flow<MonthlySummary> = _selectedDate.flatMapLatest { calendar ->

        val startOfMonth = calendar.clone() as Calendar
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0)

        val endOfMonth = calendar.clone() as Calendar
        endOfMonth.add(Calendar.MONTH, 1)
        endOfMonth.add(Calendar.DAY_OF_MONTH, -1)
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23)

        dao.getTransactionsForMonth(startOfMonth.timeInMillis, endOfMonth.timeInMillis)
            .map { transactions ->
                val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
                val expenses = transactions.filter { it.isExpense }.sumOf { it.amount }
                val chartData = transactions
                    .filter { it.isExpense }
                    .groupBy { it.category }
                    .map { (category, transactionList) ->
                        ChartData(
                            category = category,
                            amount = transactionList.sumOf { it.amount }.toFloat()
                        )
                    }
                MonthlySummary(totalIncome = income, totalExpenses = expenses, chartData = chartData)
            }
    }


    fun goToPreviousMonth() {
        val newDate = _selectedDate.value.clone() as Calendar
        newDate.add(Calendar.MONTH, -1)
        _selectedDate.value = newDate
    }

    fun goToNextMonth() {
        val newDate = _selectedDate.value.clone() as Calendar
        newDate.add(Calendar.MONTH, 1)
        _selectedDate.value = newDate
    }


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
}