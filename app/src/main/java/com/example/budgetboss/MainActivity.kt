/*
 * Main Android entry point for BudgetBoss.
 * It starts the Compose UI and loads the main BudgetBoss app screen.
 */
package com.example.budgetboss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetboss.navigation.BudgetBossApp
import com.example.budgetboss.presentation.BudgetBossViewModel
import com.example.budgetboss.ui.theme.BudgetBossTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetBossTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val budgetViewModel: BudgetBossViewModel = viewModel()
                    BudgetBossApp(viewModel = budgetViewModel)
                }
            }
        }
    }
}

