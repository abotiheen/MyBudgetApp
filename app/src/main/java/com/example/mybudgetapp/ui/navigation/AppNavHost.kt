package com.example.mybudgetapp.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mybudgetapp.ui.screens.AddingItem
import com.example.mybudgetapp.ui.screens.AddingItemDestination
import com.example.mybudgetapp.ui.screens.CloudBackupDestination
import com.example.mybudgetapp.ui.screens.CloudBackupScreen
import com.example.mybudgetapp.ui.screens.ItemDatesScreen
import com.example.mybudgetapp.ui.screens.ItemDatesScreenNavigationDestination
import com.example.mybudgetapp.ui.screens.InsightsDestination
import com.example.mybudgetapp.ui.screens.InsightsScreen
import com.example.mybudgetapp.ui.screens.SpendingOnCategoryDestination
import com.example.mybudgetapp.ui.screens.SpendingOnCategoryForYearDestination
import com.example.mybudgetapp.ui.screens.SpendingOnCategoryScreen
import com.example.mybudgetapp.ui.screens.SpendingOnCategoryScreenForYear
import com.example.mybudgetapp.ui.screens.ThisMonthDestination
import com.example.mybudgetapp.ui.screens.ThisMonthScreen
import com.example.mybudgetapp.ui.screens.ThisYearDestination
import com.example.mybudgetapp.ui.screens.ThisYearScreen
import com.example.mybudgetapp.ui.screens.TotalIncomeDestination
import com.example.mybudgetapp.ui.screens.TotalIncomeDestinationForYear
import com.example.mybudgetapp.ui.screens.TotalIncomeScreen
import com.example.mybudgetapp.ui.screens.TotalIncomeScreenForYear

@Composable
fun AppNavHost (
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController ,
        startDestination = ThisMonthDestination.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(durationMillis = 180))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 140))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(durationMillis = 180))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 140))
        },
    ) {
        composable(ThisMonthDestination.route) {
            ThisMonthScreen(
                navigateToSpendingOnCategory = { category, month, year ->
                    navController.navigate(
                        "${SpendingOnCategoryDestination.route}/$category/$month/$year"
                    )
                                               },
                navigateToTotalIncome = { month, year, isIncome ->
                    navController.navigate(
                        "${TotalIncomeDestination.route}/$month/$year/$isIncome"
                    )
                },
                navigateToInsights = { month, year ->
                    navController.navigate(
                        "${InsightsDestination.route}/month/$year/$month"
                    )
                }
            )
        }

        composable(ThisYearDestination.route) {
            ThisYearScreen(
                navigateToInsights = { year ->
                    navController.navigate(
                        "${InsightsDestination.route}/year/$year/0"
                    )
                },
                navigateToSpendingOnCategoryForYear = { category, year ->
                    navController.navigate(
                        "${SpendingOnCategoryForYearDestination.route}/$category/$year"
                    )
                },
                navigateToTotalIncomeForYear = { year, isIncome ->
                    navController.navigate(
                        "${TotalIncomeDestinationForYear.route}/$year/$isIncome"
                    )
                }
            )
        }
        composable(CloudBackupDestination.route) {
            CloudBackupScreen()
        }
        composable(
            route = InsightsDestination.routeWithArgs,
            arguments = listOf(
                navArgument(InsightsDestination.scope) {
                    type = NavType.StringType
                },
                navArgument(InsightsDestination.year) {
                    type = NavType.IntType
                },
                navArgument(InsightsDestination.month) {
                    type = NavType.IntType
                },
            )
        ) {
            val scope = checkNotNull(it.arguments?.getString(InsightsDestination.scope))
            val year = checkNotNull(it.arguments?.getInt(InsightsDestination.year))
            val month = checkNotNull(it.arguments?.getInt(InsightsDestination.month))
            InsightsScreen(
                navigateBack = { navController.navigateUp() },
                navigateToSpendingOnCategory = { category ->
                    if (scope == "year") {
                        navController.navigate(
                            "${SpendingOnCategoryForYearDestination.route}/$category/$year"
                        )
                    } else {
                        navController.navigate(
                            "${SpendingOnCategoryDestination.route}/$category/$month/$year"
                        )
                    }
                }
            )
        }
        composable(
            route = AddingItemDestination.routeWithArgs,
            arguments = listOf(
                navArgument(AddingItemDestination.category) {
                    type = NavType.StringType
                }
            )
        ) {
            AddingItem(
                navigateBack = {navController.navigateUp()}
            )
        }
        composable(
            route = SpendingOnCategoryForYearDestination.routeWithArgs,
            arguments = listOf(
                navArgument(SpendingOnCategoryForYearDestination.category) {
                    type = NavType.StringType
                },
                navArgument(SpendingOnCategoryForYearDestination.year.toString()) {
                    type = NavType.IntType
                },
            )
        ) {
            SpendingOnCategoryScreenForYear(
                navigateBack = {navController.navigateUp()},
                navigateToAddItem = {
                    navController.navigate(
                        "${AddingItemDestination.route}/$it"
                    )
                } ,
                navigateToItemDates = {
                    navController.navigate(
                        "${ItemDatesScreenNavigationDestination.route}/$it"
                    )
                }
                 ,
            )
        }
        composable(
            route = SpendingOnCategoryDestination.routeWithArgs,
            arguments = listOf(
                navArgument(SpendingOnCategoryDestination.category) {
                    type = NavType.StringType
                },
                navArgument(SpendingOnCategoryDestination.month) {
                    type = NavType.IntType
                },
                navArgument(SpendingOnCategoryDestination.year) {
                    type = NavType.IntType
                }
            )
        ) {
            SpendingOnCategoryScreen(
                navigateToAddItem = { category ->
                    navController.navigate(
                        "${AddingItemDestination.route}/$category"
                    )
                                    },
                navigateBack = {navController.navigateUp()},
                navigateToItemDates = {
                    navController.navigate(
                        "${ItemDatesScreenNavigationDestination.route}/$it"
                    )
                }
            )
        }
        composable(
            route = TotalIncomeDestination.routeWithArgs,
            arguments = listOf(
                navArgument((TotalIncomeDestination.month)){
                    type = NavType.IntType
                },
                navArgument(TotalIncomeDestination.year) {
                    type = NavType.IntType
                },
                navArgument(TotalIncomeDestination.isIncome){
                    type = NavType.BoolType
                }
            )
        ) {
            TotalIncomeScreen(
                navigateBack = {navController.popBackStack()},
                navigateToAddItem = { category ->
                    navController.navigate(
                        "${AddingItemDestination.route}/$category"
                    )
                },
                navigateToItemDates = {
                    navController.navigate(
                        "${ItemDatesScreenNavigationDestination.route}/$it"
                    )
                }
            )
        }

        composable(
            route = TotalIncomeDestinationForYear.routeWithArgs,
            arguments = listOf(
                navArgument((TotalIncomeDestinationForYear.year.toString())){
                    type = NavType.IntType
                },
                navArgument((TotalIncomeDestinationForYear.isIncome.toString())){
                    type = NavType.BoolType
                }
            )
        ) {
            TotalIncomeScreenForYear(
                navigateBack = {navController.popBackStack()},
                navigateToAddItem = { category ->
                    navController.navigate(
                        "${AddingItemDestination.route}/$category"
                    )
                },
                navigateToItemDates = {
                    navController.navigate(
                        "${ItemDatesScreenNavigationDestination.route}/$it"
                    )
                }
            )
        }

        composable(
            route = ItemDatesScreenNavigationDestination.routeWithArgs,
            arguments = listOf(
                navArgument(ItemDatesScreenNavigationDestination.id.toString()){
                    type = NavType.LongType
                },
            )
        ) {
            ItemDatesScreen(
                navigateBack = {navController.navigateUp()}
            )
        }
    }
}
