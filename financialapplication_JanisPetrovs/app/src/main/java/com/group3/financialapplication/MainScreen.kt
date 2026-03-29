package com.group3.financialapplication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.group3.financialapplication.data.ProfileData
import com.group3.financialapplication.data.UserProfileManager
import com.group3.financialapplication.ui.screens.*
import com.group3.financialapplication.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

// All top-level destinations — drawer can navigate to any of these
sealed class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    object Planning       : AppDestination("planning",        "Planning",      Icons.Default.List)
    object History        : AppDestination("history",         "History",       Icons.Default.History)
    object Reports        : AppDestination("reports",         "Reports",       Icons.Default.Assessment)
    object ReceiptScanner : AppDestination("receipt_scanner", "Scan Receipt",  Icons.Default.DocumentScanner)
    object Settings       : AppDestination("settings",        "Settings",      Icons.Default.Settings)
    object Map            : AppDestination("map",             "Map",           Icons.Default.Map)
}

// Items shown in the drawer (top section = main nav, bottom section = tools)
val drawerMainItems = listOf(
    AppDestination.Reports,
    AppDestination.Planning,
    AppDestination.History,
    AppDestination.Map
)
val drawerToolItems = listOf(
    AppDestination.ReceiptScanner,
    AppDestination.Settings
)

// Routes where the top AppBar back button should be shown instead of the hamburger
val detailRoutes = setOf("add_transaction", "profile", "edit_transaction")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FinanceViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val userProfileManager = remember { UserProfileManager(context) }
    var profileData by remember { mutableStateOf(userProfileManager.getProfile()) }
    val reloadProfileData = { profileData = userProfileManager.getProfile() }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Navigate to a top-level drawer destination — always clear back stack so
    // pressing back from any main screen doesn't go back to a previous main screen.
    fun navigateTopLevel(route: String) {
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Disable swipe-to-open drawer on map screen — map needs horizontal swipe for panning
    val gesturesEnabled = currentRoute != AppDestination.Map.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            AppDrawerContent(
                profileData = profileData,
                currentRoute = currentRoute,
                onItemClick = { route -> navigateTopLevel(route) },
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("profile")
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Financial Planner") },
                    navigationIcon = {
                        if (detailRoutes.any { currentRoute?.startsWith(it) == true }) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Planning.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppDestination.Planning.route) {
                    PlanningScreen(navController, viewModel)
                }
                composable(AppDestination.History.route) {
                    HistoryScreen(viewModel)
                }
                composable(AppDestination.Reports.route) {
                    ReportsScreen(viewModel)
                }
                composable(AppDestination.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(AppDestination.ReceiptScanner.route) {
                    ReceiptScannerScreen(navController, viewModel)
                }
                composable(AppDestination.Map.route) {
                    MapScreen()
                }
                composable("add_transaction") {
                    AddTransactionScreen(navController, viewModel)
                }
                composable("edit_transaction/{transactionId}") { backStackEntry ->
                    val transactionId = backStackEntry.arguments?.getString("transactionId")?.toIntOrNull()
                    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
                    val transaction = transactions.find { it.id == transactionId }
                    transaction?.let {
                        EditTransactionScreen(navController, viewModel, it)
                    }
                }
                composable("profile") {
                    ProfileScreen(navController, onProfileUpdate = reloadProfileData)
                }
            }
        }
    }
}

@Composable
fun AppDrawerContent(
    profileData: ProfileData,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    ModalDrawerSheet {
        // Profile header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onProfileClick() }
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = profileData.pictureUri,
                contentDescription = "Profile Picture",
                placeholder = rememberVectorPainter(Icons.Default.AccountCircle),
                error = rememberVectorPainter(Icons.Default.AccountCircle),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(CircleShape)
            )
            Spacer(Modifier.height(8.dp))
            Text(profileData.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(
                profileData.email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        // Main nav items
        drawerMainItems.forEach { dest ->
            NavigationDrawerItem(
                icon = { Icon(dest.icon, contentDescription = null) },
                label = { Text(dest.label) },
                selected = currentRoute == dest.route,
                onClick = { onItemClick(dest.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Tool items (Receipt Scanner, Settings)
        drawerToolItems.forEach { dest ->
            NavigationDrawerItem(
                icon = { Icon(dest.icon, contentDescription = null) },
                label = { Text(dest.label) },
                selected = currentRoute == dest.route,
                onClick = { onItemClick(dest.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}