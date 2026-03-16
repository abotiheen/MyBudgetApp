package com.example.mybudgetapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetapp.ui.navigation.AppNavHost
import com.example.mybudgetapp.ui.screens.CloudBackupDestination
import com.example.mybudgetapp.ui.screens.ThisMonthDestination
import com.example.mybudgetapp.ui.screens.ThisYearDestination
import com.example.mybudgetapp.ui.widgets.BottomNavigationBar

@Composable
fun MyBudgetApp (
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val rootRoutes = setOf(
        ThisYearDestination.route,
        ThisMonthDestination.route,
        CloudBackupDestination.route,
    )

    fun navigateToRoot(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in rootRoutes) {
                BottomNavigationBar(
                    navigateToThisMonthScreen = { navigateToRoot(ThisMonthDestination.route) },
                    navigateToThisYearScreen = { navigateToRoot(ThisYearDestination.route) },
                    navigateToCloudBackupScreen = { navigateToRoot(CloudBackupDestination.route) },
                    selectedItemIndex = when (currentRoute) {
                        ThisYearDestination.route -> 0
                        ThisMonthDestination.route -> 1
                        CloudBackupDestination.route -> 2
                        else -> 1
                    },
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
