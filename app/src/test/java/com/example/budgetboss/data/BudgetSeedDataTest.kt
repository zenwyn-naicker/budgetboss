package com.example.budgetboss.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetSeedDataTest {

    @Test
    fun defaultCategorySeeds_containsExpectedDefaultCategories() {
        val categoryNames = defaultCategorySeeds().map { it.name }

        assertEquals(8, categoryNames.size)
        assertTrue(categoryNames.contains("Housing"))
        assertTrue(categoryNames.contains("Food"))
        assertTrue(categoryNames.contains("Transport"))
        assertTrue(categoryNames.contains("Utilities"))
        assertTrue(categoryNames.contains("Entertainment"))
        assertTrue(categoryNames.contains("Health"))
        assertTrue(categoryNames.contains("Savings"))
        assertTrue(categoryNames.contains(DEFAULT_UNCATEGORIZED_NAME))
    }
}

