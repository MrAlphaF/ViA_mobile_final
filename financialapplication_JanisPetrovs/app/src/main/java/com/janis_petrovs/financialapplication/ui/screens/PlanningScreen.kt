package com.janis_petrovs.financialapplication.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.janis_petrovs.financialapplication.data.Transaction
import com.janis_petrovs.financialapplication.data.TransactionDao
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(navController: NavController, viewModel: FinanceViewModel) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.isExpense }.sumOf { it.amount }
    val balance = totalIncome - totalExpenses
    val balanceColor = if (balance >= 0) Color(0xFF008000) else Color.Red

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Transactions") },
                    actions = {
                        IconButton(onClick = { navController.navigate("add_transaction") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                        }
                    }
                )
            },
            bottomBar = {
                Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Balance:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "€${"%.2f".format(balance)}",
                            fontSize = 18.sp,
                            color = balanceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Text(
                        "Recent History",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(transactions) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onDeleteClicked = { viewModel.deleteTransaction(transaction) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction, onDeleteClicked: () -> Unit) {
    val amountColor = if (transaction.isExpense) Color.Red else Color(0xFF008000)
    val amountPrefix = if (transaction.isExpense) "-" else "+"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.description, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = transaction.category, fontSize = 12.sp, color = Color.Gray)
        }
        Text(
            text = "$amountPrefix€${"%.2f".format(transaction.amount)}",
            fontSize = 16.sp,
            color = amountColor,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onDeleteClicked) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Transaction", tint = Color.Gray)
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlanningScreenPreview() {
    val fakeDao = object : TransactionDao {
        override fun getAllTransactions() =
            flowOf(emptyList<Transaction>())

        override fun getTransactionsForMonth(startOfMonth: Long, endOfMonth: Long) =
            flowOf(emptyList<Transaction>())

        override suspend fun insert(transaction: Transaction) {}
        override suspend fun delete(transaction: Transaction) {}
    }

    val fakeViewModel = FinanceViewModel(fakeDao)

    fakeViewModel.apply {}
    val navController = rememberNavController()
    PlanningScreen(navController, viewModel = fakeViewModel)
}