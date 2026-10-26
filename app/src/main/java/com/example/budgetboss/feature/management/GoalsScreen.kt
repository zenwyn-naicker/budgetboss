/*
 * Budget goal screen for BudgetBoss.
 * It lets the user set minimum and maximum monthly spending goals and validates the values before saving.
 */
package com.example.budgetboss.feature.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.formatAmount
import com.example.budgetboss.data.BudgetGoalEntity
import com.example.budgetboss.ui.components.FeatureCardShape
import com.example.budgetboss.ui.components.SubtleBorder

@Composable
fun GoalsScreen(
    goal: BudgetGoalEntity,
    onSave: (Double, Double) -> Unit,
    contentPadding: PaddingValues,
) {
    var minGoal by rememberSaveable(goal.userId, goal.updatedAt) { mutableStateOf(formatAmount(goal.minMonthlyGoal)) }
    var maxGoal by rememberSaveable(goal.userId, goal.updatedAt) { mutableStateOf(formatAmount(goal.maxMonthlyGoal)) }
    var minGoalError by rememberSaveable { mutableStateOf<String?>(null) }
    var maxGoalError by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = minGoal,
            onValueChange = {
                minGoal = it
                minGoalError = null
            },
            label = { Text("Minimum goal") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = minGoalError != null,
            supportingText = {
                if (minGoalError != null) {
                    Text(minGoalError!!)
                }
            },
        )
        OutlinedTextField(
            value = maxGoal,
            onValueChange = {
                maxGoal = it
                maxGoalError = null
            },
            label = { Text("Maximum goal") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = maxGoalError != null,
            supportingText = {
                if (maxGoalError != null) {
                    Text(maxGoalError!!)
                }
            },
        )
        Card(
            shape = FeatureCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = SubtleBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Spending range", fontWeight = FontWeight.Bold)
                Text("You aim to spend between R$minGoal and R$maxGoal this month.")
                LinearProgressIndicator(progress = { 0.65f }, modifier = Modifier.fillMaxWidth())
            }
        }
        Button(onClick = {
            // Validates the minimum and maximum monthly goals before saving.
            val min = minGoal.toDoubleOrNull()
            val max = maxGoal.toDoubleOrNull()
            minGoalError = when {
                minGoal.isBlank() -> "Minimum goal is required."
                min == null -> "Enter a valid number."
                min <= 0 -> "Minimum goal must be positive."
                max != null && min >= max -> "Minimum must be less than maximum."
                else -> null
            }
            maxGoalError = when {
                maxGoal.isBlank() -> "Maximum goal is required."
                max == null -> "Enter a valid number."
                max <= 0 -> "Maximum goal must be positive."
                min != null && max <= min -> "Maximum must be greater than minimum."
                else -> null
            }
            if (minGoalError == null && maxGoalError == null) onSave(min!!, max!!)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Goals")
        }
    }
}

