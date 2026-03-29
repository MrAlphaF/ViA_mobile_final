package com.group3.financialapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.group3.financialapplication.data.Transaction
import com.group3.financialapplication.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    transaction: Transaction
) {
    var description by remember { mutableStateOf(transaction.description) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var isExpense by remember { mutableStateOf(transaction.isExpense) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedDateMillis by remember { mutableStateOf(transaction.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val todayEnd = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }.timeInMillis
                    return utcTimeMillis <= todayEnd
                }
                override fun isSelectableYear(year: Int): Boolean =
                    year <= Calendar.getInstance().get(Calendar.YEAR)
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    errorMessage?.let {
        ErrorDialog(message = it, onDismiss = { errorMessage = null })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Date picker
            OutlinedTextField(
                value = dateFormatter.format(Date(selectedDateMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                    }
                },
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
                RadioButton(selected = isExpense, onClick = {
                    isExpense = true
                    if (selectedCategory == "Income") selectedCategory = transactionCategories[0]
                })
                Text("Expense", Modifier.padding(start = 4.dp))
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = !isExpense, onClick = {
                    isExpense = false
                    selectedCategory = "Income"
                })
                Text("Income", Modifier.padding(start = 4.dp))
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (description.isNotBlank() && amountDouble != null && amountDouble > 0) {
                        viewModel.updateTransaction(
                            transaction.copy(
                                description = description,
                                amount      = amountDouble,
                                date        = selectedDateMillis,
                                isExpense   = isExpense,
                                category    = if (isExpense) selectedCategory else "Income"
                            )
                        )
                        navController.popBackStack()
                    } else {
                        errorMessage = "Please enter a valid description and a positive amount."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}