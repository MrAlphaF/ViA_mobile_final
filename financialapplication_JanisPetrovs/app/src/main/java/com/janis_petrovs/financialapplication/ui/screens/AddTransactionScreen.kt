package com.janis_petrovs.financialapplication.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.janis_petrovs.financialapplication.data.Transaction
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModel
import java.util.Date

import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.janis_petrovs.financialapplication.data.TransactionDao
import kotlinx.coroutines.flow.flowOf
import com.janis_petrovs.financialapplication.ui.theme.Orange
import com.janis_petrovs.financialapplication.ui.theme.DarkOrange
import com.janis_petrovs.financialapplication.ui.theme.BackgroundDark

val transactionCategories = listOf(
    "Food", "Transport", "Clothing", "Housing", "Pets", "Substances", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController, viewModel: FinanceViewModel) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(transactionCategories[0]) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    errorMessage?.let {
        ErrorDialog(message = it, onDismiss = { errorMessage = null })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Transaction") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (e.g., Salary)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = isCategoryMenuExpanded && isExpense,
                    onExpandedChange = { if (isExpense) isCategoryMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (isExpense) selectedCategory else "Income",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        enabled = isExpense,
                        trailingIcon = {
                            if (isExpense) ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isCategoryMenuExpanded
                            )
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryMenuExpanded && isExpense,
                        onDismissRequest = { isCategoryMenuExpanded = false }
                    ) {
                        transactionCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isExpense, onClick = { isExpense = true })
                    Text("Expense", Modifier.padding(start = 4.dp))
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = !isExpense, onClick = { isExpense = false })
                    Text("Income", Modifier.padding(start = 4.dp))
                }

                Button(
                    onClick = {
                        val amountDouble = amount.toDoubleOrNull()
                        if (description.isNotBlank() && amountDouble != null && amountDouble > 0) {
                            val newTransaction = Transaction(
                                description = description,
                                amount = amountDouble,
                                date = Date().time,
                                isExpense = isExpense,
                                category = if (isExpense) selectedCategory else "Income"
                            )
                            viewModel.addTransaction(newTransaction)
                            navController.popBackStack()
                        } else {

                            errorMessage = "Please enter a valid description and a positive amount."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(3.dp, Orange),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkOrange,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Save Transaction")
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddTransactionScreenPreview() {
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
    AddTransactionScreen(navController, viewModel = fakeViewModel)
}