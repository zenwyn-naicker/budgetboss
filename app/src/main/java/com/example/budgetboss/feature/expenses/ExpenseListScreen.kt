/*
 * Expense history screen for BudgetBoss.
 * It filters expenses by date and category, sorts the list, and allows receipt photos to be opened from saved entries.
 */
package com.example.budgetboss.feature.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.displayDate
import com.example.budgetboss.core.util.formatAmount
import com.example.budgetboss.core.util.parseIsoDate
import com.example.budgetboss.core.util.startOfMonthIso
import com.example.budgetboss.core.util.todayIso
import com.example.budgetboss.data.ExpenseEntity
import com.example.budgetboss.presentation.BudgetBossUiState
import com.example.budgetboss.presentation.ExpenseSortOption
import com.example.budgetboss.ui.components.CategoryDropdownField
import com.example.budgetboss.ui.components.DatePickerField
import com.example.budgetboss.ui.components.EmptyStateCard
import com.example.budgetboss.ui.components.ItemCardShape
import com.example.budgetboss.ui.components.ReceiptViewerDialog
import com.example.budgetboss.ui.components.SubtleBorder
import com.example.budgetboss.ui.components.parseColorSafe

@Composable
fun ExpenseListScreen(
    state: BudgetBossUiState,
    initialCategoryId: Long?,
    onExpenseSelected: (Long) -> Unit,
    onAddExpense: () -> Unit,
    contentPadding: PaddingValues,
) {
    var startDate by rememberSaveable { mutableStateOf(startOfMonthIso()) }
    var endDate by rememberSaveable { mutableStateOf(todayIso()) }
    var sortOption by rememberSaveable { mutableStateOf(ExpenseSortOption.DATE_DESC.name) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(initialCategoryId ?: -1L) }
    var previewReceiptUri by rememberSaveable { mutableStateOf<String?>(null) }

    // Filters expenses by date range and category, then sorts them for display.
    val filtered = state.expenses
        .filter { expense ->
            val date = parseIsoDate(expense.expenseDate)
            val start = runCatching { parseIsoDate(startDate) }.getOrDefault(java.time.LocalDate.MIN)
            val end = runCatching { parseIsoDate(endDate) }.getOrDefault(java.time.LocalDate.MAX)
            val categoryMatches = selectedCategoryId < 0 || expense.categoryId == selectedCategoryId
            !date.isBefore(start) && !date.isAfter(end) && categoryMatches
        }
        .sortedWith(
            when (ExpenseSortOption.valueOf(sortOption)) {
                ExpenseSortOption.DATE_DESC -> compareByDescending<ExpenseEntity> { it.expenseDate }.thenByDescending { it.startTime }
                ExpenseSortOption.AMOUNT_DESC -> compareByDescending<ExpenseEntity> { it.amount }
                ExpenseSortOption.CATEGORY_ASC -> compareBy<ExpenseEntity> {
                    state.categories.firstOrNull { category -> category.id == it.categoryId }?.name
                }
            },
        )

    LazyColumn(
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DatePickerField(label = "Start date", isoDate = startDate, onDateSelected = { startDate = it }, modifier = Modifier.weight(1f))
                DatePickerField(label = "End date", isoDate = endDate, onDateSelected = { endDate = it }, modifier = Modifier.weight(1f))
            }
        }
        item {
            CategoryDropdownField(
                label = "Filter by category",
                categories = state.categories,
                selectedCategoryId = selectedCategoryId.takeIf { it >= 0 },
                onCategorySelected = { selectedCategoryId = it },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = sortOption == ExpenseSortOption.DATE_DESC.name, onClick = { sortOption = ExpenseSortOption.DATE_DESC.name }, label = { Text("Date") })
                FilterChip(selected = sortOption == ExpenseSortOption.AMOUNT_DESC.name, onClick = { sortOption = ExpenseSortOption.AMOUNT_DESC.name }, label = { Text("Amount") })
                FilterChip(selected = sortOption == ExpenseSortOption.CATEGORY_ASC.name, onClick = { sortOption = ExpenseSortOption.CATEGORY_ASC.name }, label = { Text("Category") })
            }
        }
        if (selectedCategoryId >= 0) {
            item {
                TextButton(onClick = { selectedCategoryId = -1L }) { Text("Clear category filter") }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateCard("No expenses match this period yet.", actionLabel = "Add expense", onAction = onAddExpense, icon = Icons.AutoMirrored.Filled.ReceiptLong)
            }
        } else {
            items(filtered, key = { it.id }) { expense ->
                val category = state.categories.firstOrNull { it.id == expense.categoryId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpenseSelected(expense.id) },
                    shape = ItemCardShape,
                    border = SubtleBorder(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(parseColorSafe(category?.colorHex), CircleShape),
                                )
                                Text(category?.name ?: "Unknown", style = MaterialTheme.typography.labelLarge)
                            }
                            Text(
                                text = "R${formatAmount(expense.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${displayDate(expense.expenseDate)} • ${expense.startTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (!expense.receiptUri.isNullOrBlank()) {
                                TextButton(
                                    onClick = { previewReceiptUri = expense.receiptUri },
                                ) {
                                    Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("View receipt", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (previewReceiptUri != null) {
        ReceiptViewerDialog(
            receiptUri = previewReceiptUri!!,
            onDismiss = { previewReceiptUri = null },
        )
    }
}

