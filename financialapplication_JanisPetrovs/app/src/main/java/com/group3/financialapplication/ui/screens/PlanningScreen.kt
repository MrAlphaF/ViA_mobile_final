package com.group3.financialapplication.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.group3.financialapplication.data.AppSettings
import com.group3.financialapplication.data.SettingsManager
import com.group3.financialapplication.data.Transaction
import com.group3.financialapplication.ui.viewmodel.FinanceViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(navController: NavController, viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val settingsManager = androidx.compose.runtime.remember { SettingsManager(context) }
    val settings by settingsManager.settingsFlow.collectAsState(
        initial = AppSettings(1000.0, "€", true)
    )

    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    // Current month expenses only for limit tracking
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val thisMonthExpenses = transactions.filter {
        it.isExpense && run {
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
    }
    val totalSpentThisMonth = thisMonthExpenses.sumOf { it.amount }

    val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.isExpense }.sumOf { it.amount }
    val balance = totalIncome - totalExpenses
    val balanceColor = if (balance >= 0) Color(0xFF008000) else Color.Red

    // Spending limit progress
    val monthlyLimit = settings.monthlyLimit
    val limitProgress = if (monthlyLimit > 0) (totalSpentThisMonth / monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
    val isOverLimit = totalSpentThisMonth > monthlyLimit
    val limitColor = when {
        limitProgress < 0.5f -> Color(0xFF008000)
        limitProgress < 0.8f -> Color(0xFFFF9800)
        else -> Color.Red
    }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Balance:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${settings.currency}${"%.2f".format(balance)}",
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

            // --- Spending Limit Card ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Monthly Limit",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${settings.currency}${"%.2f".format(totalSpentThisMonth)} / ${settings.currency}${"%.2f".format(monthlyLimit)}",
                                fontSize = 13.sp,
                                color = if (isOverLimit) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { limitProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = limitColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        if (isOverLimit) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "⚠ Over limit by ${settings.currency}${"%.2f".format(totalSpentThisMonth - monthlyLimit)}",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${settings.currency}${"%.2f".format(monthlyLimit - totalSpentThisMonth)} remaining this month",
                                color = limitColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

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
                    currency = settings.currency,
                    onDeleteClicked = { viewModel.deleteTransaction(transaction) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction, currency: String, onDeleteClicked: () -> Unit) {
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
            text = "$amountPrefix$currency${"%.2f".format(transaction.amount)}",
            fontSize = 16.sp,
            color = amountColor,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onDeleteClicked) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Transaction", tint = Color.Gray)
        }
    }
}