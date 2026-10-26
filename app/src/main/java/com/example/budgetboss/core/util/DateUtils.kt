/*
 * Date helper functions for BudgetBoss.
 * It formats, parses, and creates date values used for expenses, reports, and default date ranges.
 */
package com.example.budgetboss.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun LocalDate.toIsoDateString(): String = format(ISO_DATE_FORMATTER)

fun parseIsoDate(raw: String): LocalDate = LocalDate.parse(raw, ISO_DATE_FORMATTER)

fun todayIso(): String = LocalDate.now().toIsoDateString()

fun startOfMonthIso(): String = LocalDate.now().withDayOfMonth(1).toIsoDateString()

fun thisWeekStartIso(): String = LocalDate.now().minusDays(6).toIsoDateString()

fun displayDate(raw: String): String =
    parseIsoDate(raw).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))


