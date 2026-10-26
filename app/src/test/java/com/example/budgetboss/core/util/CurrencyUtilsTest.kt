/*
 * Unit tests for currency formatting.
 * These tests confirm that money values are displayed consistently in the app.
 */
package com.example.budgetboss.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun formatAmount_formatsWholeNumberWithTwoDecimals() {
        assertEquals("0,00", formatAmount(0.0))
    }

    @Test
    fun formatAmount_formatsDecimalNumberWithTwoDecimals() {
        assertEquals("12,50", formatAmount(12.5))
    }
}

