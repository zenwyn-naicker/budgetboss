package com.example.budgetboss.data

internal const val DEFAULT_UNCATEGORIZED_NAME = "Uncategorized"
internal data class CategorySeed(
    val name: String,
    val colorHex: String,
    val iconName: String,
    val monthlyLimit: Double?,
    val isDefault: Boolean = true,
)

internal fun defaultCategorySeeds(): List<CategorySeed> = listOf(
    CategorySeed("Housing", "#C2410C", "Home", 8000.0),
    CategorySeed("Food", "#15803D", "Restaurant", 4000.0),
    CategorySeed("Transport", "#1D4ED8", "DirectionsCar", 2500.0),
    CategorySeed("Utilities", "#7C3AED", "Bolt", 2500.0),
    CategorySeed("Entertainment", "#DB2777", "Celebration", 2000.0),
    CategorySeed("Health", "#DC2626", "Favorite", 1000.0),
    CategorySeed("Savings", "#0F766E", "Savings", 5000.0),
    CategorySeed(DEFAULT_UNCATEGORIZED_NAME, "#64748B", "Category", null),
)

