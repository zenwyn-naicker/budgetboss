/*
 * Add and edit expense screen for BudgetBoss.
 * It collects expense details, validates the form, handles receipt photo selection, and sends valid expenses to be saved.
 */
package com.example.budgetboss.feature.expenses

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.ISO_DATE_FORMATTER
import com.example.budgetboss.core.util.parseIsoDate
import com.example.budgetboss.core.util.todayIso
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.data.ExpenseEntity
import com.example.budgetboss.ui.components.CategoryDropdownField
import com.example.budgetboss.ui.components.DatePickerField
import com.example.budgetboss.ui.components.EmptyStateCard
import com.example.budgetboss.ui.components.ReceiptPreview
import com.example.budgetboss.ui.components.ReceiptViewerDialog
import com.example.budgetboss.ui.components.TimePickerField
import java.time.LocalTime

@Composable
fun ExpenseFormScreen(
    existingExpense: ExpenseEntity?,
    categories: List<CategoryEntity>,
    currentUserId: Long,
    onSave: (ExpenseEntity) -> Unit,
    onCancel: () -> Unit,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    var amount by rememberSaveable(existingExpense?.id) { mutableStateOf(existingExpense?.amount?.toString().orEmpty()) }
    var date by rememberSaveable(existingExpense?.id) { mutableStateOf(existingExpense?.expenseDate ?: todayIso()) }
    var startTime by rememberSaveable(existingExpense?.id) { mutableStateOf(existingExpense?.startTime ?: "09:00") }
    var endTime by rememberSaveable(existingExpense?.id) { mutableStateOf(existingExpense?.endTime ?: "09:30") }
    var description by rememberSaveable(existingExpense?.id) { mutableStateOf(existingExpense?.description.orEmpty()) }
    var selectedCategoryId by rememberSaveable(existingExpense?.id) {
        mutableStateOf(existingExpense?.categoryId ?: categories.firstOrNull()?.id ?: 0L)
    }
    var receiptUri by rememberSaveable(existingExpense?.id) { mutableStateOf(existingExpense?.receiptUri) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var dateError by rememberSaveable { mutableStateOf<String?>(null) }
    var startTimeError by rememberSaveable { mutableStateOf<String?>(null) }
    var endTimeError by rememberSaveable { mutableStateOf<String?>(null) }
    var descriptionError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var previewReceiptUri by rememberSaveable { mutableStateOf<String?>(null) }
    // Launches the camera and stores the new receipt photo URI when capture succeeds.
    val cameraCapture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            receiptUri = pendingCameraUri
        }
        pendingCameraUri = null
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
            }
            receiptUri = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (categories.isEmpty()) {
            EmptyStateCard("Create a category first before adding expenses.", actionLabel = "Back", onAction = onCancel, icon = Icons.Default.Category)
            return@Column
        }
        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                amountError = null
            },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountError != null,
            supportingText = {
                if (amountError != null) {
                    Text(amountError!!)
                }
            },
        )
        DatePickerField(
            label = "Date",
            isoDate = date,
            onDateSelected = {
                date = it
                dateError = null
            },
            errorMessage = dateError,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimePickerField(
                label = "Start",
                time = startTime,
                onTimeSelected = {
                    startTime = it
                    startTimeError = null
                },
                errorMessage = startTimeError,
                modifier = Modifier.weight(1f),
            )
            TimePickerField(
                label = "End",
                time = endTime,
                onTimeSelected = {
                    endTime = it
                    endTimeError = null
                },
                errorMessage = endTimeError,
                modifier = Modifier.weight(1f),
            )
        }
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    val newCaptureUri = createReceiptCaptureUri(context)
                    pendingCameraUri = newCaptureUri.toString()
                    cameraCapture.launch(newCaptureUri)
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Take Photo")
            }
            OutlinedButton(
                onClick = { photoPicker.launch(arrayOf("image/*")) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Choose Photo")
            }
        }
        ReceiptPreview(
            receiptUri = receiptUri,
            onPreviewClick = receiptUri?.let { { previewReceiptUri = it } },
        )
        if (receiptUri != null) {
            TextButton(onClick = { receiptUri = null }) { Text("Remove receipt") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = {
                    // Validates the expense form before saving to prevent invalid data or crashes.
                    val parsedAmount = amount.toDoubleOrNull()
                    val parsedDate = runCatching { parseIsoDate(date) }.getOrNull()
                    val parsedStartTime = runCatching { LocalTime.parse(startTime) }.getOrNull()
                    val parsedEndTime = runCatching { LocalTime.parse(endTime) }.getOrNull()

                    amountError = when {
                        amount.isBlank() -> "Amount is required."
                        parsedAmount == null || parsedAmount <= 0.0 -> "Enter a positive amount."
                        else -> null
                    }
                    dateError = if (parsedDate == null) "Use a valid date." else null
                    startTimeError = if (parsedStartTime == null) "Use a valid 24-hour start time." else null
                    endTimeError = when {
                        parsedEndTime == null -> "Use a valid 24-hour end time."
                        parsedStartTime != null && !parsedEndTime.isAfter(parsedStartTime) -> "End time must be later than start time."
                        else -> null
                    }
                    descriptionError = if (description.isBlank()) "Description is required." else null
                    categoryError = if (selectedCategoryId == 0L) "Select a category." else null

                    if (
                        amountError == null &&
                        dateError == null &&
                        startTimeError == null &&
                        endTimeError == null &&
                        descriptionError == null &&
                        categoryError == null
                    ) {
                        onSave(
                            ExpenseEntity(
                                id = existingExpense?.id ?: 0,
                                userId = currentUserId,
                                categoryId = selectedCategoryId,
                                amount = parsedAmount!!,
                                expenseDate = parsedDate!!.format(ISO_DATE_FORMATTER),
                                startTime = startTime,
                                endTime = endTime,
                                description = description.trim(),
                                receiptUri = receiptUri,
                                isGeneratedFromTemplate = existingExpense?.isGeneratedFromTemplate ?: false,
                                createdAt = existingExpense?.createdAt ?: todayIso(),
                            ),
                        )
                    }
                },
                modifier = Modifier.weight(1f),
            ) { Text("Save Expense") }
        }
    }
    if (previewReceiptUri != null) {
        ReceiptViewerDialog(
            receiptUri = previewReceiptUri!!,
            onDismiss = { previewReceiptUri = null },
        )
    }
}

