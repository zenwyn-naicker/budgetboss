package com.example.budgetboss.feature.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetboss.ui.components.ItemCardShape
import com.example.budgetboss.ui.components.SubtleBorder

@Composable
fun MoreScreen(
    onOpenExpenses: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenRecurring: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val manageColor = MaterialTheme.colorScheme.primaryContainer
        val smartColor = MaterialTheme.colorScheme.secondaryContainer

        Text("Manage", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ActionCard("Expenses", "Browse and edit logged spending.", Icons.AutoMirrored.Filled.ReceiptLong, onOpenExpenses, manageColor)
        ActionCard("Categories", "Adjust your spending buckets and colors.", Icons.Default.Category, onOpenCategories, manageColor)
        ActionCard("Budget Goals", "Update your monthly target range.", Icons.Default.Savings, onOpenGoals, manageColor)

        Text("Smart features", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ActionCard("Smart Alerts", "Control overspending warnings.", Icons.Default.Notifications, onOpenAlerts, smartColor)
        ActionCard("Recurring Templates", "Reuse repeated expenses faster.", Icons.Default.AutoAwesome, onOpenRecurring, smartColor)
        ActionCard("Achievements", "Track streaks, badges, and XP.", Icons.Default.EmojiEvents, onOpenAchievements, smartColor)
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = ItemCardShape,
        border = SubtleBorder(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null)
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

