package com.example.budgetboss.presentation

import com.example.budgetboss.core.util.formatAmount
import com.example.budgetboss.core.util.parseIsoDate
import com.example.budgetboss.core.util.toIsoDateString
import com.example.budgetboss.data.AchievementEntity
import com.example.budgetboss.data.AlertSettingsEntity
import com.example.budgetboss.data.BudgetGoalEntity
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.data.ExpenseEntity
import com.example.budgetboss.data.RecurringTemplateEntity
import com.example.budgetboss.data.UserEntity
import com.example.budgetboss.data.calculateCurrentStreak
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

object BudgetBossUiStateFactory {
    fun fallbackBudgetGoal(
        userId: Long,
        now: LocalDate = LocalDate.now(),
    ): BudgetGoalEntity = BudgetGoalEntity(userId, 2500.0, 5000.0, now.toIsoDateString())

    fun fallbackAlertSettings(userId: Long): AlertSettingsEntity =
        AlertSettingsEntity(userId, true, 0.75, 20, false, true)

    fun create(
        user: UserEntity,
        categories: List<CategoryEntity>,
        expenses: List<ExpenseEntity>,
        budgetGoal: BudgetGoalEntity,
        alertSettings: AlertSettingsEntity,
        recurringTemplates: List<RecurringTemplateEntity>,
        achievements: List<AchievementEntity>,
        statusMessage: String?,
        now: LocalDate = LocalDate.now(),
    ): BudgetBossUiState {
        val currentMonth = YearMonth.from(now)
        val thisMonthExpenses = expenses.filter { YearMonth.from(parseIsoDate(it.expenseDate)) == currentMonth }
        val totalSpent = thisMonthExpenses.sumOf { it.amount }
        val remainingBudget = budgetGoal.maxMonthlyGoal - totalSpent
        val topCategoryId = thisMonthExpenses
            .groupBy { it.categoryId }
            .maxByOrNull { (_, items) -> items.sumOf { it.amount } }
            ?.key
        val topCategoryName = categories.firstOrNull { it.id == topCategoryId }?.name ?: "No spending yet"
        val streakDays = calculateCurrentStreak(expenses, now)
        val projectedMonthEnd = projectEndOfMonthSpend(totalSpent, now, currentMonth)
        val smartAlertMessage = buildSmartAlert(
            settings = alertSettings,
            budgetGoal = budgetGoal,
            totalSpent = totalSpent,
            projected = projectedMonthEnd,
            now = now,
        )

        val warnings = mutableListOf<String>()
        categories.forEach { category ->
            val spent = thisMonthExpenses.filter { it.categoryId == category.id }.sumOf { it.amount }
            val limit = category.monthlyLimit
            if (limit != null && spent > limit) {
                warnings += "${category.name} is over its R${formatAmount(limit)} limit by R${formatAmount(spent - limit)}."
            }
        }
        if (smartAlertMessage != null && alertSettings.showBanner) {
            warnings += smartAlertMessage
        }

        val categoryProgress = categories
            .map { category ->
                val spent = thisMonthExpenses.filter { it.categoryId == category.id }.sumOf { it.amount }
                val progress = category.monthlyLimit?.takeIf { it > 0.0 }?.let { (spent / it).toFloat() }
                val statusLabel = when {
                    category.monthlyLimit == null -> "No limit"
                    progress == null -> "No limit"
                    progress < 0.75f -> "On track"
                    progress < 1f -> "Approaching limit"
                    else -> "Over limit"
                }
                CategoryProgressUi(
                    category = category,
                    spent = spent,
                    progress = progress,
                    statusLabel = statusLabel,
                )
            }
            .sortedByDescending { it.spent }

        val earnedMap = achievements.associateBy { it.badgeKey }
        val badges = badgeDefinitions().map { definition ->
            BadgeUi(
                key = definition.key,
                title = definition.title,
                description = definition.description,
                earned = earnedMap.containsKey(definition.key),
                unlockedAt = earnedMap[definition.key]?.unlockedAt,
            )
        }

        val points = expenses.size * 10 + achievements.size * 75 + streakDays * 5 + recurringTemplates.size * 15
        val level = (points / 100) + 1
        val xpProgress = (points % 100) / 100f

        return BudgetBossUiState(
            currentUser = user,
            categories = categories,
            expenses = expenses,
            budgetGoal = budgetGoal,
            alertSettings = alertSettings,
            recurringTemplates = recurringTemplates,
            achievements = achievements,
            dashboard = DashboardSnapshot(
                totalSpent = totalSpent,
                remainingBudget = remainingBudget,
                topCategoryName = topCategoryName,
                progressToMax = if (budgetGoal.maxMonthlyGoal > 0.0) {
                    (totalSpent / budgetGoal.maxMonthlyGoal).coerceIn(0.0, 1.5).toFloat()
                } else {
                    0f
                },
                rangeSummary = "You aim to spend between R${formatAmount(budgetGoal.minMonthlyGoal)} and R${formatAmount(budgetGoal.maxMonthlyGoal)} this month.",
                streakDays = streakDays,
                projectedMonthEnd = projectedMonthEnd,
                daysRemaining = (currentMonth.lengthOfMonth() - now.dayOfMonth).coerceAtLeast(0),
                warnings = warnings,
                smartAlertMessage = smartAlertMessage,
            ),
            categoryProgress = categoryProgress,
            badges = badges,
            level = level,
            points = points,
            xpProgress = xpProgress,
            statusMessage = statusMessage,
        )
    }

    private data class BadgeDefinition(
        val key: String,
        val title: String,
        val description: String,
    )

    private fun badgeDefinitions(): List<BadgeDefinition> = listOf(
        BadgeDefinition("first_expense", "First Step", "Log your first expense."),
        BadgeDefinition("seven_day_streak", "7-Day Streak", "Record spending seven days in a row."),
        BadgeDefinition("category_curator", "Category Curator", "Maintain at least five categories."),
        BadgeDefinition("automation_ace", "Automation Ace", "Create three recurring templates."),
        BadgeDefinition("under_budget_month", "Under Budget", "Stay within your monthly spending range."),
    )

    private fun buildSmartAlert(
        settings: AlertSettingsEntity,
        budgetGoal: BudgetGoalEntity,
        totalSpent: Double,
        projected: Double,
        now: LocalDate,
    ): String? {
        if (!settings.enabled) return null

        val reachedThreshold = totalSpent >= budgetGoal.maxMonthlyGoal * settings.thresholdPercent
        return when {
            reachedThreshold && now.dayOfMonth <= settings.dayOfMonthCutoff -> {
                "You have reached ${(settings.thresholdPercent * 100).roundToInt()}% of your max budget before day ${settings.dayOfMonthCutoff}."
            }
            projected > budgetGoal.maxMonthlyGoal -> {
                "At your current pace, you are projected to spend R${formatAmount(projected)} this month."
            }
            else -> null
        }
    }

    private fun projectEndOfMonthSpend(
        totalSpent: Double,
        now: LocalDate,
        month: YearMonth,
    ): Double {
        val currentDay = now.dayOfMonth.coerceAtLeast(1)
        return (totalSpent / currentDay) * month.lengthOfMonth()
    }
}

