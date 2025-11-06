// ui/screens/MainScreen.kt
package com.mak7chek.carexpenses.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mak7chek.carexpenses.ui.navigation.BottomNavItem
import com.mak7chek.carexpenses.ui.screens.settings.SettingsScreen
import com.mak7chek.carexpenses.ui.screens.vehicles.VehiclesScreen

@Composable
fun MainScreen(navController: NavHostController,onNavigateToAuth: () -> Unit) {
    val nestedNavController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem.Journal,
        BottomNavItem.Map,
        BottomNavItem.Vehicles,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,

                        onClick = {
                            nestedNavController.navigate(screen.route) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(painter = painterResource(id = screen.iconResId),
                            contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = BottomNavItem.Journal.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Journal.route) { JournalScreen() }
            composable(BottomNavItem.Map.route) {  }
            composable(BottomNavItem.Vehicles.route) { VehiclesScreen(navController = navController)}
                composable(BottomNavItem.Settings.route) {
                    SettingsScreen(onNavigateToAuth = onNavigateToAuth)
                }
        }
    }
}