package com.example.budgetboss.presentation

import com.example.budgetboss.data.AchievementEntity
import com.example.budgetboss.data.AlertSettingsEntity
import com.example.budgetboss.data.BudgetGoalEntity
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.data.ExpenseEntity
import com.example.budgetboss.data.RecurringTemplateEntity
import com.example.budgetboss.data.UserEntity
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetBossUiStateFactoryTest {
    private val now: LocalDate = LocalDate.of(2026, 3, 11)
    private val user = UserEntity(id = 1, username = "zenwyn", passwordHash = "hash", createdAt = "2026-03-01")
    private val foodCategory = CategoryEntity(
        id = 10,
        userId = 1,
        name = "Food",
        colorHex = "#15803D",
        iconName = "Restaurant",
        monthlyLimit = 50.0,
        isDefault = true,
    )

    @Test
    fun create_buildsAlertAndOverLimitWarnings_fromCurrentMonthData() {
        val state = BudgetBossUiStateFactory.create(
            user = user,
            categories = listOf(foodCategory),
            expenses = listOf(
                expense(amount = 80.0, expenseDate = "2026-03-05"),
            ),
            budgetGoal = BudgetGoalEntity(userId = 1, minMonthlyGoal = 20.0, maxMonthlyGoal = 100.0, updatedAt = "2026-03-01"),
            alertSettings = AlertSettingsEntity(userId = 1, enabled = true, thresholdPercent = 0.75, dayOfMonthCutoff = 20, showNotification = false, showBanner = true),
            recurringTemplates = emptyList(),
            achievements = emptyList(),
            statusMessage = "Saved",
            now = now,
        )

        assertEquals(80.0, state.dashboard.totalSpent, 0.0)
        assertNotNull(state.dashboard.smartAlertMessage)
        assertTrue(state.dashboard.smartAlertMessage!!.contains("75%"))
        assertTrue(state.dashboard.warnings.any { it.contains("Food is over its") })
        assertTrue(state.dashboard.warnings.any { it.contains("75% of your max budget") })
    }

    @Test
    fun create_mapsAchievementsIntoBadges_andComputesXp() {
        val state = BudgetBossUiStateFactory.create(
            user = user,
            categories = listOf(foodCategory),
            expenses = listOf(
                expense(amount = 20.0, expenseDate = "2026-03-10"),
                expense(amount = 15.0, expenseDate = "2026-03-11"),
            ),
            budgetGoal = BudgetGoalEntity(userId = 1, minMonthlyGoal = 10.0, maxMonthlyGoal = 100.0, updatedAt = "2026-03-01"),
            alertSettings = AlertSettingsEntity(userId = 1, enabled = false, thresholdPercent = 0.75, dayOfMonthCutoff = 20, showNotification = false, showBanner = false),
            recurringTemplates = listOf(
                RecurringTemplateEntity(
                    id = 1,
                    userId = 1,
                    description = "Gym",
                    amount = 10.0,
                    categoryId = foodCategory.id,
                    frequency = "Monthly",
                    nextScheduledDate = "2026-03-20",
                    active = true,
                ),
            ),
            achievements = listOf(
                AchievementEntity(userId = 1, badgeKey = "first_expense", unlockedAt = "2026-03-10"),
            ),
            statusMessage = null,
            now = now,
        )

        assertTrue(state.badges.first { it.key == "first_expense" }.earned)
        assertEquals(120, state.points)
        assertEquals(2, state.level)
        assertEquals(0.2f, state.xpProgress, 0f)
    }

    private fun expense(
        amount: Double,
        expenseDate: String,
    ): ExpenseEntity = ExpenseEntity(
        userId = 1,
        categoryId = foodCategory.id,
        amount = amount,
        expenseDate = expenseDate,
        startTime = "09:00",
        endTime = "09:15",
        description = "Expense",
        receiptUri = null,
        isGeneratedFromTemplate = false,
        createdAt = expenseDate,
    )
}

