package com.janis_petrovs.financialapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.janis_petrovs.financialapplication.data.DatabaseProvider
import com.janis_petrovs.financialapplication.ui.theme.FinancialApplicationTheme
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModel
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {

    private val financeViewModel: FinanceViewModel by viewModels {
        FinanceViewModelFactory(DatabaseProvider.getDatabase(this).transactionDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinancialApplicationTheme {
                MainScreen(viewModel = financeViewModel)
            }
        }
    }
}