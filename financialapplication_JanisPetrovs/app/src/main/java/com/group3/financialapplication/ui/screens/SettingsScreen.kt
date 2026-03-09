package com.group3.financialapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.group3.financialapplication.data.SettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val settings by settingsManager.settingsFlow.collectAsState(
        initial = com.group3.financialapplication.data.AppSettings(1000.0, "€", true)
    )
    val scope = rememberCoroutineScope()

    var limitInput by remember(settings.monthlyLimit) {
        mutableStateOf(settings.monthlyLimit.toString())
    }
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- Monthly Limit ---
            Text("Monthly Spending Limit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = limitInput,
                onValueChange = { limitInput = it },
                label = { Text("Limit (${settings.currency})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = {
                        val parsed = limitInput.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            scope.launch { settingsManager.setMonthlyLimit(parsed) }
                        }
                    }) {
                        Text("Save")
                    }
                }
            )

            HorizontalDivider()

            // --- Currency ---
            Text("Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ExposedDropdownMenuBox(
                expanded = currencyMenuExpanded,
                onExpandedChange = { currencyMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = settings.currency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Currency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = currencyMenuExpanded,
                    onDismissRequest = { currencyMenuExpanded = false }
                ) {
                    SettingsManager.CURRENCIES.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                scope.launch { settingsManager.setCurrency(currency) }
                                currencyMenuExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // --- Notifications ---
            Text("Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Enable Spending Alerts", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Get notified when nearing your limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { settingsManager.setNotificationsEnabled(enabled) }
                    }
                )
            }
        }
    }
}