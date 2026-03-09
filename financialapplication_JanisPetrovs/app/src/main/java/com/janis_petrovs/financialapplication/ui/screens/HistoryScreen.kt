package com.janis_petrovs.financialapplication.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.janis_petrovs.financialapplication.data.Transaction
import com.janis_petrovs.financialapplication.data.TransactionDao
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.*

// Datu klases, kas nepieciešamas vēstures un atskaišu loģikai
data class ChartData(val label: String, val value: Float, val color: Color)
data class MonthlySummary(val month: String, val income: Double, val expense: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: FinanceViewModel) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    // Aprēķinām kopsavilkumu vēsturei, izmantojot jauno +/- amount sistēmu
    val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.amount < 0 }.sumOf { it.amount }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Transaction History") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text("Summary", style = MaterialTheme.typography.titleLarge)
                Text("Total Income: €${"%.2f".format(totalIncome)}", color = Color(0xFF008000))
                Text("Total Expenses: €${"%.2f".format(Math.abs(totalExpenses))}", color = Color.Red)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            items(transactions) { transaction ->
                HistoryRow(transaction)
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun HistoryRow(transaction: Transaction) {
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val isExpense = transaction.amount < 0 // Atpazīstam izdevumu pēc mīnuss zīmes

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            // Izmantojam title lauku
            Text(text = transaction.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = dateFormatter.format(Date(transaction.date)), style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = "€${"%.2f".format(Math.abs(transaction.amount))}",
            color = if (isExpense) Color.Red else Color(0xFF008000),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val fakeDao = object : TransactionDao {
        override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>> = flowOf(emptyList())

        override fun getTotalSpentForMonth(start: Long, end: Long): Flow<Double?> = flowOf(0.0)
        override fun getTransactionsWithLocation(): Flow<List<Transaction>> = flowOf(emptyList())

        override suspend fun insert(transaction: Transaction) {}
        override suspend fun delete(transaction: Transaction) {}
    }

    val fakeViewModel = FinanceViewModel(fakeDao)
    HistoryScreen(viewModel = fakeViewModel)
}