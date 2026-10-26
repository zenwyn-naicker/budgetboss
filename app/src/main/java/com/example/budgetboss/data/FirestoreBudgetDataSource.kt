package com.example.budgetboss.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

data class RemoteBudgetSnapshot(
    val categories: List<CategoryEntity>,
    val expenses: List<ExpenseEntity>,
    val budgetGoal: BudgetGoalEntity?,
    val alertSettings: AlertSettingsEntity?,
    val recurringTemplates: List<RecurringTemplateEntity>,
    val achievements: List<AchievementEntity>,
) {
    val hasData: Boolean =
        categories.isNotEmpty() ||
            expenses.isNotEmpty() ||
            budgetGoal != null ||
            alertSettings != null ||
            recurringTemplates.isNotEmpty() ||
            achievements.isNotEmpty()
}

class FirestoreBudgetDataSource(
    private val context: Context,
) {
    private val isConfigured: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    suspend fun loadSnapshot(user: UserEntity): ActionResult<RemoteBudgetSnapshot> {
        if (!isConfigured || user.remoteId.isBlank()) {
            return ActionResult(value = RemoteBudgetSnapshot(emptyList(), emptyList(), null, null, emptyList(), emptyList()))
        }

        return runCatching {
            val root = FirebaseFirestore.getInstance().collection(USERS).document(user.remoteId)
            val categories = root.collection(CATEGORIES).get().await().toCategories(user.id)
            val expenses = root.collection(EXPENSES).get().await().toExpenses(user.id)
            val goalDocument = root.collection(GOALS).document(CURRENT_GOAL).get().await()
            val goal = goalDocument.data?.toBudgetGoal(user.id)
            val alertDocument = root.collection(ALERT_SETTINGS).document(CURRENT_SETTINGS).get().await()
            val alertSettings = alertDocument.data?.toAlertSettings(user.id)
            val recurringTemplates = root.collection(RECURRING_TEMPLATES).get().await().toRecurringTemplates(user.id)
            val achievements = root.collection(ACHIEVEMENTS).get().await().toAchievements(user.id)
            RemoteBudgetSnapshot(categories, expenses, goal, alertSettings, recurringTemplates, achievements)
        }.fold(
            onSuccess = { ActionResult(value = it) },
            onFailure = { ActionResult(errorMessage = it.localizedMessage ?: "Could not load cloud budget data.") },
        )
    }

    suspend fun saveUserProfile(user: UserEntity) {
        if (!isConfigured || user.remoteId.isBlank()) return

        runCloudWrite("save user profile") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .set(
                    mapOf(
                        "email" to user.username,
                        "createdAt" to user.createdAt,
                    ),
                ).await()
        }
    }

    suspend fun saveCategory(user: UserEntity, category: CategoryEntity) {
        if (!isConfigured || user.remoteId.isBlank() || category.id == 0L) return

        runCloudWrite("save category") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(CATEGORIES)
                .document(category.id.toString())
                .set(category.toRemoteMap())
                .await()
        }
    }

    suspend fun deleteCategory(user: UserEntity, categoryId: Long) {
        if (!isConfigured || user.remoteId.isBlank()) return

        runCloudWrite("delete category") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(CATEGORIES)
                .document(categoryId.toString())
                .delete()
                .await()
        }
    }

    suspend fun saveExpense(user: UserEntity, expense: ExpenseEntity) {
        if (!isConfigured || user.remoteId.isBlank() || expense.id == 0L) return

        runCloudWrite("save expense") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(EXPENSES)
                .document(expense.id.toString())
                .set(expense.toRemoteMap())
                .await()
        }
    }

    suspend fun deleteExpense(user: UserEntity, expenseId: Long) {
        if (!isConfigured || user.remoteId.isBlank()) return

        runCloudWrite("delete expense") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(EXPENSES)
                .document(expenseId.toString())
                .delete()
                .await()
        }
    }

    suspend fun saveBudgetGoal(user: UserEntity, goal: BudgetGoalEntity) {
        if (!isConfigured || user.remoteId.isBlank()) return

        runCloudWrite("save budget goal") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(GOALS)
                .document(CURRENT_GOAL)
                .set(goal.toRemoteMap())
                .await()
        }
    }

    suspend fun saveAlertSettings(user: UserEntity, settings: AlertSettingsEntity) {
        if (!isConfigured || user.remoteId.isBlank()) return

        runCloudWrite("save alert settings") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(ALERT_SETTINGS)
                .document(CURRENT_SETTINGS)
                .set(settings.toRemoteMap())
                .await()
        }
    }

    suspend fun saveRecurringTemplate(user: UserEntity, template: RecurringTemplateEntity) {
        if (!isConfigured || user.remoteId.isBlank() || template.id == 0L) return

        runCloudWrite("save recurring template") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(RECURRING_TEMPLATES)
                .document(template.id.toString())
                .set(template.toRemoteMap())
                .await()
        }
    }

    suspend fun deleteRecurringTemplate(user: UserEntity, templateId: Long) {
        if (!isConfigured || user.remoteId.isBlank()) return

        runCloudWrite("delete recurring template") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(RECURRING_TEMPLATES)
                .document(templateId.toString())
                .delete()
                .await()
        }
    }

    suspend fun saveAchievement(user: UserEntity, achievement: AchievementEntity) {
        if (!isConfigured || user.remoteId.isBlank()) return

        runCloudWrite("save achievement") {
            FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(user.remoteId)
                .collection(ACHIEVEMENTS)
                .document(achievement.badgeKey)
                .set(achievement.toRemoteMap())
                .await()
        }
    }

    private fun QuerySnapshot.toCategories(userId: Long): List<CategoryEntity> =
        documents.mapNotNull { document ->
            val id = document.id.toLongOrNull() ?: return@mapNotNull null
            val data = document.data ?: return@mapNotNull null
            CategoryEntity(
                id = id,
                userId = userId,
                name = data["name"] as? String ?: return@mapNotNull null,
                colorHex = data["colorHex"] as? String ?: "#64748B",
                iconName = data["iconName"] as? String ?: "Category",
                monthlyLimit = data.doubleOrNull("monthlyLimit"),
                isDefault = data["isDefault"] as? Boolean ?: false,
            )
        }

    private fun QuerySnapshot.toExpenses(userId: Long): List<ExpenseEntity> =
        documents.mapNotNull { document ->
            val id = document.id.toLongOrNull() ?: return@mapNotNull null
            val data = document.data ?: return@mapNotNull null
            ExpenseEntity(
                id = id,
                userId = userId,
                categoryId = data.longOrNull("categoryId") ?: return@mapNotNull null,
                amount = data.doubleOrNull("amount") ?: return@mapNotNull null,
                expenseDate = data["expenseDate"] as? String ?: return@mapNotNull null,
                startTime = data["startTime"] as? String ?: "09:00",
                endTime = data["endTime"] as? String ?: "09:30",
                description = data["description"] as? String ?: "",
                receiptUri = data["receiptUri"] as? String,
                isGeneratedFromTemplate = data["isGeneratedFromTemplate"] as? Boolean ?: false,
                createdAt = data["createdAt"] as? String ?: "",
            )
        }

    private fun QuerySnapshot.toRecurringTemplates(userId: Long): List<RecurringTemplateEntity> =
        documents.mapNotNull { document ->
            val id = document.id.toLongOrNull() ?: return@mapNotNull null
            val data = document.data ?: return@mapNotNull null
            RecurringTemplateEntity(
                id = id,
                userId = userId,
                description = data["description"] as? String ?: return@mapNotNull null,
                amount = data.doubleOrNull("amount") ?: return@mapNotNull null,
                categoryId = data.longOrNull("categoryId") ?: return@mapNotNull null,
                frequency = data["frequency"] as? String ?: "Monthly",
                nextScheduledDate = data["nextScheduledDate"] as? String ?: "",
                active = data["active"] as? Boolean ?: true,
            )
        }

    private fun QuerySnapshot.toAchievements(userId: Long): List<AchievementEntity> =
        documents.mapNotNull { document ->
            val data = document.data ?: return@mapNotNull null
            AchievementEntity(
                userId = userId,
                badgeKey = document.id,
                unlockedAt = data["unlockedAt"] as? String ?: return@mapNotNull null,
            )
        }

    private fun Map<String, Any>.toBudgetGoal(userId: Long): BudgetGoalEntity =
        BudgetGoalEntity(
            userId = userId,
            minMonthlyGoal = doubleOrNull("minMonthlyGoal") ?: 2500.0,
            maxMonthlyGoal = doubleOrNull("maxMonthlyGoal") ?: 5000.0,
            updatedAt = this["updatedAt"] as? String ?: "",
        )

    private fun Map<String, Any>.toAlertSettings(userId: Long): AlertSettingsEntity =
        AlertSettingsEntity(
            userId = userId,
            enabled = this["enabled"] as? Boolean ?: true,
            thresholdPercent = doubleOrNull("thresholdPercent") ?: 0.75,
            dayOfMonthCutoff = longOrNull("dayOfMonthCutoff")?.toInt() ?: 20,
            showNotification = this["showNotification"] as? Boolean ?: false,
            showBanner = this["showBanner"] as? Boolean ?: true,
        )

    private fun CategoryEntity.toRemoteMap(): Map<String, Any?> =
        mapOf(
            "name" to name,
            "colorHex" to colorHex,
            "iconName" to iconName,
            "monthlyLimit" to monthlyLimit,
            "isDefault" to isDefault,
        )

    private fun ExpenseEntity.toRemoteMap(): Map<String, Any?> =
        mapOf(
            "categoryId" to categoryId,
            "amount" to amount,
            "expenseDate" to expenseDate,
            "startTime" to startTime,
            "endTime" to endTime,
            "description" to description,
            "receiptUri" to receiptUri,
            "isGeneratedFromTemplate" to isGeneratedFromTemplate,
            "createdAt" to createdAt,
        )

    private fun BudgetGoalEntity.toRemoteMap(): Map<String, Any> =
        mapOf(
            "minMonthlyGoal" to minMonthlyGoal,
            "maxMonthlyGoal" to maxMonthlyGoal,
            "updatedAt" to updatedAt,
        )

    private fun AlertSettingsEntity.toRemoteMap(): Map<String, Any> =
        mapOf(
            "enabled" to enabled,
            "thresholdPercent" to thresholdPercent,
            "dayOfMonthCutoff" to dayOfMonthCutoff,
            "showNotification" to showNotification,
            "showBanner" to showBanner,
        )

    private fun RecurringTemplateEntity.toRemoteMap(): Map<String, Any> =
        mapOf(
            "description" to description,
            "amount" to amount,
            "categoryId" to categoryId,
            "frequency" to frequency,
            "nextScheduledDate" to nextScheduledDate,
            "active" to active,
        )

    private fun AchievementEntity.toRemoteMap(): Map<String, Any> =
        mapOf(
            "unlockedAt" to unlockedAt,
        )

    private fun Map<String, Any>.doubleOrNull(key: String): Double? =
        when (val value = this[key]) {
            is Double -> value
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Number -> value.toDouble()
            else -> null
        }

    private fun Map<String, Any>.longOrNull(key: String): Long? =
        when (val value = this[key]) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is Number -> value.toLong()
            else -> null
        }

    private suspend fun runCloudWrite(action: String, block: suspend () -> Unit) {
        runCatching { block() }
            .onFailure { error -> Log.w(TAG, "Could not $action in Firestore.", error) }
    }

    private suspend fun <T> Task<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
            addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
            addOnCanceledListener {
                continuation.cancel()
            }
        }

    private companion object {
        const val TAG = "FirestoreBudgetData"
        const val USERS = "users"
        const val CATEGORIES = "categories"
        const val EXPENSES = "expenses"
        const val GOALS = "goals"
        const val CURRENT_GOAL = "current"
        const val ALERT_SETTINGS = "alertSettings"
        const val CURRENT_SETTINGS = "current"
        const val RECURRING_TEMPLATES = "recurringTemplates"
        const val ACHIEVEMENTS = "achievements"
    }
}

