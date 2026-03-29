package com.group3.financialapplication.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.group3.financialapplication.data.Transaction
import com.group3.financialapplication.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

val transactionCategories = listOf(
    "Food", "Transport", "Clothing", "Housing", "Pets", "Substances", "Other"
)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController, viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(transactionCategories[0]) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Date picker — defaults to today, only past dates selectable
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Location
    var saveLocation by remember { mutableStateOf(false) }
    var locationStatus by remember { mutableStateOf("") }
    var capturedLat by remember { mutableStateOf<Double?>(null) }
    var capturedLng by remember { mutableStateOf<Double?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun fetchLocation() {
        locationStatus = "Getting location..."
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    capturedLat = loc.latitude
                    capturedLng = loc.longitude
                    locationStatus = "Location saved ✓"
                } else {
                    locationStatus = "Could not get location"
                    saveLocation = false
                }
            }
            .addOnFailureListener {
                locationStatus = "Location failed"
                saveLocation = false
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) fetchLocation()
        else { saveLocation = false; locationStatus = "Permission denied" }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            // Only allow selecting today or earlier
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Strip time component for fair comparison
                    val todayStart = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }.timeInMillis
                    return utcTimeMillis <= todayStart
                }
                override fun isSelectableYear(year: Int): Boolean {
                    return year <= Calendar.getInstance().get(Calendar.YEAR)
                }
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
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            // Date picker field
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
                        if (isExpense) ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded)
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

            // Location card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (capturedLat != null)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (capturedLat != null) Icons.Default.LocationOn
                            else Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = if (capturedLat != null) MaterialTheme.colorScheme.primary
                            else Color.Gray
                        )
                        Column {
                            Text("Save Location", style = MaterialTheme.typography.bodyMedium)
                            if (locationStatus.isNotEmpty()) {
                                Text(
                                    locationStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (capturedLat != null) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Switch(
                        checked = saveLocation,
                        onCheckedChange = { on ->
                            saveLocation = on
                            if (on) {
                                if (hasLocationPermission) fetchLocation()
                                else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            } else {
                                capturedLat = null; capturedLng = null; locationStatus = ""
                            }
                        }
                    )
                }
            }

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (description.isNotBlank() && amountDouble != null && amountDouble > 0) {
                        val transaction = Transaction(
                            description = description,
                            amount      = amountDouble,
                            date        = selectedDateMillis,
                            isExpense   = isExpense,
                            category    = if (isExpense) selectedCategory else "Income"
                        )
                        viewModel.addTransactionWithLocation(
                            transaction = transaction,
                            latitude    = capturedLat,
                            longitude   = capturedLng
                        )
                        navController.popBackStack()
                    } else {
                        errorMessage = "Please enter a valid description and a positive amount."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Transaction")
            }
        }
    }
}