/*
 * Currency formatting helpers for BudgetBoss.
 * It formats money values consistently so amounts display clearly throughout the app.
 */
package com.example.budgetboss.core.util

import java.text.NumberFormat
import java.util.Locale

private val amountFormatter: NumberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-ZA")).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

fun formatAmount(amount: Double): String = amountFormatter.format(amount)


