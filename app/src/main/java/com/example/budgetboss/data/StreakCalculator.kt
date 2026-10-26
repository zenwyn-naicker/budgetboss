package com.example.budgetboss.data

import com.example.budgetboss.core.util.parseIsoDate
import java.time.LocalDate

fun calculateCurrentStreak(
    expenses: List<ExpenseEntity>,
    today: LocalDate = LocalDate.now(),
): Int {
    if (expenses.isEmpty()) return 0

    val distinctDates = expenses.map { parseIsoDate(it.expenseDate) }.distinct().sortedDescending()
    var cursor = today
    if (distinctDates.firstOrNull() == today.minusDays(1)) {
        cursor = today.minusDays(1)
    }

    var streak = 0
    while (cursor in distinctDates) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
}

