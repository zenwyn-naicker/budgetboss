package com.example.budgetboss.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budgetboss.presentation.BudgetBossUiState
import com.example.budgetboss.presentation.BudgetBossViewModel
import com.example.budgetboss.ui.components.BottomDestinationItem
import com.example.budgetboss.ui.components.BudgetBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetBossShell(
    state: BudgetBossUiState,
    viewModel: BudgetBossViewModel,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()
    val primaryRoutes = setOf(Routes.Dashboard, Routes.Progress, Routes.Graph, Routes.More)
    val selectedBottomRoute = when {
        currentRoute.startsWith(Routes.Progress) -> Routes.Progress
        currentRoute.startsWith(Routes.Graph) -> Routes.Graph
        currentRoute.startsWith(Routes.More) ||
            currentRoute.startsWith(Routes.Goals) ||
            currentRoute.startsWith(Routes.Achievements) ||
            currentRoute.startsWith(Routes.Alerts) ||
            currentRoute.startsWith(Routes.Recurring) ||
            currentRoute.startsWith(Routes.Categories) ||
            currentRoute.startsWith(Routes.Expenses) ||
            currentRoute.startsWith(Routes.ExpenseDetail) ||
            currentRoute.startsWith(Routes.ExpenseForm) ||
            currentRoute.startsWith(Routes.Reports) -> Routes.More
        else -> Routes.Dashboard
    }
    val title = when {
        currentRoute.startsWith(Routes.ExpenseDetail) -> "Expense Detail"
        currentRoute.startsWith(Routes.ExpenseForm) -> "Add Expense"
        currentRoute.startsWith(Routes.Expenses) -> "Expenses"
        currentRoute.startsWith(Routes.Categories) -> "Categories"
        currentRoute.startsWith(Routes.Goals) -> "Budget Goals"
        currentRoute.startsWith(Routes.Reports) -> "Category Totals"
        currentRoute.startsWith(Routes.Graph) -> "Spending Graph"
        currentRoute.startsWith(Routes.Progress) -> "Progress Dashboard"
        currentRoute.startsWith(Routes.Achievements) -> "Achievements"
        currentRoute.startsWith(Routes.Alerts) -> "Smart Alerts"
        currentRoute.startsWith(Routes.Recurring) -> "Recurring Templates"
        currentRoute.startsWith(Routes.More) -> "More"
        else -> "Home"
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    val bottomDestinations = listOf(
        BottomDestinationItem(Routes.Dashboard, "Home", { Icon(Icons.Default.Dashboard, contentDescription = null) }),
        BottomDestinationItem(Routes.Progress, "Progress", { Icon(Icons.Default.Analytics, contentDescription = null) }),
        BottomDestinationItem(Routes.Graph, "Spending", { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null) }),
        BottomDestinationItem(Routes.More, "More", { Icon(Icons.Default.MoreHoriz, contentDescription = null) }),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                navigationIcon = {
                    if (selectedBottomRoute !in primaryRoutes || currentRoute !in primaryRoutes) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute.startsWith(Routes.More)) {
                        IconButton(onClick = viewModel::logout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (currentRoute.startsWith(Routes.Dashboard)) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.expenseForm()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add expense")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BudgetBottomBar(
                items = bottomDestinations,
                selectedRoute = selectedBottomRoute,
                onSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.Dashboard) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { paddingValues ->
        BudgetBossNavHost(
            navController = navController,
            state = state,
            viewModel = viewModel,
            contentPadding = paddingValues,
        )
    }
}

