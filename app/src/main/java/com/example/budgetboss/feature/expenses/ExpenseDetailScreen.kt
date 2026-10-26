/*
 * Expense detail screen for BudgetBoss.
 * It shows the full information for one selected expense and provides actions to edit or delete it.
 */
package com.example.budgetboss.feature.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.displayDate
import com.example.budgetboss.core.util.formatAmount
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.data.ExpenseEntity
import com.example.budgetboss.ui.components.EmptyStateCard
import com.example.budgetboss.ui.components.FeatureCardShape
import com.example.budgetboss.ui.components.LabeledValue
import com.example.budgetboss.ui.components.ReceiptPreview
import com.example.budgetboss.ui.components.ReceiptViewerDialog
import com.example.budgetboss.ui.components.SubtleBorder
import com.example.budgetboss.ui.components.parseColorSafe

@Composable
fun ExpenseDetailScreen(
    expense: ExpenseEntity?,
    category: CategoryEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    contentPadding: PaddingValues,
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var previewReceiptUri by rememberSaveable { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (expense == null) {
            EmptyStateCard("That expense could not be found.", icon = Icons.Default.Warning)
        } else {
            Card(
                shape = FeatureCardShape,
                border = SubtleBorder(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("R${formatAmount(expense.amount)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                    AssistChip(
                        onClick = {},
                        label = { Text(category?.name ?: "Unknown category") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(parseColorSafe(category?.colorHex), CircleShape),
                            )
                        },
                    )
                    LabeledValue("Date", displayDate(expense.expenseDate))
                    LabeledValue("Time", "${expense.startTime} - ${expense.endTime}")
                    LabeledValue("Description", expense.description)
                    ReceiptPreview(
                        receiptUri = expense.receiptUri,
                        emptyMessage = "No receipt attached for this expense.",
                        onPreviewClick = expense.receiptUri?.let { { previewReceiptUri = it } },
                    )
                    if (!expense.receiptUri.isNullOrBlank()) {
                        Text(
                            text = "Tap the receipt to view it full screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
                        OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.weight(1f)) { Text("Delete") }
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
            title = { Text("Delete expense?") },
            text = { Text("This action cannot be undone.") },
        )
    }
    if (previewReceiptUri != null) {
        ReceiptViewerDialog(
            receiptUri = previewReceiptUri!!,
            onDismiss = { previewReceiptUri = null },
        )
    }
}

