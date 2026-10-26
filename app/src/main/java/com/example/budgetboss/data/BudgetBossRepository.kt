/*
 * Main data and business logic layer for BudgetBoss.
 * It validates input, handles Firebase login and registration, saves app data through Room, and creates default data.
 */
package com.example.budgetboss.data

import androidx.room.withTransaction
import com.example.budgetboss.core.util.ISO_DATE_FORMATTER
import com.example.budgetboss.core.util.parseIsoDate
import com.example.budgetboss.core.util.toIsoDateString
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class BudgetBossRepository(
    private val database: BudgetBossDatabase,
    private val authDataSource: FirebaseAuthDataSource,
    private val remoteDataSource: FirestoreBudgetDataSource,
    private val receiptStorageDataSource: SupabaseReceiptStorageDataSource,
) {
    private val dao = database.budgetDao()

    fun observeCategories(userId: Long): Flow<List<CategoryEntity>> = dao.observeCategories(userId)

    fun observeExpenses(userId: Long): Flow<List<ExpenseEntity>> = dao.observeExpenses(userId)

    fun observeBudgetGoals(userId: Long): Flow<BudgetGoalEntity?> = dao.observeBudgetGoals(userId)

    fun observeAlertSettings(userId: Long): Flow<AlertSettingsEntity?> = dao.observeAlertSettings(userId)

    fun observeRecurringTemplates(userId: Long): Flow<List<RecurringTemplateEntity>> =
        dao.observeRecurringTemplates(userId)

    fun observeAchievements(userId: Long): Flow<List<AchievementEntity>> = dao.observeAchievements(userId)

    suspend fun register(email: String, password: String): ActionResult<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail)) {
            return ActionResult(errorMessage = "Enter a valid email address.")
        }
        if (!isStrongPassword(password)) {
            return ActionResult(errorMessage = "Password must be 8+ chars with upper, lower, and number.")
        }

        val authResult = authDataSource.register(normalizedEmail, password)
        if (!authResult.isSuccess || authResult.value == null) {
            return ActionResult(errorMessage = authResult.errorMessage)
        }
        return openLocalSession(authResult.value)
    }

    suspend fun login(email: String, password: String): ActionResult<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        val normalizedPassword = password.trim()

        if (!isValidEmail(normalizedEmail) || normalizedPassword.isBlank()) {
            return ActionResult(errorMessage = "Enter a valid email and password.")
        }

        val authResult = authDataSource.login(normalizedEmail, normalizedPassword)
        if (!authResult.isSuccess || authResult.value == null) {
            return ActionResult(errorMessage = authResult.errorMessage)
        }
        return openLocalSession(authResult.value)
    }

    fun logout() {
        authDataSource.logout()
    }

    suspend fun saveExpense(expense: ExpenseEntity): ActionResult<Unit> {
        val savedId = dao.upsertExpense(expense)
        val expenseId = if (expense.id == 0L) savedId else expense.id
        var savedExpense = dao.getExpense(expenseId) ?: expense.copy(id = expenseId)
        var uploadError: String? = null
        dao.getUser(savedExpense.userId)?.let { user ->
            if (receiptStorageDataSource.shouldUpload(savedExpense.receiptUri)) {
                val uploadResult = receiptStorageDataSource.uploadReceipt(user, savedExpense)
                if (uploadResult.isSuccess && !uploadResult.value.isNullOrBlank()) {
                    savedExpense = savedExpense.copy(receiptUri = uploadResult.value)
                    dao.upsertExpense(savedExpense)
                } else {
                    uploadError = uploadResult.errorMessage ?: "Could not upload the receipt image."
                }
            }
            remoteDataSource.saveExpense(user, savedExpense)
        }
        processRecurringTemplates(savedExpense.userId)
        unlockAchievements(savedExpense.userId)
        return if (uploadError == null) {
            ActionResult(value = Unit)
        } else {
            ActionResult(errorMessage = uploadError)
        }
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
        dao.getUser(expense.userId)?.let { user ->
            remoteDataSource.deleteExpense(user, expense.id)
        }
        unlockAchievements(expense.userId)
    }

    suspend fun saveCategory(category: CategoryEntity): ActionResult<Unit> {
        val normalizedName = category.name.trim()
        if (normalizedName.isBlank()) {
            return ActionResult(errorMessage = "Category name cannot be empty.")
        }
        if (category.monthlyLimit != null && category.monthlyLimit <= 0.0) {
            return ActionResult(errorMessage = "Category limit must be positive.")
        }

        val existing = dao.findCategoryByName(category.userId, normalizedName)
        if (existing != null && existing.id != category.id) {
            return ActionResult(errorMessage = "A category with that name already exists.")
        }

        val savedId = dao.upsertCategory(category.copy(name = normalizedName))
        val savedCategory = dao.getCategory(savedId) ?: category.copy(id = savedId, name = normalizedName)
        dao.getUser(savedCategory.userId)?.let { user ->
            remoteDataSource.saveCategory(user, savedCategory)
        }
        unlockAchievements(category.userId)
        return ActionResult(value = Unit)
    }

    // Moves linked expenses to Uncategorized before deleting a category.
    suspend fun deleteCategory(userId: Long, categoryId: Long): ActionResult<Int> {
        val category = dao.getCategory(categoryId) ?: return ActionResult(errorMessage = "Category not found.")
        if (category.isDefault && category.name == DEFAULT_UNCATEGORIZED_NAME) {
            return ActionResult(errorMessage = "The Uncategorized category cannot be deleted.")
        }

        val linkedExpenses = dao.expenseCountForCategory(categoryId)
        val uncategorized = getOrCreateUncategorizedCategory(userId)
        database.withTransaction {
            dao.reassignExpenses(categoryId, uncategorized.id)
            dao.deleteCategory(category)
        }
        dao.getUser(userId)?.let { user ->
            remoteDataSource.deleteCategory(user, categoryId)
        }
        unlockAchievements(userId)
        return ActionResult(value = linkedExpenses)
    }

    suspend fun saveBudgetGoals(goal: BudgetGoalEntity): ActionResult<Unit> {
        if (goal.minMonthlyGoal <= 0.0 || goal.maxMonthlyGoal <= 0.0) {
            return ActionResult(errorMessage = "Goals must both be positive values.")
        }
        if (goal.minMonthlyGoal >= goal.maxMonthlyGoal) {
            return ActionResult(errorMessage = "Minimum goal must be less than maximum goal.")
        }

        dao.upsertBudgetGoals(goal)
        dao.getUser(goal.userId)?.let { user ->
            remoteDataSource.saveBudgetGoal(user, goal)
        }
        unlockAchievements(goal.userId)
        return ActionResult(value = Unit)
    }

    suspend fun saveAlertSettings(settings: AlertSettingsEntity): ActionResult<Unit> {
        if (settings.thresholdPercent !in 0.1..1.0) {
            return ActionResult(errorMessage = "Alert threshold must be between 10% and 100%.")
        }
        if (settings.dayOfMonthCutoff !in 1..31) {
            return ActionResult(errorMessage = "Alert day must be between 1 and 31.")
        }

        dao.upsertAlertSettings(settings)
        dao.getUser(settings.userId)?.let { remoteDataSource.saveAlertSettings(it, settings) }
        return ActionResult(value = Unit)
    }

    suspend fun saveRecurringTemplate(template: RecurringTemplateEntity): ActionResult<Unit> {
        if (template.description.trim().isBlank()) {
            return ActionResult(errorMessage = "Template description cannot be empty.")
        }
        if (template.amount <= 0.0) {
            return ActionResult(errorMessage = "Template amount must be positive.")
        }

        val savedId = dao.upsertRecurringTemplate(template.copy(description = template.description.trim()))
        val savedTemplate = template.copy(id = if (template.id == 0L) savedId else template.id)
        dao.getUser(savedTemplate.userId)?.let { remoteDataSource.saveRecurringTemplate(it, savedTemplate) }
        processRecurringTemplates(savedTemplate.userId)
        unlockAchievements(savedTemplate.userId)
        return ActionResult(value = Unit)
    }

    suspend fun deleteRecurringTemplate(template: RecurringTemplateEntity) {
        dao.deleteRecurringTemplate(template)
        dao.getUser(template.userId)?.let { remoteDataSource.deleteRecurringTemplate(it, template.id) }
        unlockAchievements(template.userId)
    }

    suspend fun getExpense(expenseId: Long): ExpenseEntity? = dao.getExpense(expenseId)

    suspend fun getCategories(userId: Long): List<CategoryEntity> = dao.getCategories(userId)

    private suspend fun openLocalSession(account: AuthenticatedAccount): ActionResult<UserEntity> {
        val existing = dao.findUserByRemoteId(account.remoteId) ?: dao.findUserByUsername(account.email)
        if (existing != null) {
            syncRemoteBudget(existing)
            return ActionResult(value = existing)
        }

        val createdAt = LocalDate.now().toIsoDateString()
        var createdUser: UserEntity? = null
        database.withTransaction {
            val userId = dao.upsertUser(
                UserEntity(
                    remoteId = account.remoteId,
                    username = account.email,
                    passwordHash = "",
                    createdAt = createdAt,
                ),
            )
            seedDefaults(userId)
            createdUser = dao.findUserByRemoteId(account.remoteId)
        }
        createdUser?.let { user ->
            remoteDataSource.saveUserProfile(user)
            pushLocalBudget(user)
        }
        return ActionResult(value = createdUser)
    }

    private suspend fun syncRemoteBudget(user: UserEntity) {
        val snapshotResult = remoteDataSource.loadSnapshot(user)
        val snapshot = snapshotResult.value
        if (snapshotResult.isSuccess && snapshot != null && snapshot.hasData) {
            database.withTransaction {
                snapshot.categories.forEach { dao.upsertCategory(it) }
                snapshot.budgetGoal?.let { dao.upsertBudgetGoals(it) }
                snapshot.alertSettings?.let { dao.upsertAlertSettings(it) }
                snapshot.recurringTemplates.forEach { dao.upsertRecurringTemplate(it) }
                snapshot.achievements.forEach { dao.upsertAchievement(it) }
                snapshot.expenses.forEach { dao.upsertExpense(it) }
            }
        } else {
            pushLocalBudget(user)
        }
    }

    private suspend fun pushLocalBudget(user: UserEntity) {
        remoteDataSource.saveUserProfile(user)
        dao.getCategories(user.id).forEach { remoteDataSource.saveCategory(user, it) }
        dao.getBudgetGoals(user.id)?.let { remoteDataSource.saveBudgetGoal(user, it) }
        dao.getAlertSettings(user.id)?.let { remoteDataSource.saveAlertSettings(user, it) }
        dao.getRecurringTemplates(user.id).forEach { remoteDataSource.saveRecurringTemplate(user, it) }
        dao.getAchievements(user.id).forEach { remoteDataSource.saveAchievement(user, it) }
        dao.getExpenses(user.id).forEach { remoteDataSource.saveExpense(user, it) }
    }

    suspend fun processRecurringTemplates(userId: Long) {
        val templates = dao.getRecurringTemplates(userId).filter { it.active }
        if (templates.isEmpty()) return

        val today = LocalDate.now()
        database.withTransaction {
            templates.forEach { template ->
                var nextDate = parseIsoDate(template.nextScheduledDate)
                while (!nextDate.isAfter(today)) {
                    dao.upsertExpense(
                        ExpenseEntity(
                            userId = userId,
                            categoryId = template.categoryId,
                            amount = template.amount,
                            expenseDate = nextDate.format(ISO_DATE_FORMATTER),
                            startTime = "09:00",
                            endTime = "09:15",
                            description = template.description,
                            receiptUri = null,
                            isGeneratedFromTemplate = true,
                            createdAt = nextDate.format(ISO_DATE_FORMATTER),
                        ),
                    )
                    nextDate = when (template.frequency) {
                        "Daily" -> nextDate.plusDays(1)
                        "Weekly" -> nextDate.plusWeeks(1)
                        else -> nextDate.plusMonths(1)
                    }
                }
                dao.upsertRecurringTemplate(
                    template.copy(nextScheduledDate = nextDate.format(ISO_DATE_FORMATTER)),
                )
            }
        }
    }

    private suspend fun seedDefaults(userId: Long) {
        defaultCategorySeeds().forEach { seed ->
            dao.upsertCategory(
                CategoryEntity(
                    userId = userId,
                    name = seed.name,
                    colorHex = seed.colorHex,
                    iconName = seed.iconName,
                    monthlyLimit = seed.monthlyLimit,
                    isDefault = seed.isDefault,
                ),
            )
        }
        dao.upsertBudgetGoals(
            BudgetGoalEntity(
                userId = userId,
                minMonthlyGoal = 2500.0,
                maxMonthlyGoal = 5000.0,
                updatedAt = LocalDate.now().toIsoDateString(),
            ),
        )
        dao.upsertAlertSettings(
            AlertSettingsEntity(
                userId = userId,
                enabled = true,
                thresholdPercent = 0.75,
                dayOfMonthCutoff = 20,
                showNotification = false,
                showBanner = true,
            ),
        )
    }

    private suspend fun getOrCreateUncategorizedCategory(userId: Long): CategoryEntity {
        val existing = dao.findCategoryByName(userId, DEFAULT_UNCATEGORIZED_NAME)
        if (existing != null) return existing

        val id = dao.upsertCategory(
            CategoryEntity(
                userId = userId,
                name = DEFAULT_UNCATEGORIZED_NAME,
                colorHex = "#64748B",
                iconName = "Category",
                monthlyLimit = null,
                isDefault = true,
            ),
        )
        return dao.getCategory(id) ?: CategoryEntity(
            id = id,
            userId = userId,
            name = DEFAULT_UNCATEGORIZED_NAME,
            colorHex = "#64748B",
            iconName = "Category",
            monthlyLimit = null,
            isDefault = true,
        )
    }

    private fun isValidEmail(value: String): Boolean =
        value.contains("@") && value.substringAfter("@").contains(".")

    private suspend fun unlockAchievements(userId: Long) {
        val expenses = dao.getExpenses(userId)
        val categories = dao.getCategories(userId)
        val templates = dao.getRecurringTemplates(userId)
        val goal = dao.getBudgetGoals(userId)
        val earnedKeys = dao.getAchievements(userId).map { it.badgeKey }.toSet()
        val today = LocalDate.now().toIsoDateString()
        val user = dao.getUser(userId)

        suspend fun unlock(key: String, unlockedAt: String = today) {
            if (key !in earnedKeys) {
                val achievement = AchievementEntity(userId = userId, badgeKey = key, unlockedAt = unlockedAt)
                dao.upsertAchievement(achievement)
                if (user != null) {
                    remoteDataSource.saveAchievement(user, achievement)
                }
            }
        }

        if (expenses.isNotEmpty()) {
            unlock("first_expense", expenses.minOf { it.expenseDate })
        }
        if (calculateCurrentStreak(expenses) >= 7) {
            unlock("seven_day_streak")
        }
        if (categories.size >= 5) {
            unlock("category_curator")
        }
        if (templates.size >= 3) {
            unlock("automation_ace")
        }
        if (goal != null) {
            val month = YearMonth.now()
            val spentThisMonth = expenses
                .filter {
                    val expenseDate = parseIsoDate(it.expenseDate)
                    expenseDate.year == month.year && expenseDate.month == month.month
                }
                .sumOf { it.amount }
            if (spentThisMonth in goal.minMonthlyGoal..goal.maxMonthlyGoal && spentThisMonth > 0.0) {
                unlock("under_budget_month")
            }
        }
    }
}

