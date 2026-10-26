package com.example.budgetboss.feature.settings

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.formatAmount
import com.example.budgetboss.core.util.todayIso
import com.example.budgetboss.data.AlertSettingsEntity
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.data.RecurringTemplateEntity
import com.example.budgetboss.ui.components.CategoryDropdownField
import com.example.budgetboss.ui.components.DatePickerField
import com.example.budgetboss.ui.components.EmptyStateCard
import com.example.budgetboss.ui.components.FeatureCardShape
import com.example.budgetboss.ui.components.ItemCardShape
import com.example.budgetboss.ui.components.SubtleBorder

@Composable
fun AlertsScreen(
    currentUserId: Long,
    settings: AlertSettingsEntity,
    onSave: (AlertSettingsEntity) -> Unit,
    contentPadding: PaddingValues,
) {
    var enabled by rememberSaveable(settings.userId, settings.enabled) { mutableStateOf(settings.enabled) }
    var threshold by rememberSaveable(settings.userId, settings.thresholdPercent) {
        mutableStateOf(settings.thresholdPercent.toFloat())
    }
    var cutoffDay by rememberSaveable(settings.userId, settings.dayOfMonthCutoff) { mutableStateOf(settings.dayOfMonthCutoff.toString()) }
    var showNotification by rememberSaveable(settings.userId, settings.showNotification) { mutableStateOf(settings.showNotification) }
    var showBanner by rememberSaveable(settings.userId, settings.showBanner) { mutableStateOf(settings.showBanner) }
    var cutoffDayError by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            shape = FeatureCardShape,
            border = SubtleBorder(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable alerts", fontWeight = FontWeight.Bold)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Text("Alert threshold: ${(threshold * 100).toInt()}% of max budget")
                Slider(value = threshold, onValueChange = { threshold = it }, valueRange = 0.1f..1f)
                OutlinedTextField(
                    value = cutoffDay,
                    onValueChange = {
                        cutoffDay = it
                        cutoffDayError = null
                    },
                    label = { Text("Day of month cutoff") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = cutoffDayError != null,
                    supportingText = {
                        if (cutoffDayError != null) {
                            Text(cutoffDayError!!)
                        }
                    },
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("In-app notification")
                    Switch(checked = showNotification, onCheckedChange = { showNotification = it })
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Warning banner")
                    Switch(checked = showBanner, onCheckedChange = { showBanner = it })
                }
            }
        }
        Card(
            shape = ItemCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Text(
                "Notify me when I reach ${(threshold * 100).toInt()}% of my maximum budget before day ${cutoffDay.ifBlank { "20" }}.",
                modifier = Modifier.padding(16.dp),
            )
        }
        Button(onClick = {
            val parsedCutoff = cutoffDay.toIntOrNull()
            cutoffDayError = when {
                cutoffDay.isBlank() -> "Day of month cutoff is required."
                parsedCutoff == null -> "Enter a valid day number."
                parsedCutoff !in 1..31 -> "Use a day between 1 and 31."
                else -> null
            }
            if (cutoffDayError == null) {
                onSave(
                    AlertSettingsEntity(
                        userId = currentUserId,
                        enabled = enabled,
                        thresholdPercent = threshold.toDouble(),
                        dayOfMonthCutoff = parsedCutoff!!,
                        showNotification = showNotification,
                        showBanner = showBanner,
                    ),
                )
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Alerts")
        }
    }
}

@Composable
fun RecurringTemplatesScreen(
    currentUserId: Long,
    categories: List<CategoryEntity>,
    templates: List<RecurringTemplateEntity>,
    onSave: (RecurringTemplateEntity) -> Unit,
    onDelete: (RecurringTemplateEntity) -> Unit,
    contentPadding: PaddingValues,
) {
    var editingTemplate by remember { mutableStateOf<RecurringTemplateEntity?>(null) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<RecurringTemplateEntity?>(null) }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(onClick = {
            editingTemplate = null
            showDialog = true
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Template")
        }
        if (templates.isEmpty()) {
            EmptyStateCard("No recurring templates yet.", icon = Icons.Default.AutoAwesome)
        } else {
            templates.forEach { template ->
                val category = categories.firstOrNull { it.id == template.categoryId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editingTemplate = template
                            showDialog = true
                        },
                    shape = FeatureCardShape,
                    border = SubtleBorder(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(template.description, fontWeight = FontWeight.Bold)
                            Text("R${formatAmount(template.amount)}")
                        }
                        Text("${category?.name ?: "Unknown"} • ${template.frequency} • Next ${template.nextScheduledDate}")
                        Row {
                            IconButton(onClick = {
                                editingTemplate = template
                                showDialog = true
                            }) { Icon(Icons.Default.Edit, contentDescription = null) }
                            IconButton(onClick = { pendingDelete = template }) { Icon(Icons.Default.Delete, contentDescription = null) }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        RecurringTemplateDialog(
            initial = editingTemplate,
            userId = currentUserId,
            categories = categories,
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
                    onDelete(pendingDelete!!)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
            title = { Text("Delete template?") },
            text = { Text("This recurring template will stop generating expenses.") },
        )
    }
}

@Composable
private fun RecurringTemplateDialog(
    initial: RecurringTemplateEntity?,
    userId: Long,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (RecurringTemplateEntity) -> Unit,
) {
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var amount by rememberSaveable(initial?.id) { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var selectedCategoryId by rememberSaveable(initial?.id) {
        mutableLongStateOf(initial?.categoryId ?: categories.firstOrNull()?.id ?: 0L)
    }
    var frequency by rememberSaveable(initial?.id) { mutableStateOf(initial?.frequency ?: "Monthly") }
    var nextDate by rememberSaveable(initial?.id) { mutableStateOf(initial?.nextScheduledDate ?: todayIso()) }
    var active by rememberSaveable(initial?.id) { mutableStateOf(initial?.active ?: true) }
    var descriptionError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }
    val frequencies = listOf("Daily", "Weekly", "Monthly")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                descriptionError = if (description.trim().isBlank()) "Description is required." else null
                amountError = when {
                    amount.isBlank() -> "Amount is required."
                    parsedAmount == null -> "Enter a valid amount."
                    parsedAmount <= 0.0 -> "Amount must be positive."
                    else -> null
                }
                categoryError = if (selectedCategoryId == 0L) "Select a category." else null

                if (descriptionError == null && amountError == null && categoryError == null) {
                    onSave(
                        RecurringTemplateEntity(
                            id = initial?.id ?: 0,
                            userId = userId,
                            description = description.trim(),
                            amount = parsedAmount!!,
                            categoryId = selectedCategoryId,
                            frequency = frequency,
                            nextScheduledDate = nextDate,
                            active = active,
                        ),
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (initial == null) "Add template" else "Edit template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = null
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = descriptionError != null,
                    supportingText = {
                        if (descriptionError != null) {
                            Text(descriptionError!!)
                        }
                    },
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = null
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountError != null,
                    supportingText = {
                        if (amountError != null) {
                            Text(amountError!!)
                        }
                    },
                )
                CategoryDropdownField(
                    label = "Category",
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = {
                        selectedCategoryId = it
                        categoryError = null
                    },
                    errorMessage = categoryError,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    frequencies.forEach { option ->
                        FilterChip(selected = frequency == option, onClick = { frequency = option }, label = { Text(option) })
                    }
                }
                DatePickerField(label = "Next date", isoDate = nextDate, onDateSelected = { nextDate = it }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Template active")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
    )
}

