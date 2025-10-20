// In: ui/screens/PlanningScreen.kt
package com.janis_petrovs.financialapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.janis_petrovs.financialapplication.ui.theme.FinancialApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen() {
    // A list of sample categories based on your mockup
    val categories = listOf("Food", "Fuel", "Repairs", "Clothing", "Pets")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planning") },
                actions = {
                    IconButton(onClick = { /* TODO: Handle add action */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item")
                    }
                }
            )
        },
        bottomBar = {
            // This mimics the "Income" section at the bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp // Gives a nice shadow effect
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Income:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "10000 $", fontSize = 18.sp)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(categories) { category ->
                CategoryRow(categoryName = category)
                Divider() // A thin line between items
            }
        }
    }
}

@Composable
fun CategoryRow(categoryName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = categoryName, fontSize = 16.sp, modifier = Modifier.weight(1f))
    }
}

// The preview allows you to see your design without running the app
@Preview(showBackground = true)
@Composable
fun PlanningScreenPreview() {
    FinancialApplicationTheme {
        PlanningScreen()
    }
}