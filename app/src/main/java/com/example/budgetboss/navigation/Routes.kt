package com.example.budgetboss.navigation

object Routes {
    const val Dashboard = "dashboard"
    const val Expenses = "expenses"
    const val ExpenseDetail = "expense-detail"
    const val ExpenseForm = "expense-form"
    const val Categories = "categories"
    const val Goals = "goals"
    const val Reports = "reports"
    const val Graph = "graph"
    const val Progress = "progress"
    const val Achievements = "achievements"
    const val Alerts = "alerts"
    const val Recurring = "recurring"
    const val More = "more"

    fun expenseList(categoryId: Long? = null): String =
        if (categoryId == null) Expenses else "$Expenses?categoryId=$categoryId"

    fun expenseDetail(expenseId: Long): String = "$ExpenseDetail/$expenseId"

    fun expenseForm(expenseId: Long? = null): String =
        if (expenseId == null) ExpenseForm else "$ExpenseForm?expenseId=$expenseId"
}

