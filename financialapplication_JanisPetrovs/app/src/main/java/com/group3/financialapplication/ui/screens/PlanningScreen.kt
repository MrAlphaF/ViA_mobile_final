package com.group3.financialapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val settingsManager = remember { SettingsManager(context) }
    val settings by settingsManager.settingsFlow.collectAsState(
        initial = AppSettings(1000.0, "€", true)
    )
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    val totalIncome   = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val totalExpenses = transactions.filter {  it.isExpense }.sumOf { it.amount }
    val balance       = totalIncome - totalExpenses
    val balanceColor  = if (balance >= 0) Color(0xFF2E7D32) else Color.Red

    val now = Calendar.getInstance()
    val totalSpentThisMonth = transactions.filter { t ->
        t.isExpense && Calendar.getInstance().apply { timeInMillis = t.date }.let {
            it.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                    it.get(Calendar.YEAR)  == now.get(Calendar.YEAR)
        }
    }.sumOf { it.amount }

    val monthlyLimit  = settings.monthlyLimit
    val limitProgress = if (monthlyLimit > 0)
        (totalSpentThisMonth / monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
    val isOverLimit   = totalSpentThisMonth > monthlyLimit
    val limitColor    = when {
        limitProgress < 0.5f -> Color(0xFF2E7D32)
        limitProgress < 0.8f -> Color(0xFFFF9800)
        else                 -> Color.Red
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { navController.navigate("add_transaction") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
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
                    Text("Balance:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            // Monthly limit card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                                "${settings.currency}${"%.2f".format(totalSpentThisMonth)} / ${settings.currency}${"%.2f".format(monthlyLimit)}",
                                fontSize = 13.sp,
                                color = if (isOverLimit) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { limitProgress },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = limitColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isOverLimit)
                                "⚠ Over limit by ${settings.currency}${"%.2f".format(totalSpentThisMonth - monthlyLimit)}"
                            else
                                "${settings.currency}${"%.2f".format(monthlyLimit - totalSpentThisMonth)} remaining this month",
                            color = if (isOverLimit) Color.Red else limitColor,
                            fontSize = 12.sp,
                            fontWeight = if (isOverLimit) FontWeight.Medium else FontWeight.Normal
                        )
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

            items(transactions, key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    currency = settings.currency,
                    onEditClicked = {
                        navController.navigate("edit_transaction/${transaction.id}")
                    },
                    onDeleteClicked = {
                        viewModel.deleteTransaction(transaction)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    currency: String,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val amountColor  = if (transaction.isExpense) Color.Red else Color(0xFF2E7D32)
    val amountPrefix = if (transaction.isExpense) "-" else "+"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.description, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(transaction.category, fontSize = 12.sp, color = Color.Gray)
        }
        Text(
            text = "$amountPrefix$currency${"%.2f".format(transaction.amount)}",
            fontSize = 15.sp,
            color = amountColor,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onEditClicked) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
        }
        IconButton(onClick = onDeleteClicked) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
        }
    }
}