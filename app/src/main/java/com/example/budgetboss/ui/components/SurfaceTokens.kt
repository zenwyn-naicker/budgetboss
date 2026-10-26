/*
 * Shared surface styling values for BudgetBoss.
 * It keeps common card shapes, borders, and layout styling consistent across the app.
 */
package com.example.budgetboss.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val SubtleBorder: @Composable () -> BorderStroke = {
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
}

val ItemCardShape = RoundedCornerShape(20.dp)

val FeatureCardShape = RoundedCornerShape(24.dp)


