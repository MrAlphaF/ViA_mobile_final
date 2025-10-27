package com.group3.financialapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group3.financialapplication.data.Transaction
import com.group3.financialapplication.data.TransactionDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class ChartData(val category: String, val amount: Float)
data class MonthlySummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val chartData: List<ChartData>
)
data class GaugeData(val category: String, val spent: Double, val budget: Double)
data class BarChartData(val day: String, val amount: Float)
data class ReportsData(
    val gaugeData: List<GaugeData>,
    val barChartData: List<BarChartData>
)

class FinanceViewModel(private val dao: TransactionDao) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthlySummary: Flow<MonthlySummary> = _selectedDate.flatMapLatest {
        val (startOfMonth, endOfMonth) = getMonthStartAndEnd(it)
        dao.getTransactionsForMonth(startOfMonth, endOfMonth)
            .map {
                val income = it.filter { t -> !t.isExpense }.sumOf { t -> t.amount }
                val expenses = it.filter { t -> t.isExpense }.sumOf { t -> t.amount }
                val chartData = it.filter { t -> t.isExpense }
                    .groupBy { t -> t.category }
                    .map { (cat, list) -> ChartData(cat, list.sumOf { t -> t.amount }.toFloat()) }
                MonthlySummary(income, expenses, chartData)
            }
    }

    fun getReportsData(): Flow<ReportsData> {
        val (startOfMonth, endOfMonth) = getMonthStartAndEnd(_selectedDate.value)

        return dao.getTransactionsForMonth(startOfMonth, endOfMonth)
            .map { transactions ->
                val expenses = transactions.filter { it.isExpense }
                val totalExpenses = expenses.sumOf { it.amount }

                val spendingByCategory = expenses
                    .groupBy { it.category }
                    .mapValues { (_, transactionList) -> transactionList.sumOf { it.amount } }

                val spendingWithPercentages = spendingByCategory.map { (category, spent) ->
                    val percentage = if (totalExpenses > 0) spent / totalExpenses else 0.0
                    mapOf("category" to category, "spent" to spent, "percentage" to percentage)
                }.sortedByDescending { it["percentage"] as Double }

                val topTwo = spendingWithPercentages.take(2)
                val rest = spendingWithPercentages.drop(2)


                val finalGaugeData = topTwo.map {
                    GaugeData(it["category"] as String, it["spent"] as Double, totalExpenses)
                }.toMutableList()

                if (rest.isNotEmpty()) {
                    val otherSpent = rest.sumOf { it["spent"] as Double }
                    finalGaugeData.add(GaugeData("Other", otherSpent, totalExpenses))
                }

                val barChartData = expenses
                    .groupBy {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.get(Calendar.DAY_OF_MONTH).toString()
                    }
                    .map { (day, transactionList) ->
                        BarChartData(day, transactionList.sumOf { it.amount }.toFloat())
                    }
                    .sortedBy { it.day.toInt() }

                ReportsData(finalGaugeData, barChartData)
            }
    }

    private fun getMonthStartAndEnd(calendar: Calendar): Pair<Long, Long> {
        val start = calendar.clone() as Calendar
        start.set(Calendar.DAY_OF_MONTH, 1)
        start.set(Calendar.HOUR_OF_DAY, 0)

        val end = calendar.clone() as Calendar
        end.add(Calendar.MONTH, 1)
        end.add(Calendar.DAY_OF_MONTH, -1)
        end.set(Calendar.HOUR_OF_DAY, 23)
        return Pair(start.timeInMillis, end.timeInMillis)
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
        viewModelScope.launch { dao.insert(transaction) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { dao.delete(transaction) }
    }
}