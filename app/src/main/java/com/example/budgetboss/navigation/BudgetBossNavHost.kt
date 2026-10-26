package com.example.budgetboss.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.budgetboss.feature.dashboard.DashboardScreen
import com.example.budgetboss.feature.expenses.ExpenseDetailScreen
import com.example.budgetboss.feature.expenses.ExpenseFormScreen
import com.example.budgetboss.feature.expenses.ExpenseListScreen
import com.example.budgetboss.feature.insights.AchievementsScreen
import com.example.budgetboss.feature.insights.GraphScreen
import com.example.budgetboss.feature.insights.ProgressScreen
import com.example.budgetboss.feature.insights.ReportsScreen
import com.example.budgetboss.feature.management.CategoriesScreen
import com.example.budgetboss.feature.management.GoalsScreen
import com.example.budgetboss.feature.more.MoreScreen
import com.example.budgetboss.feature.settings.AlertsScreen
import com.example.budgetboss.feature.settings.RecurringTemplatesScreen
import com.example.budgetboss.presentation.BudgetBossUiState
import com.example.budgetboss.presentation.BudgetBossViewModel

@Composable
fun BudgetBossNavHost(
    navController: NavHostController,
    state: BudgetBossUiState,
    viewModel: BudgetBossViewModel,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard,
    ) {
        composable(Routes.Dashboard) {
            DashboardScreen(
                state = state,
                onOpenAchievements = { navController.navigate(Routes.Achievements) },
                contentPadding = contentPadding,
            )
        }
        composable(
            route = "${Routes.Expenses}?categoryId={categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType; defaultValue = -1L }),
        ) { entry ->
            val categoryId = entry.arguments?.getLong("categoryId")?.takeIf { it >= 0 }
            ExpenseListScreen(
                state = state,
                initialCategoryId = categoryId,
                onExpenseSelected = { navController.navigate(Routes.expenseDetail(it)) },
                onAddExpense = { navController.navigate(Routes.expenseForm()) },
                contentPadding = contentPadding,
            )
        }
        composable(
            route = "${Routes.ExpenseDetail}/{expenseId}",
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType }),
        ) { entry ->
            val expenseId = entry.arguments?.getLong("expenseId") ?: return@composable
            ExpenseDetailScreen(
                expense = viewModel.expenseById(expenseId),
                category = viewModel.expenseById(expenseId)?.let { viewModel.categoryById(it.categoryId) },
                onEdit = { navController.navigate(Routes.expenseForm(expenseId)) },
                onDelete = {
                    viewModel.expenseById(expenseId)?.let(viewModel::deleteExpense)
                    navController.popBackStack()
                },
                contentPadding = contentPadding,
            )
        }
        composable(
            route = "${Routes.ExpenseForm}?expenseId={expenseId}",
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType; defaultValue = -1L }),
        ) { entry ->
            val expenseId = entry.arguments?.getLong("expenseId")?.takeIf { it >= 0 }
            ExpenseFormScreen(
                existingExpense = expenseId?.let(viewModel::expenseById),
                categories = state.categories,
                currentUserId = state.currentUser?.id ?: 0,
                onSave = {
                    viewModel.saveExpense(it)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                contentPadding = contentPadding,
            )
        }
        composable(Routes.Categories) {
            CategoriesScreen(
                state = state,
                onSave = viewModel::saveCategory,
                onDelete = viewModel::deleteCategory,
                contentPadding = contentPadding,
            )
        }
        composable(Routes.Goals) {
            GoalsScreen(
                goal = state.budgetGoal,
                onSave = viewModel::saveBudgetGoals,
                contentPadding = contentPadding,
            )
        }
        composable(Routes.Reports) {
            ReportsScreen(
                state = state,
                onOpenCategoryExpenses = { navController.navigate(Routes.expenseList(it)) },
                contentPadding = contentPadding,
            )
        }
        composable(Routes.Graph) {
            GraphScreen(
                state = state,
                contentPadding = contentPadding,
            )
        }
        composable(Routes.Progress) {
            ProgressScreen(
                state = state,
                onOpenReports = { navController.navigate(Routes.Reports) },
                contentPadding = contentPadding,
            )
        }
        composable(Routes.Achievements) {
            AchievementsScreen(
                state = state,
                contentPadding = contentPadding,
            )
        }
        composable(Routes.Alerts) {
            AlertsScreen(
                currentUserId = state.currentUser?.id ?: 0,
                settings = state.alertSettings,
                onSave = viewModel::saveAlertSettings,
                contentPadding = contentPadding,
            )
        }
        composable(Routes.Recurring) {
            RecurringTemplatesScreen(
                currentUserId = state.currentUser?.id ?: 0,
                categories = state.categories,
                templates = state.recurringTemplates,
                onSave = viewModel::saveRecurringTemplate,
                onDelete = viewModel::deleteRecurringTemplate,
                contentPadding = contentPadding,
            )
        }
        composable(Routes.More) {
            MoreScreen(
                onOpenExpenses = { navController.navigate(Routes.Expenses) },
                onOpenCategories = { navController.navigate(Routes.Categories) },
                onOpenGoals = { navController.navigate(Routes.Goals) },
                onOpenAchievements = { navController.navigate(Routes.Achievements) },
                onOpenAlerts = { navController.navigate(Routes.Alerts) },
                onOpenRecurring = { navController.navigate(Routes.Recurring) },
                contentPadding = contentPadding,
            )
        }
    }
}

