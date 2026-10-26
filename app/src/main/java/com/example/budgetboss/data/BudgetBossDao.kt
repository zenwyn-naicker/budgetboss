/*
 * Room DAO for BudgetBoss database access.
 * It contains the queries used to insert, update, delete, and observe users, categories, expenses, and goals.
 */
package com.example.budgetboss.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetBossDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findUserByRemoteId(remoteId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: Long): UserEntity?

    @Upsert
    suspend fun upsertUser(user: UserEntity): Long

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name")
    fun observeCategories(userId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name")
    suspend fun getCategories(userId: Long): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategory(categoryId: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE userId = :userId AND lower(name) = lower(:name) LIMIT 1")
    suspend fun findCategoryByName(userId: Long, name: String): CategoryEntity?

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun expenseCountForCategory(categoryId: Long): Int

    @Query("UPDATE expenses SET categoryId = :replacementCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignExpenses(oldCategoryId: Long, replacementCategoryId: Long)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY expenseDate DESC, startTime DESC")
    fun observeExpenses(userId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY expenseDate DESC, startTime DESC")
    suspend fun getExpenses(userId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpense(expenseId: Long): ExpenseEntity?

    @Upsert
    suspend fun upsertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM budget_goals WHERE userId = :userId LIMIT 1")
    fun observeBudgetGoals(userId: Long): Flow<BudgetGoalEntity?>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId LIMIT 1")
    suspend fun getBudgetGoals(userId: Long): BudgetGoalEntity?

    @Upsert
    suspend fun upsertBudgetGoals(goal: BudgetGoalEntity)

    @Query("SELECT * FROM alert_settings WHERE userId = :userId LIMIT 1")
    fun observeAlertSettings(userId: Long): Flow<AlertSettingsEntity?>

    @Query("SELECT * FROM alert_settings WHERE userId = :userId LIMIT 1")
    suspend fun getAlertSettings(userId: Long): AlertSettingsEntity?

    @Upsert
    suspend fun upsertAlertSettings(settings: AlertSettingsEntity)

    @Query("SELECT * FROM recurring_templates WHERE userId = :userId ORDER BY nextScheduledDate")
    fun observeRecurringTemplates(userId: Long): Flow<List<RecurringTemplateEntity>>

    @Query("SELECT * FROM recurring_templates WHERE userId = :userId ORDER BY nextScheduledDate")
    suspend fun getRecurringTemplates(userId: Long): List<RecurringTemplateEntity>

    @Upsert
    suspend fun upsertRecurringTemplate(template: RecurringTemplateEntity): Long

    @Delete
    suspend fun deleteRecurringTemplate(template: RecurringTemplateEntity)

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt")
    fun observeAchievements(userId: Long): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt")
    suspend fun getAchievements(userId: Long): List<AchievementEntity>

    @Upsert
    suspend fun upsertAchievement(achievement: AchievementEntity)
}

