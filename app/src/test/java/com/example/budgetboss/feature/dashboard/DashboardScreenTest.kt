package com.example.budgetboss.feature.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardScreenTest {
    @Test
    fun formatDisplayName_preservesEmailAddress() {
        assertEquals("demo@gmail.com", formatDisplayName("Demo@gmail.Com"))
    }

    @Test
    fun formatDisplayName_formatsPlainUsername() {
        assertEquals("Demo User", formatDisplayName("demo_user"))
    }
}

