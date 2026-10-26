/*
 * Root Compose screen for BudgetBoss.
 * It checks the current authentication state and shows either the login screen or the main app shell.
 */
package com.example.budgetboss.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgetboss.feature.auth.AuthScreen
import com.example.budgetboss.presentation.BudgetBossViewModel

@Composable
fun BudgetBossApp(viewModel: BudgetBossViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (!state.isAuthenticated) {
        AuthScreen(
            statusMessage = state.statusMessage,
            onLogin = viewModel::login,
            onRegister = viewModel::register,
        )
    } else {
        BudgetBossShell(
            state = state,
            viewModel = viewModel,
        )
    }
}

