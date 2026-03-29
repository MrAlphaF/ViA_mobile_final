package com.group3.financialapplication.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group3.financialapplication.data.LocationManager
import com.group3.financialapplication.data.Transaction
import com.group3.financialapplication.data.TransactionDao
import com.group3.financialapplication.data.TransactionLocation
import com.group3.financialapplication.widget.BudgetWidgetReceiver
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

class FinanceViewModel(
    private val dao: TransactionDao,
    private val appContext: Context
) : ViewModel() {

    private val locationManager = LocationManager(appContext)

    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthlySummary: Flow<MonthlySummary> = _selectedDate.flatMapLatest { cal ->
        val (start, end) = getMonthStartAndEnd(cal)
        dao.getTransactionsForMonth(start, end).map { list ->
            val income   = list.filter { !it.isExpense }.sumOf { it.amount }
            val expenses = list.filter {  it.isExpense }.sumOf { it.amount }
            val chartData = list.filter { it.isExpense }
                .groupBy { it.category }
                .map { (cat, items) -> ChartData(cat, items.sumOf { it.amount }.toFloat()) }
            MonthlySummary(income, expenses, chartData)
        }
    }

    fun getReportsData(): Flow<ReportsData> {
        val (start, end) = getMonthStartAndEnd(_selectedDate.value)
        return dao.getTransactionsForMonth(start, end).map { transactions ->
            val expenses      = transactions.filter { it.isExpense }
            val totalExpenses = expenses.sumOf { it.amount }

            val ranked = expenses
                .groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .map { (cat, spent) -> Triple(cat, spent, if (totalExpenses > 0) spent / totalExpenses else 0.0) }
                .sortedByDescending { it.third }

            val topTwo = ranked.take(2)
            val rest   = ranked.drop(2)
            val gaugeData = topTwo
                .map { (cat, spent, _) -> GaugeData(cat, spent, totalExpenses) }
                .toMutableList()
            if (rest.isNotEmpty()) {
                gaugeData.add(GaugeData("Other", rest.sumOf { it.second }, totalExpenses))
            }

            val barData = expenses
                .groupBy {
                    Calendar.getInstance().apply { timeInMillis = it.date }
                        .get(Calendar.DAY_OF_MONTH).toString()
                }
                .map { (day, list) -> BarChartData(day, list.sumOf { it.amount }.toFloat()) }
                .sortedBy { it.day.toInt() }

            ReportsData(gaugeData, barData)
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.insert(transaction)
            pingWidget()
        }
    }

    fun addTransactionWithLocation(
        transaction: Transaction,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            dao.insert(transaction)
            if (latitude != null && longitude != null) {
                // Find the inserted row by matching description + amount + date
                val inserted = dao.getAllTransactions().first().firstOrNull {
                    it.description == transaction.description &&
                            it.amount == transaction.amount &&
                            it.date == transaction.date
                }
                inserted?.let {
                    locationManager.saveLocation(
                        TransactionLocation(
                            transactionId = it.id,
                            latitude      = latitude,
                            longitude     = longitude,
                            description   = it.description,
                            amount        = it.amount,
                            category      = it.category,
                            isExpense     = it.isExpense
                        )
                    )
                }
            }
            pingWidget()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.delete(transaction)
            locationManager.deleteLocation(transaction.id)
            pingWidget()
        }
    }

    private fun pingWidget() {
        val intent = Intent(appContext, BudgetWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        appContext.sendBroadcast(intent)
    }

    fun goToPreviousMonth() {
        _selectedDate.value = (_selectedDate.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
    }

    fun goToNextMonth() {
        _selectedDate.value = (_selectedDate.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
    }

    private fun getMonthStartAndEnd(calendar: Calendar): Pair<Long, Long> {
        val start = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val end = (calendar.clone() as Calendar).apply {
            add(Calendar.MONTH, 1); set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }
        return Pair(start.timeInMillis, end.timeInMillis)
    }
}