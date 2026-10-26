/*
 * Colour helper functions for BudgetBoss UI.
 * It safely converts stored colour hex values into Compose colours without crashing on invalid values.
 */
package com.example.budgetboss.ui.components

import androidx.compose.ui.graphics.Color

fun parseColorSafe(raw: String?): Color {
    return try {
        if (raw.isNullOrBlank()) {
            Color(0xFF64748B)
        } else {
            Color(android.graphics.Color.parseColor(raw))
        }
    } catch (_: IllegalArgumentException) {
        Color(0xFF64748B)
    }
}


