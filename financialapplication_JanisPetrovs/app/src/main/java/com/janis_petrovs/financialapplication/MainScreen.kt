package com.janis_petrovs.financialapplication

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
import com.janis_petrovs.financialapplication.data.ProfileData
import com.janis_petrovs.financialapplication.data.UserProfileManager
import com.janis_petrovs.financialapplication.ui.screens.*
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

sealed class DrawerScreen(val route: String, val label: String, val icon: ImageVector) {
    object Planning : DrawerScreen("planning", "Planning", Icons.Default.List)
    object History : DrawerScreen("history", "History", Icons.Default.History)
    object Reports : DrawerScreen("reports", "Reports", Icons.Default.Assessment)
}
val drawerItems = listOf(DrawerScreen.Reports, DrawerScreen.Planning, DrawerScreen.History)

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                profileData = profileData,
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("profile")
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
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
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
                composable(DrawerScreen.History.route) { HistoryScreen(viewModel) }
                composable(DrawerScreen.Reports.route) { ReportsScreen(viewModel) }
                composable("add_transaction") { AddTransactionScreen(navController, viewModel) }
                composable("profile") { ProfileScreen(navController, onProfileUpdate = reloadProfileData) }
            }
        }
    }
}

@Composable
fun AppDrawerContent(
    profileData: ProfileData,
    onItemClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    currentRoute: String?
) {
    ModalDrawerSheet {
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(profileData.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(profileData.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider()

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