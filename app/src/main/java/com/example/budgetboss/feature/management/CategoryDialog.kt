/*
 * Category form dialog for BudgetBoss.
 * It validates category details such as name and spending limit before creating or updating a category.
 */
package com.example.budgetboss.feature.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.ui.components.parseColorSafe

@Composable
fun CategoryDialog(
    initial: CategoryEntity?,
    userId: Long,
    existingCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit,
) {
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var colorHex by rememberSaveable(initial?.id) { mutableStateOf(initial?.colorHex ?: "#2563EB") }
    var iconName by rememberSaveable(initial?.id) { mutableStateOf(initial?.iconName ?: "Category") }
    var limit by rememberSaveable(initial?.id) { mutableStateOf(initial?.monthlyLimit?.toString().orEmpty()) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var limitError by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val normalizedName = name.trim()
                val parsedLimit = limit.toDoubleOrNull()
                nameError = when {
                    normalizedName.isBlank() -> "Category name is required."
                    existingCategories.any { it.id != initial?.id && it.name.equals(normalizedName, ignoreCase = true) } ->
                        "A category with this name already exists."
                    else -> null
                }
                limitError = when {
                    limit.isBlank() -> null
                    parsedLimit == null -> "Enter a valid number."
                    parsedLimit <= 0.0 -> "Limit must be a positive amount."
                    else -> null
                }

                if (nameError == null && limitError == null) {
                    onSave(
                        CategoryEntity(
                            id = initial?.id ?: 0,
                            userId = userId,
                            name = normalizedName,
                            colorHex = colorHex.trim(),
                            iconName = iconName.trim().ifBlank { "Category" },
                            monthlyLimit = parsedLimit,
                            isDefault = initial?.isDefault ?: false,
                        ),
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (initial == null) "Add category" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(nameError!!)
                        }
                    },
                )
                Text("Color", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ColorSwatchGrid(
                    selectedHex = colorHex,
                    onColorSelected = { colorHex = it },
                )
                OutlinedTextField(value = iconName, onValueChange = { iconName = it }, label = { Text("Icon name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = limit,
                    onValueChange = {
                        limit = it
                        limitError = null
                    },
                    label = { Text("Optional limit") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = limitError != null,
                    supportingText = {
                        if (limitError != null) {
                            Text(limitError!!)
                        }
                    },
                )
            }
        },
    )
}

private val presetColors = listOf(
    "#2563EB", "#0F6A5A", "#7C3AED", "#DB2777",
    "#DC2626", "#EA580C", "#D97706", "#CA8A04",
    "#16A34A", "#0D9488", "#0284C7", "#6366F1",
    "#64748B", "#78716C", "#334155", "#1E293B",
)

@Composable
private fun ColorSwatchGrid(
    selectedHex: String,
    onColorSelected: (String) -> Unit,
) {
    val columns = 8
    presetColors.chunked(columns).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { hex ->
                val isSelected = hex.equals(selectedHex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            parseColorSafe(hex),
                            CircleShape,
                        )
                        .then(
                            if (isSelected) {
                                Modifier
                                    .padding(2.dp)
                                    .background(Color.White, CircleShape)
                                    .padding(2.dp)
                                    .background(parseColorSafe(hex), CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onColorSelected(hex) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.White, CircleShape),
                        )
                    }
                }
            }
        }
    }
}

