/*
 * Category management screen for BudgetBoss.
 * It displays all spending categories and allows users to add, edit, or delete categories.
 */
package com.example.budgetboss.feature.management

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.formatAmount
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.presentation.BudgetBossUiState
import com.example.budgetboss.ui.components.ItemCardShape
import com.example.budgetboss.ui.components.SubtleBorder
import com.example.budgetboss.ui.components.parseColorSafe

@Composable
fun CategoriesScreen(
    state: BudgetBossUiState,
    onSave: (CategoryEntity) -> Unit,
    onDelete: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Button(onClick = {
                editingCategory = null
                showDialog = true
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Add Category")
            }
        }
        items(state.categories, key = { it.id }) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        editingCategory = category
                        showDialog = true
                    },
                shape = ItemCardShape,
                border = SubtleBorder(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(parseColorSafe(category.colorHex), CircleShape),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(category.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = category.monthlyLimit?.let { "Limit: R${formatAmount(it)}" } ?: "No category limit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = {
                                editingCategory = category
                                showDialog = true
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit category", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { pendingDelete = category }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete category", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CategoryDialog(
            initial = editingCategory,
            userId = state.currentUser?.id ?: 0,
            existingCategories = state.categories,
            onDismiss = { showDialog = false },
            onSave = {
                onSave(it)
                showDialog = false
            },
        )
    }
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(pendingDelete!!.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
            title = { Text("Delete category?") },
            text = { Text("Linked expenses will move to Uncategorized.") },
        )
    }
}

