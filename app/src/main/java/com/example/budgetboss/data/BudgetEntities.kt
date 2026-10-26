/*
 * Room database entity definitions for BudgetBoss.
 * These data classes define the local tables for users, categories, expenses, and budget goals.
 */
package com.example.budgetboss.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = "",
    val username: String,
    val passwordHash: String,
    val createdAt: String,
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("userId"), Index(value = ["userId", "name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val monthlyLimit: Double?,
    val isDefault: Boolean,
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index("userId"), Index("categoryId")],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val amount: Double,
    val expenseDate: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val receiptUri: String?,
    val isGeneratedFromTemplate: Boolean,
    val createdAt: String,
)

@Entity(
    tableName = "budget_goals",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("userId")],
)
data class BudgetGoalEntity(
    @PrimaryKey val userId: Long,
    val minMonthlyGoal: Double,
    val maxMonthlyGoal: Double,
    val updatedAt: String,
)

@Entity(
    tableName = "alert_settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("userId")],
)
data class AlertSettingsEntity(
    @PrimaryKey val userId: Long,
    val enabled: Boolean,
    val thresholdPercent: Double,
    val dayOfMonthCutoff: Int,
    val showNotification: Boolean,
    val showBanner: Boolean,
)

@Entity(
    tableName = "recurring_templates",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index("userId"), Index("categoryId")],
)
data class RecurringTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val description: String,
    val amount: Double,
    val categoryId: Long,
    val frequency: String,
    val nextScheduledDate: String,
    val active: Boolean,
)

@Entity(
    tableName = "achievements",
    primaryKeys = ["userId", "badgeKey"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("userId")],
)
data class AchievementEntity(
    val userId: Long,
    val badgeKey: String,
    val unlockedAt: String,
)

