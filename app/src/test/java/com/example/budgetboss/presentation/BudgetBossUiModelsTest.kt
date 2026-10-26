/*
 * Unit tests for BudgetBoss UI models.
 * These tests confirm that default UI state and budget goal values are created correctly.
 */
package com.example.budgetboss.presentation

import com.example.budgetboss.data.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetBossUiModelsTest {

    @Test
    fun defaultBudgetGoal_usesExpectedDefaultValues() {
        val goal = defaultBudgetGoal(userId = 5L)

        assertEquals(5L, goal.userId)
        assertEquals(2500.0, goal.minMonthlyGoal, 0.0)
        assertEquals(5000.0, goal.maxMonthlyGoal, 0.0)
    }

    @Test
    fun uiState_isNotAuthenticated_whenNoUserExists() {
        val state = BudgetBossUiState()

        assertFalse(state.isAuthenticated)
    }

    @Test
    fun uiState_isAuthenticated_whenUserExists() {
        val state = BudgetBossUiState(
            currentUser = UserEntity(
                id = 1L,
                username = "user@example.com",
                passwordHash = "hash",
                createdAt = "2026-04-23",
            ),
        )

        assertTrue(state.isAuthenticated)
    }
}

