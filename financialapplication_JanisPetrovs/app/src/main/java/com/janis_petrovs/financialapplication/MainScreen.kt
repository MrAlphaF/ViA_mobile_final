package com.janis_petrovs.financialapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.janis_petrovs.financialapplication.ui.screens.AddTransactionScreen
import com.janis_petrovs.financialapplication.ui.screens.HistoryScreen
import com.janis_petrovs.financialapplication.ui.screens.PlanningScreen
import com.janis_petrovs.financialapplication.ui.screens.ReportsScreen
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

sealed class DrawerScreen(val route: String, val label: String, val icon: ImageVector) {
    object Planning : DrawerScreen("planning", "Planning", Icons.Default.List)
    object History : DrawerScreen("history", "History", Icons.Default.History)
    object Reports : DrawerScreen("reports", "Reports", Icons.Default.Assessment)
}

val drawerItems = listOf(
    DrawerScreen.Reports,
    DrawerScreen.Planning,
    DrawerScreen.History,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FinanceViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            )
        }
    ) {

        Scaffold(
            topBar = {

                TopAppBar(
                    title = { Text("Financial Planner") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController,
                startDestination = DrawerScreen.Planning.route,
                Modifier.padding(innerPadding)
            ) {
                composable(DrawerScreen.Planning.route) { PlanningScreen(navController, viewModel) }
                composable(DrawerScreen.History.route) { HistoryScreen() }
                composable(DrawerScreen.Reports.route) { ReportsScreen() }
                composable("add_transaction") { AddTransactionScreen(navController, viewModel) }
            }
        }
    }
}

@Composable
fun AppDrawerContent(onItemClick: (String) -> Unit, currentRoute: String?) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Anna", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Anna@company.com", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Divider()


            drawerItems.forEach { screen ->
                NavigationDrawerItem(
                    icon = { Icon(screen.icon, contentDescription = null) },
                    label = { Text(screen.label) },
                    selected = currentRoute == screen.route,
                    onClick = { onItemClick(screen.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}