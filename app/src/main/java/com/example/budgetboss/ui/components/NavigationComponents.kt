/*
 * Shared navigation UI components for BudgetBoss.
 * It builds the bottom navigation bar used to move between Expenses, Categories, Goals, and Reports.
 */
package com.example.budgetboss.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class BottomDestinationItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@Composable
fun BudgetBottomBar(
    items: List<BottomDestinationItem>,
    selectedRoute: String,
    onSelected: (String) -> Unit,
) {
    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
        Card(
            shape = RoundedCornerShape(28.dp),
            border = SubtleBorder(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = item.route == selectedRoute,
                        onClick = { onSelected(item.route) },
                        icon = item.icon,
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}


