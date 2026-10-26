package com.example.budgetboss.presentation

import com.example.budgetboss.core.util.todayIso
import com.example.budgetboss.data.AchievementEntity
import com.example.budgetboss.data.AlertSettingsEntity
import com.example.budgetboss.data.BudgetGoalEntity
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.data.ExpenseEntity
import com.example.budgetboss.data.RecurringTemplateEntity
import com.example.budgetboss.data.UserEntity

enum class ExpenseSortOption {
    DATE_DESC,
    AMOUNT_DESC,
    CATEGORY_ASC,
}

enum class GraphRange {
    THIS_WEEK,
    THIS_MONTH,
    CUSTOM,
}

data class DashboardSnapshot(
    val totalSpent: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val topCategoryName: String = "No spending yet",
    val progressToMax: Float = 0f,
    val rangeSummary: String = "Stay between your min and max budget this month.",
    val streakDays: Int = 0,
    val projectedMonthEnd: Double = 0.0,
    val daysRemaining: Int = 0,
    val warnings: List<String> = emptyList(),
    val smartAlertMessage: String? = null,
)

data class CategoryProgressUi(
    val category: CategoryEntity,
    val spent: Double,
    val progress: Float?,
    val statusLabel: String,
)

data class BadgeUi(
    val key: String,
    val title: String,
    val description: String,
    val earned: Boolean,
    val unlockedAt: String?,
)

data class BudgetBossUiState(
    val currentUser: UserEntity? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val budgetGoal: BudgetGoalEntity = BudgetGoalEntity(0, 1800.0, 5000.0, todayIso()),
    val alertSettings: AlertSettingsEntity = AlertSettingsEntity(0, true, 0.75, 20, false, true),
    val recurringTemplates: List<RecurringTemplateEntity> = emptyList(),
    val achievements: List<AchievementEntity> = emptyList(),
    val dashboard: DashboardSnapshot = DashboardSnapshot(),
    val categoryProgress: List<CategoryProgressUi> = emptyList(),
    val badges: List<BadgeUi> = emptyList(),
    val level: Int = 1,
    val points: Int = 0,
    val xpProgress: Float = 0f,
    val syncStatus: String = "Firebase and Supabase sync are enabled after project configuration is added.",
    val statusMessage: String? = null,
) {
    val isAuthenticated: Boolean = currentUser != null
}

fun defaultBudgetGoal(userId: Long = 0): BudgetGoalEntity =
    BudgetBossUiStateFactory.fallbackBudgetGoal(userId)

