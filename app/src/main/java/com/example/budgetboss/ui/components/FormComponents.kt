/*
 * Shared form input components for BudgetBoss.
 * It provides reusable category, date, and time picker fields used across expense and reporting screens.
 */
package com.example.budgetboss.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.ISO_DATE_FORMATTER
import com.example.budgetboss.core.util.displayDate
import com.example.budgetboss.core.util.parseIsoDate
import com.example.budgetboss.data.CategoryEntity
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun CategoryDropdownField(
    label: String,
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedCategoryId }
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.size(6.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = SubtleBorder(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(
                        modifier = Modifier
                            .size(14.dp)
                            .background(parseColorSafe(selected?.colorHex), CircleShape),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selected?.name ?: "Select category")
                }
                Text("Change", color = MaterialTheme.colorScheme.primary)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(parseColorSafe(category.colorHex), CircleShape),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.name)
                        }
                    },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    },
                )
            }
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    isoDate: String,
    onDateSelected: (String) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val displayText = runCatching { displayDate(isoDate) }.getOrDefault(isoDate)

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier.clickable { showPicker = true },
        readOnly = true,
        singleLine = true,
        isError = errorMessage != null,
        trailingIcon = {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = "Pick date",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showPicker = true },
            )
        },
        supportingText = {
            if (errorMessage != null) {
                Text(errorMessage)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(),
    )

    if (showPicker) {
        val initial = runCatching {
            parseIsoDate(isoDate).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        }.getOrDefault(System.currentTimeMillis())
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initial)

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        onDateSelected(picked.format(ISO_DATE_FORMATTER))
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    time: String,
    onTimeSelected: (String) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val parsed = runCatching { LocalTime.parse(time) }.getOrDefault(LocalTime.of(9, 0))

    OutlinedTextField(
        value = time,
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier.clickable { showPicker = true },
        readOnly = true,
        singleLine = true,
        isError = errorMessage != null,
        trailingIcon = {
            Icon(
                Icons.Default.Schedule,
                contentDescription = "Pick time",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showPicker = true },
            )
        },
        supportingText = {
            if (errorMessage != null) {
                Text(errorMessage)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(),
    )

    if (showPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = parsed.hour,
            initialMinute = parsed.minute,
            is24Hour = true,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    onTimeSelected("$hour:$minute")
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
            title = { Text(label) },
            text = { TimePicker(state = timePickerState) },
        )
    }
}


