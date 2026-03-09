package com.group3.financialapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.group3.financialapplication.data.DatabaseProvider
import com.group3.financialapplication.ui.screens.LoginScreen
import com.group3.financialapplication.ui.screens.SignUpScreen
import com.group3.financialapplication.ui.theme.FinancialApplicationTheme
import com.group3.financialapplication.ui.viewmodel.FinanceViewModel
import com.group3.financialapplication.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {

    private val financeViewModel: FinanceViewModel by viewModels {
        FinanceViewModelFactory(DatabaseProvider.getDatabase(this).transactionDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinancialApplicationTheme {
                AppNavigator(viewModel = financeViewModel)
            }
        }
    }
}

@Composable
fun AppNavigator(viewModel: FinanceViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth") {
        navigation(startDestination = "login", route = "auth") {
            composable("login") {
                LoginScreen(navController = navController)
            }
            composable("signup") {
                SignUpScreen(navController = navController)
            }
        }
        
        composable("main_app") {
            MainScreen(viewModel = viewModel)
        }
    }
}