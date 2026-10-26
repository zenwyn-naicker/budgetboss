package com.example.budgetboss.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetboss.core.util.todayIso
import com.example.budgetboss.data.ActionResult
import com.example.budgetboss.data.AchievementEntity
import com.example.budgetboss.data.AlertSettingsEntity
import com.example.budgetboss.data.BudgetBossDatabase
import com.example.budgetboss.data.BudgetBossRepository
import com.example.budgetboss.data.BudgetGoalEntity
import com.example.budgetboss.data.CategoryEntity
import com.example.budgetboss.data.ExpenseEntity
import com.example.budgetboss.data.FirebaseAuthDataSource
import com.example.budgetboss.data.FirestoreBudgetDataSource
import com.example.budgetboss.data.RecurringTemplateEntity
import com.example.budgetboss.data.SupabaseReceiptStorageDataSource
import com.example.budgetboss.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class UserSessionData(
    val categories: List<CategoryEntity>,
    val expenses: List<ExpenseEntity>,
    val budgetGoal: BudgetGoalEntity?,
    val alertSettings: AlertSettingsEntity?,
    val recurringTemplates: List<RecurringTemplateEntity> = emptyList(),
    val achievements: List<AchievementEntity> = emptyList(),
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BudgetBossViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BudgetBossRepository(
        database = BudgetBossDatabase.getInstance(application),
        authDataSource = FirebaseAuthDataSource(application),
        remoteDataSource = FirestoreBudgetDataSource(application),
        receiptStorageDataSource = SupabaseReceiptStorageDataSource(application),
    )
    private val currentUser = MutableStateFlow<UserEntity?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BudgetBossUiState> = currentUser
        .flatMapLatest { user ->
            if (user == null) {
                statusMessage.map { message -> BudgetBossUiState(statusMessage = message) }
            } else {
                combine(
                    repository.observeCategories(user.id),
                    repository.observeExpenses(user.id),
                    repository.observeBudgetGoals(user.id),
                    repository.observeAlertSettings(user.id),
                ) { categories, expenses, budgetGoal, alertSettings ->
                    UserSessionData(
                        categories = categories,
                        expenses = expenses,
                        budgetGoal = budgetGoal,
                        alertSettings = alertSettings,
                    )
                }.combine(repository.observeRecurringTemplates(user.id)) { partialState, recurringTemplates ->
                    partialState.copy(recurringTemplates = recurringTemplates)
                }.combine(repository.observeAchievements(user.id)) { partialState, achievements ->
                    partialState.copy(achievements = achievements)
                }.combine(statusMessage) { partialState, message ->
                    BudgetBossUiStateFactory.create(
                        user = user,
                        categories = partialState.categories,
                        expenses = partialState.expenses,
                        budgetGoal = partialState.budgetGoal ?: BudgetBossUiStateFactory.fallbackBudgetGoal(user.id),
                        alertSettings = partialState.alertSettings ?: BudgetBossUiStateFactory.fallbackAlertSettings(user.id),
                        recurringTemplates = partialState.recurringTemplates,
                        achievements = partialState.achievements,
                        statusMessage = message,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BudgetBossUiState(),
        )

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.register(email, password)
            handleAuthResult(result, successMessage = "Account created. You are now signed in.")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            val successMessage = "Welcome back, ${email.trim()}."
            handleAuthResult(result, successMessage = successMessage)
        }
    }

    fun logout() {
        repository.logout()
        currentUser.value = null
        statusMessage.value = "Logged out safely."
    }

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            val result = repository.saveExpense(expense)
            statusMessage.value = if (result.isSuccess) {
                if (expense.id == 0L) "Expense saved." else "Expense updated."
            } else {
                result.errorMessage
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            statusMessage.value = "Expense deleted."
        }
    }

    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val result = repository.saveCategory(category)
            statusMessage.value = if (result.isSuccess) {
                if (category.id == 0L) "Category saved." else "Category updated."
            } else {
                result.errorMessage
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.deleteCategory(user.id, categoryId)
            statusMessage.value = if (result.isSuccess) {
                val reassigned = result.value ?: 0
                if (reassigned > 0) {
                    "Category deleted. ${reassigned} expense(s) moved to Uncategorized."
                } else {
                    "Category deleted."
                }
            } else {
                result.errorMessage
            }
        }
    }

    fun saveBudgetGoals(minGoal: Double, maxGoal: Double) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.saveBudgetGoals(
                BudgetGoalEntity(
                    userId = user.id,
                    minMonthlyGoal = minGoal,
                    maxMonthlyGoal = maxGoal,
                    updatedAt = todayIso(),
                ),
            )
            statusMessage.value = if (result.isSuccess) "Budget goals saved." else result.errorMessage
        }
    }

    fun saveAlertSettings(settings: AlertSettingsEntity) {
        viewModelScope.launch {
            val result = repository.saveAlertSettings(settings)
            statusMessage.value = if (result.isSuccess) "Alert preferences saved." else result.errorMessage
        }
    }

    fun saveRecurringTemplate(template: RecurringTemplateEntity) {
        viewModelScope.launch {
            val result = repository.saveRecurringTemplate(template)
            statusMessage.value = if (result.isSuccess) {
                if (template.id == 0L) "Recurring template saved." else "Recurring template updated."
            } else {
                result.errorMessage
            }
        }
    }

    fun deleteRecurringTemplate(template: RecurringTemplateEntity) {
        viewModelScope.launch {
            repository.deleteRecurringTemplate(template)
            statusMessage.value = "Recurring template deleted."
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    fun expenseById(expenseId: Long): ExpenseEntity? =
        uiState.value.expenses.firstOrNull { it.id == expenseId }

    fun categoryById(categoryId: Long): CategoryEntity? =
        uiState.value.categories.firstOrNull { it.id == categoryId }

    private fun handleAuthResult(result: ActionResult<UserEntity>, successMessage: String) {
        if (result.isSuccess && result.value != null) {
            currentUser.value = result.value
            statusMessage.value = successMessage
        } else {
            statusMessage.value = result.errorMessage
        }
    }
}

