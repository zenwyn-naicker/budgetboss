package com.example.budgetboss.feature.insights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.formatAmount
import com.example.budgetboss.core.util.parseIsoDate
import com.example.budgetboss.core.util.startOfMonthIso
import com.example.budgetboss.core.util.thisWeekStartIso
import com.example.budgetboss.core.util.todayIso
import com.example.budgetboss.presentation.BadgeUi
import com.example.budgetboss.presentation.BudgetBossUiState
import com.example.budgetboss.presentation.GraphRange
import com.example.budgetboss.ui.components.ChartDay
import com.example.budgetboss.ui.components.ChartSegment
import com.example.budgetboss.ui.components.DailySpendingChart
import com.example.budgetboss.ui.components.DatePickerField
import com.example.budgetboss.ui.components.FeatureCardShape
import com.example.budgetboss.ui.components.ItemCardShape
import com.example.budgetboss.ui.components.SubtleBorder
import com.example.budgetboss.ui.components.parseColorSafe
import java.time.LocalDate

@Composable
fun ReportsScreen(
    state: BudgetBossUiState,
    onOpenCategoryExpenses: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    var startDate by rememberSaveable { mutableStateOf(startOfMonthIso()) }
    var endDate by rememberSaveable { mutableStateOf(todayIso()) }
    val start = runCatching { parseIsoDate(startDate) }.getOrDefault(LocalDate.MIN)
    val end = runCatching { parseIsoDate(endDate) }.getOrDefault(LocalDate.MAX)
    val filteredExpenses = state.expenses.filter {
        val date = parseIsoDate(it.expenseDate)
        !date.isBefore(start) && !date.isAfter(end)
    }
    val totalSpent = filteredExpenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(label = "Start date", isoDate = startDate, onDateSelected = { startDate = it }, modifier = Modifier.weight(1f))
            DatePickerField(label = "End date", isoDate = endDate, onDateSelected = { endDate = it }, modifier = Modifier.weight(1f))
        }
        state.categories.forEach { category ->
            val spent = filteredExpenses.filter { it.categoryId == category.id }.sumOf { it.amount }
            val progress = category.monthlyLimit?.let { (spent / it).toFloat() }
            val progressColor = when {
                progress == null -> MaterialTheme.colorScheme.primary
                progress < 0.75f -> Color(0xFF15803D)
                progress < 1f -> Color(0xFFD97706)
                else -> MaterialTheme.colorScheme.error
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCategoryExpenses(category.id) },
                shape = ItemCardShape,
                border = SubtleBorder(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(category.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("R${formatAmount(spent)}", style = MaterialTheme.typography.labelLarge)
                    }
                    Text(
                        text = category.monthlyLimit?.let { "Limit: R${formatAmount(it)}" } ?: "No category limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { (progress ?: 0f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = progressColor,
                    )
                }
            }
        }
        Card(
            shape = ItemCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Text("Total spending for this report: R${formatAmount(totalSpent)}", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GraphScreen(
    state: BudgetBossUiState,
    contentPadding: PaddingValues,
) {
    var range by rememberSaveable { mutableStateOf(GraphRange.THIS_MONTH.name) }
    var customStart by rememberSaveable { mutableStateOf(thisWeekStartIso()) }
    var customEnd by rememberSaveable { mutableStateOf(todayIso()) }
    var selectedDay by remember { mutableStateOf<ChartDay?>(null) }

    val today = LocalDate.now()
    val start = when (GraphRange.valueOf(range)) {
        GraphRange.THIS_WEEK -> today.minusDays(6)
        GraphRange.THIS_MONTH -> today.withDayOfMonth(1)
        GraphRange.CUSTOM -> runCatching { parseIsoDate(customStart) }.getOrDefault(today.minusDays(6))
    }
    val end = when (GraphRange.valueOf(range)) {
        GraphRange.THIS_WEEK, GraphRange.THIS_MONTH -> today
        GraphRange.CUSTOM -> runCatching { parseIsoDate(customEnd) }.getOrDefault(today)
    }
    val chartDays = buildChartDays(start, end, state)

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = range == GraphRange.THIS_WEEK.name, onClick = { range = GraphRange.THIS_WEEK.name }, label = { Text("This Week") })
                    FilterChip(selected = range == GraphRange.THIS_MONTH.name, onClick = { range = GraphRange.THIS_MONTH.name }, label = { Text("This Month") })
                    FilterChip(selected = range == GraphRange.CUSTOM.name, onClick = { range = GraphRange.CUSTOM.name }, label = { Text("Custom") })
                }
            }
            if (range == GraphRange.CUSTOM.name) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DatePickerField(label = "Start", isoDate = customStart, onDateSelected = { customStart = it }, modifier = Modifier.weight(1f))
                    DatePickerField(label = "End", isoDate = customEnd, onDateSelected = { customEnd = it }, modifier = Modifier.weight(1f))
                }
            }
        }
        DailySpendingChart(
            chartDays = chartDays,
            budgetGoal = state.budgetGoal,
            onDaySelected = { selectedDay = it },
        )
    }
    if (selectedDay != null) {
        AlertDialog(
            onDismissRequest = { selectedDay = null },
            confirmButton = { TextButton(onClick = { selectedDay = null }) { Text("Close") } },
            title = { Text("Spending for ${selectedDay!!.date}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Total spent: R${formatAmount(selectedDay!!.total)}",
                        fontWeight = FontWeight.Bold,
                    )
                    if (selectedDay!!.segments.isEmpty()) {
                        Text("No spending recorded for this day.")
                    } else {
                        selectedDay!!.segments
                            .sortedByDescending { it.amount }
                            .forEach { segment ->
                                Text("${segment.label}: R${formatAmount(segment.amount)}")
                            }
                    }
                }
            },
        )
    }
}

@Composable
fun ProgressScreen(
    state: BudgetBossUiState,
    onOpenReports: () -> Unit,
    contentPadding: PaddingValues,
) {
    val overallProgress = state.dashboard.progressToMax.coerceIn(0f, 1f)
    val overallColor = Color(0xFF2563EB)

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            shape = FeatureCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = SubtleBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier.size(160.dp),
                        color = overallColor,
                        trackColor = MaterialTheme.colorScheme.surface,
                        strokeWidth = 16.dp,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${(overallProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "of budget used",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.dashboard.daysRemaining}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Days left", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("R${formatAmount(state.dashboard.projectedMonthEnd)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Projected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("R${formatAmount(state.budgetGoal.maxMonthlyGoal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Max goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
                Button(onClick = onOpenReports, modifier = Modifier.fillMaxWidth(0.7f)) {
                    Text("Open Reports")
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text("Category health", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        state.categoryProgress.forEach { item ->
            val color = progressColor(item.progress)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenReports),
                shape = ItemCardShape,
                border = SubtleBorder(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(item.category.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (isOverBudget(item.progress)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Over budget warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(item.statusLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Text(
                        text = "R${formatAmount(item.spent)} spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { (item.progress ?: 0f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = color,
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsScreen(
    state: BudgetBossUiState,
    contentPadding: PaddingValues,
) {
    var selectedBadge by remember { mutableStateOf<BadgeUi?>(null) }
    val nextBadge = state.badges.firstOrNull { !it.earned }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            shape = FeatureCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = SubtleBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Level ${state.level}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("${state.points} XP total")
                LinearProgressIndicator(progress = { state.xpProgress }, modifier = Modifier.fillMaxWidth())
                Text("Current streak: ${state.dashboard.streakDays} days")
            }
        }
        Card(
            shape = ItemCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = SubtleBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Text(
                text = nextBadge?.let { "Next badge: ${it.title}. ${it.description}" }
                    ?: "Every badge is unlocked. Keep logging expenses to maintain your streak.",
                modifier = Modifier.padding(16.dp),
            )
        }
        state.badges.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { badge ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (badge.earned) selectedBadge = badge },
                        colors = CardDefaults.cardColors(
                            containerColor = if (badge.earned) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        shape = FeatureCardShape,
                        border = SubtleBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null)
                            Text(badge.title, fontWeight = FontWeight.Bold)
                            Text(badge.description)
                            Text(if (badge.earned) "Unlocked" else "Locked")
                        }
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
    if (selectedBadge != null) {
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            confirmButton = { TextButton(onClick = { selectedBadge = null }) { Text("Close") } },
            title = { Text(selectedBadge!!.title) },
            text = { Text("Unlocked on ${selectedBadge!!.unlockedAt ?: "an earlier day"}.") },
        )
    }
}

private fun buildChartDays(
    start: LocalDate,
    end: LocalDate,
    state: BudgetBossUiState,
): List<ChartDay> {
    if (end.isBefore(start)) return emptyList()
    val dates = generateSequence(start) { previous -> previous.plusDays(1).takeIf { !it.isAfter(end) } }.toList()
    return dates.map { date ->
        val dayExpenses = state.expenses.filter { parseIsoDate(it.expenseDate) == date }
        val segments = dayExpenses
            .groupBy { it.categoryId }
            .map { (categoryId, expenses) ->
                val category = state.categories.firstOrNull { it.id == categoryId }
                ChartSegment(
                    amount = expenses.sumOf { it.amount },
                    color = parseColorSafe(category?.colorHex),
                    label = category?.name ?: "Unknown",
                )
            }
        ChartDay(date = date, segments = segments)
    }
}

private fun progressColor(progress: Float?): Color = when {
    progress == null -> Color(0xFF2563EB)
    progress < 0.75f -> Color(0xFF15803D)
    progress < 1f -> Color(0xFFD97706)
    else -> Color(0xFFDC2626)
}

private fun isOverBudget(progress: Float?): Boolean = progress != null && progress >= 1f

