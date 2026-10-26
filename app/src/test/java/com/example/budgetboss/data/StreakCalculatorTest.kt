package com.example.budgetboss.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {
    private val today: LocalDate = LocalDate.of(2026, 3, 11)

    @Test
    fun calculateCurrentStreak_returnsZero_whenThereAreNoExpenses() {
        assertEquals(0, calculateCurrentStreak(emptyList(), today))
    }

    @Test
    fun calculateCurrentStreak_countsConsecutiveDays_backFromToday() {
        val expenses = listOf(
            expenseOn("2026-03-11"),
            expenseOn("2026-03-10"),
            expenseOn("2026-03-09"),
            expenseOn("2026-03-05"),
        )

        assertEquals(3, calculateCurrentStreak(expenses, today))
    }

    @Test
    fun calculateCurrentStreak_startsFromYesterday_whenTodayHasNoExpense() {
        val expenses = listOf(
            expenseOn("2026-03-10"),
            expenseOn("2026-03-09"),
            expenseOn("2026-03-08"),
        )

        assertEquals(3, calculateCurrentStreak(expenses, today))
    }

    private fun expenseOn(date: String): ExpenseEntity = ExpenseEntity(
        userId = 1,
        categoryId = 1,
        amount = 10.0,
        expenseDate = date,
        startTime = "09:00",
        endTime = "09:30",
        description = "Test",
        receiptUri = null,
        isGeneratedFromTemplate = false,
        createdAt = date,
    )
}

