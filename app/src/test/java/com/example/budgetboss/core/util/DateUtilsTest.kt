/*
 * Unit tests for date helper functions.
 * These tests confirm that dates are parsed and formatted correctly for expenses and reports.
 */
package com.example.budgetboss.core.util

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DateUtilsTest {

    @Test
    fun toIsoDateString_formatsDateCorrectly() {
        val date = LocalDate.of(2026, 4, 23)

        assertEquals("2026-04-23", date.toIsoDateString())
    }

    @Test
    fun parseIsoDate_readsIsoDateCorrectly() {
        val result = parseIsoDate("2026-04-23")

        assertEquals(LocalDate.of(2026, 4, 23), result)
    }

    @Test
    fun displayDate_formatsDateForScreen() {
        assertEquals("23 Apr 2026", displayDate("2026-04-23"))
    }

    @Test
    fun startOfMonthIso_returnsFirstDayOfCurrentMonth() {
        val expected = LocalDate.now().withDayOfMonth(1).toIsoDateString()

        assertEquals(expected, startOfMonthIso())
    }

    @Test
    fun thisWeekStartIso_returnsSixDaysBeforeToday() {
        val expected = LocalDate.now().minusDays(6).toIsoDateString()

        assertEquals(expected, thisWeekStartIso())
    }
}

